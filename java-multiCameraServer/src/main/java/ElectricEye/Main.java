package ElectricEye;
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import Messages.HeartbeatMessage;
import Messages.TargetsMessage;
import Messages.UnknownTypeMessage;
import Messages.VisionMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import Util.Target;
import VisionPipeline.ReflectiveTapePipeline;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionThread;

import org.opencv.core.Mat;

/*
   JSON format:
   {
       "team": <team number>,
       "ntmode": <"client" or "server", "client" if unspecified>
       "cameras": [
           {
               "name": <camera name>
               "path": <path, e.g. "/dev/video0">
               "pixel format": <"MJPEG", "YUYV", etc>   // optional
               "width": <video mode width>              // optional
               "height": <video mode height>            // optional
               "fps": <video mode fps>                  // optional
               "brightness": <percentage brightness>    // optional
               "white balance": <"auto", "hold", value> // optional
               "exposure": <"auto", "hold", value>      // optional
               "properties": [                          // optional
                   {
                       "name": <property name>
                       "value": <property value>
                   }
               ],
               "stream": {                              // optional
                   "properties": [
                       {
                           "name": <stream property name>
                           "value": <stream property value>
                       }
                   ]
               }
           }
       ]
       "switched cameras": [
           {
               "name": <virtual camera name>
               "key": <network table key used for selection>
               // if NT value is a string, it's treated as a name
               // if NT value is a double, it's treated as an integer index
           }
       ]
   }
 */

public final class Main {
  private static String configFile = "/boot/frc.json";

  public static ArrayList<Target> targets;
  public static double lastRIOHeartBeatTimestamp;
  public static double lastRIOHeartBeatPiTime;
  public static long lastSentHeartBeat;
  public static Double targetTimestamp = 0.0;
  public static Double imageTimestamp = 0.0;
  public static boolean shouldRun = true;
  public static Socket mSocket;
  public static Boolean mConnected;
  private static ConnectThread mConnThread;
  private static WriteThread mWriteThread;
  private static ReceiveThread mReceiveThread;

  @SuppressWarnings("MemberName")
  public static class CameraConfig {
    public String name;
    public String path;
    public JsonObject config;
    public JsonElement streamConfig;
  }

  @SuppressWarnings("MemberName")
  public static class SwitchedCameraConfig {
    public String name;
    public String key;
  }

  public static int team;
  public static boolean server;
  public static List<CameraConfig> cameraConfigs = new ArrayList<>();
  public static List<SwitchedCameraConfig> switchedCameraConfigs = new ArrayList<>();
  public static List<VideoSource> cameras = new ArrayList<>();

  private Main() {
  }

  /**
   * Report parse error.
   */
  public static void parseError(String str) {
    System.err.println("config error in '" + configFile + "': " + str);
  }

  /**
   * Read single camera configuration.
   */
  public static boolean readCameraConfig(JsonObject config) {
    CameraConfig cam = new CameraConfig();

    // name
    JsonElement nameElement = config.get("name");
    if (nameElement == null) {
      parseError("could not read camera name");
      return false;
    }
    cam.name = nameElement.getAsString();

    // path
    JsonElement pathElement = config.get("path");
    if (pathElement == null) {
      parseError("camera '" + cam.name + "': could not read path");
      return false;
    }
    cam.path = pathElement.getAsString();

    // stream properties
    cam.streamConfig = config.get("stream");

    cam.config = config;

    cameraConfigs.add(cam);
    return true;
  }

  /**
   * Read single switched camera configuration.
   */
  public static boolean readSwitchedCameraConfig(JsonObject config) {
    SwitchedCameraConfig cam = new SwitchedCameraConfig();

    // name
    JsonElement nameElement = config.get("name");
    if (nameElement == null) {
      parseError("could not read switched camera name");
      return false;
    }
    cam.name = nameElement.getAsString();

    // path
    JsonElement keyElement = config.get("key");
    if (keyElement == null) {
      parseError("switched camera '" + cam.name + "': could not read key");
      return false;
    }
    cam.key = keyElement.getAsString();

    switchedCameraConfigs.add(cam);
    return true;
  }

  /**
   * Read configuration file.
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  public static boolean readConfig() {
    // parse file
    JsonElement top;
    try {
      top = new JsonParser().parse(Files.newBufferedReader(Paths.get(configFile)));
    } catch (IOException ex) {
      System.err.println("could not open '" + configFile + "': " + ex);
      return false;
    }

    // top level must be an object
    if (!top.isJsonObject()) {
      parseError("must be JSON object");
      return false;
    }
    JsonObject obj = top.getAsJsonObject();

    // team number
    JsonElement teamElement = obj.get("team");
    if (teamElement == null) {
      parseError("could not read team number");
      return false;
    }
    team = teamElement.getAsInt();

    // ntmode (optional)
    if (obj.has("ntmode")) {
      String str = obj.get("ntmode").getAsString();
      if ("client".equalsIgnoreCase(str)) {
        server = false;
      } else if ("server".equalsIgnoreCase(str)) {
        server = true;
      } else {
        parseError("could not understand ntmode value '" + str + "'");
      }
    }

    // cameras
    JsonElement camerasElement = obj.get("cameras");
    if (camerasElement == null) {
      parseError("could not read cameras");
      return false;
    }
    JsonArray cameras = camerasElement.getAsJsonArray();
    for (JsonElement camera : cameras) {
      if (!readCameraConfig(camera.getAsJsonObject())) {
        return false;
      }
    }

    if (obj.has("switched cameras")) {
      JsonArray switchedCameras = obj.get("switched cameras").getAsJsonArray();
      for (JsonElement camera : switchedCameras) {
        if (!readSwitchedCameraConfig(camera.getAsJsonObject())) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Start running the camera.
   */
  public static VideoSource startCamera(CameraConfig config) {
    System.out.println("Starting camera '" + config.name + "' on " + config.path);
    CameraServer inst = CameraServer.getInstance();
    UsbCamera camera = new UsbCamera(config.name, config.path);
    MjpegServer server = inst.startAutomaticCapture(camera);

    Gson gson = new GsonBuilder().create();

    camera.setConfigJson(gson.toJson(config.config));
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);

    if (config.streamConfig != null) {
      server.setConfigJson(gson.toJson(config.streamConfig));
    }

    return camera;
  }

  /**
   * Start running the switched camera.
   */
  public static MjpegServer startSwitchedCamera(SwitchedCameraConfig config) {
    System.out.println("Starting switched camera '" + config.name + "' on " + config.key);
    MjpegServer server = CameraServer.getInstance().addSwitchedCamera(config.name);

    NetworkTableInstance.getDefault()
        .getEntry(config.key)
        .addListener(event -> {
              if (event.value.isDouble()) {
                int i = (int) event.value.getDouble();
                if (i >= 0 && i < cameras.size()) {
                  server.setSource(cameras.get(i));
                }
              } else if (event.value.isString()) {
                String str = event.value.getString();
                for (int i = 0; i < cameraConfigs.size(); i++) {
                  if (str.equals(cameraConfigs.get(i).name)) {
                    server.setSource(cameras.get(i));
                    break;
                  }
                }
              }
            },
            EntryListenerFlags.kImmediate | EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);

    return server;
  }

  /**
   * Example pipeline.
   */

  /**
   * Main.
   */
  public static void main(String... args) {
    if (args.length > 0) {
      configFile = args[0];
    }

    // read configuration
    if (!readConfig()) {
      return;
    }

    // start NetworkTables
    NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
    if (server) {
      System.out.println("Setting up NetworkTables server");
      ntinst.startServer();
    } else {
      System.out.println("Setting up NetworkTables client for team " + team);
      ntinst.startClientTeam(team);
    }

    System.out.println("Setting up high speed comms with:" + Constants.K_ROBOOT_PROXY_HOST + ":" + Constants.K_ROBOT_PORT);
    mConnThread = new ConnectThread();
    new Thread(mConnThread).start();
    mReceiveThread = new ReceiveThread();
    new Thread(mReceiveThread).start();
    mWriteThread = new WriteThread();
    new Thread(mWriteThread).start();

    // start cameras
    for (CameraConfig config : cameraConfigs) {
      cameras.add(startCamera(config));
    }

    // start switched cameras
    for (SwitchedCameraConfig config : switchedCameraConfigs) {
      startSwitchedCamera(config);
    }

    // start image processing on camera 0 if present
    if (cameras.size() >= 1) {
      VisionThread visionThread = new VisionThread(cameras.get(0),
              new ReflectiveTapePipeline(), pipeline -> {
              targets = pipeline.getTargets();
        // do something with pipeline results
      });
      /* something like this for GRIP:
      VisionThread visionThread = new VisionThread(cameras.get(0),
              new GripPipeline(), pipeline -> {
        ...
      });
       */
      visionThread.start();
    }

    // loop forever
    for (;;) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }


  protected static class ReceiveThread implements Runnable {

    public void handleMessage(UnknownTypeMessage message) {
      if ("heartbeat".equals(message.getType())) {
        HeartbeatMessage heartbeat = new HeartbeatMessage(message.getMessage());
        if(heartbeat.isValid()){
          lastRIOHeartBeatPiTime = System.currentTimeMillis();
          lastRIOHeartBeatTimestamp = heartbeat.getTimestamp();
        }

      }
      if ("hatch_mode".equals(message.getType())) {
        //TODO tell lights to indicate hatch mode
      }
      if ("cargo_modo".equals(message.getType())) {
        //TODO tell lights to indicate cargo mode
      }
      if ("climb_complete".equals(message.getType())){
        //TODO tell the lights to play climb complete animation
      }
      if ("scoring".equals(message.getType())){
        //TODO tell the lights to play scoring animation
      }/*
      if ("target_cargo".equals(message.getType())){
        changeVisionMode(VisionMode.MODE_CARGO);
      }
      if ("target_tape".equals(message.getType())){
        changeVisionMode(VisionMode.MODE_REFLECTIVE_TAPE);
      }
      if ("target_nothing".equals(message.getMessage())){
        changeVisionMode(VisionMode.NOT_TARGETING);
      }
*/
      System.out.println("Connection:" + message.getType() + " " + message.getMessage());
    }

    @Override
    public void run() {
      while (shouldRun) {
        if (mSocket != null || mConnected) {
          BufferedReader reader;
          try {
            InputStream is = mSocket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
          } catch (IOException e) {
            System.out.println("ReceiveThread " + "Could not get input stream");
            continue;
          } catch (NullPointerException npe) {
            System.out.println("ReceiveThread " + "socket was null");
            continue;
          }
          String jsonMessage = null;
          try {
            jsonMessage = reader.readLine();
          } catch (IOException e) {
          }
          if (jsonMessage != null) {
            UnknownTypeMessage parsedMessage = new UnknownTypeMessage(jsonMessage);
            if (parsedMessage.isValid()) {
              handleMessage(parsedMessage);
            }
          }
        } else {
          try {
            Thread.sleep(100, 0);
          } catch (InterruptedException e) {
          }
        }
      }
    }
  }

  protected static class WriteThread implements Runnable {
    @Override
    public void run(){
      while(shouldRun){
        sendMessage(new TargetsMessage(targets, targetTimestamp));
        try {
          Thread.sleep(10);
        }
        catch(InterruptedException e){
          System.out.println("Write thread sleep interrupted");
        }
      }
    }
  }

  protected static class ConnectThread implements Runnable {

    @Override
    public void run() {
      while (shouldRun) {
        if (mSocket == null || !mSocket.isConnected() && !mConnected) {
          attemptConnection();
        }

        long now = System.currentTimeMillis();

        if(now - lastSentHeartBeat > Constants.K_SEND_HEARTBEAT_PERIOD){
          sendMessage(new HeartbeatMessage(getCurrentEstimatedTimestamp()));
          lastSentHeartBeat = now;
        }

        if(Math.abs(lastRIOHeartBeatPiTime - lastSentHeartBeat) > Constants.K_THRESHOLD_HEARTBEAT && mConnected){
          mConnected = false;
          System.out.println("Robot disconnected");
        }

        if(Math.abs(lastRIOHeartBeatPiTime- lastSentHeartBeat) < Constants.K_THRESHOLD_HEARTBEAT && !mConnected){
          mConnected = true;
          System.out.println("Robot connected");
        }
        try {
          Thread.sleep(Constants.K_CONNECTOR_SLEEP_MS);
        } catch (InterruptedException e){
          System.out.println("Connector sleeping interrupted. Weird.");
        }
      }
    }
  }

  private static void attemptConnection(){
    if(mSocket == null){
      try{
        mSocket = new Socket(Constants.K_ROBOOT_PROXY_HOST, Constants.K_ROBOT_PORT);
        mSocket.setSoTimeout(100);
      } catch (IOException e){
        //System.out.println("Could not connect to robot");
        mSocket = null;
        mConnected = false;
      }
    }
  }

  private static void sendMessage(VisionMessage message){
    if(mSocket != null){
      try{
        synchronized (mSocket){
          mSocket.getOutputStream().write(message.getMessage().getBytes());
        }

      } catch(IOException e){
        System.out.println("Could not send" + message.getType());
      }
    }
  }

  public static double getCurrentEstimatedTimestamp(){
    return lastRIOHeartBeatTimestamp + ((System.currentTimeMillis() - lastRIOHeartBeatPiTime) / 1000.0);
  }

}
