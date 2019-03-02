package org.usfirst.frc.team1806.Vision;

import java.net.Socket;
import java.util.ArrayList;

import edu.wpi.cscore.CvSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.CircularBuffer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import org.usfirst.frc.team1806.Vision.Util.*;
import org.usfirst.frc.team1806.Vision.VisionPipeline.Filter.*;
import org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor.CargoTargetExtractor;
import org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor.ReflectiveTapeTargetExtractor;
import org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor.TargetExtractor;

public class ElectricEye {
    //	Process for GRIP
    public static VideoCapture videoCaptureA;
    public static VideoCapture videoCaptureB;
    public static CameraServer cameraServer;
    public static CvSource cvSource;

    private static Mat matA;
    private static Mat matB;

    public static Boolean newImageAStream = false;
    public static Boolean newImageB = false;

    volatile static private Socket mSocket;
    private static boolean mConnected;

    public static ArrayList<Target> targets;
    public static double lastRIOHeartBeatTimestamp;
    public static double lastRIOHeartBeatPiTime;


    protected static class ProcessThread implements Runnable {
        SWATCircularBuffer runTimes = new SWATCircularBuffer(30);
        @Override
        public void run() {
            int frame = 0;
            Boolean newProcImg = false;
            double startTime = System.nanoTime();
            while (shouldRun) {
                synchronized (newImageAStream) {
                    newProcImg = newImageAStream;
                }
                if (newProcImg) {
                    Mat processMat = new Mat();
                    synchronized (matA) {
                        matA.copyTo(processMat);
                        synchronized (newImageAStream){
                            newImageAStream = false;
                        }
                        matA.release();
                    }
                        if (processMat != null) {
                            processImage(processMat);
                        }
                    processMat.release();
                    runTimes.addLast(System.nanoTime() - startTime);
                    frame ++;
                    if(frame % 30 == 29){
                        System.out.println("Processing FPS:" + 1000000000 / runTimes.getAverage());
                    }
                    startTime = System.nanoTime();
                }

            }

        }

        public void processImage(Mat matOriginal) {

//		only run for the specified time
            Mat matFlipper = new Mat();
            if (processCamera.getIsFlipped()) {
                Core.flip(matOriginal, matFlipper, 0);
            } else {
                matFlipper = matOriginal;
            }

            //System.out.println("Hey I'm Processing Something!");

            if (visionMode.getFilter() != null && visionMode.getTargetExtractor() != null) {
                visionMode.getFilter().process(matFlipper);
                ArrayList<Target> targets = visionMode.getTargetExtractor().processTargetInformation(visionMode.getFilter().getOutput(), processCamera.getCameraCalculationInformation(), processCamera.getCameraOffset());
            }
            // matFlipper.release();
            //TODO:Send targets to robot
            frame++;
        }

        public void processWithPNP(Mat matOriginal) {
            Mat matFlipper = new Mat();
            if (processCamera.getIsFlipped()) {
                Core.flip(matOriginal, matFlipper, 0);
            } else {
                matFlipper = matOriginal;
            }

            if (visionMode.getFilter() != null && visionMode.getTargetExtractor() != null) {
                visionMode.getFilter().process(matFlipper);
                ArrayList<Target> targets = visionMode.getTargetExtractor().processTargetInformation(visionMode.getFilter().getOutput(), processCamera.getCameraCalculationInformation(), processCamera.getCameraOffset());

                MatOfPoint3f objPoints = new MatOfPoint3f(new Point3[]{new Point3(-5.938, 2.938, 0.0), new Point3(-4.063, 2.375, 0.0),
                        new Point3(-5.438, -2.938, 0.0), new Point3(-7.375, -2.500, 0.0), new Point3(3.938, 2.375, 0.0),
                        new Point3(5.875, 2.875, 0.0), new Point3(7.313, -2.500, 0.0), new Point3(5.375, -2.938, 0.0)});

            /*
                # Left target
    (-5.938, 2.938, 0.0), # top left
    (-4.063, 2.375, 0.0), # top right
    (-5.438, -2.938, 0.0), # bottom left
    (-7.375, -2.500, 0.0), # bottom right

    # Right target
    (3.938, 2.375, 0.0), # top left
    (5.875, 2.875, 0.0), # top right
    (7.313, -2.500, 0.0), # bottom left
    (5.375, -2.938, 0.0), # bottom right
             */
            }


        }
    }

    protected static class StreamThread implements Runnable {

        @Override
        public void run() {
            int frame = 0;
            Boolean newStreamImage = false;
            double startTime = System.nanoTime();
            SWATCircularBuffer runTimes = new SWATCircularBuffer(5);
            while (shouldRun) {
                synchronized (newImageB) {
                    newStreamImage = newImageB;
                }
                if (newStreamImage) {
                    Mat streamMat = new Mat();
                    synchronized (matB) {
                        matB.copyTo(streamMat);
                        synchronized (newImageB){
                            newImageB = false;
                        }
                        matB.release();
                    }
                        if (streamMat != null && !streamMat.empty()) {
                            Mat resizeMat = new Mat();
                            Imgproc.resize(streamCamera == StreamCamera.VISION_POST_PROCESSING? overlayInfo(streamMat):streamMat, resizeMat, Constants.K_STREAM_SIZE);
                            streamImage(resizeMat);
                            resizeMat.release();
                    }
                    streamMat.release();
                        runTimes.addLast(System.nanoTime() - startTime);
                        frame ++;
                        if(frame % 30 == 29){
                            System.out.println("Stream FPS:" + 1000000000/ runTimes.getAverage());
                        }
                        startTime = System.nanoTime();
                }
            }
        }

        public Mat overlayInfo(Mat matToOverlay) {
            //TODO: Make overlay
            return matToOverlay;
        }

        public void streamImage(Mat matToStream) {
            cvSource.putFrame(matToStream);


        }
    }

    protected static class AcquisitionThread implements Runnable {

        Mat tempMat;
        @Override
        public void run() {
            while (shouldRun) {

                if (streamCamera.cameraID == processCamera.cameraID) {
                    tempMat = new Mat();
                    synchronized (videoCaptureA) {
                        videoCaptureA.read(tempMat);
                        synchronized (matA) {
                            tempMat.copyTo(matA);
                            synchronized (newImageAStream) {
                                newImageAStream = true;
                            }
                        }
                        synchronized (matB) {
                            tempMat.copyTo(matB);
                            synchronized (newImageB) {
                                newImageB = true;
                            }
                        }
                    }
                    tempMat.release();
                }
                else {

                    synchronized (videoCaptureA) {
                        synchronized (matA) {
                            videoCaptureA.read(matA);
                            synchronized (newImageAStream) {
                                newImageAStream = true;
                            }
                        }

                    }
                    synchronized (videoCaptureB) {
                        synchronized (matB) {
                            videoCaptureB.read(matB);
                            synchronized (newImageB) {
                                newImageB = true;
                            }
                        }
                    }

                }
            }
        }
    }

    protected static class CommThread implements Runnable {

        @Override
        public void run() {
            //TODO: Send things to robot, accept heartbeats, etc
            while (shouldRun) {
                if (!mConnected) {
                    continue;
                } else {
                    continue;
                }
            }
        }
    }

    private static ProcessThread mProcThread;
    private static StreamThread mStreamThread;
    private static AcquisitionThread mAcquisitionThread;
    private static CommThread mCommThread;


    public static boolean shouldRun = true;

    static NetworkTable table;


    public enum VisionMode {
        MODE_REFLECTIVE_TAPE(new TapeTestFilter(), new ReflectiveTapeTargetExtractor()),
        MODE_CARGO(new CargoFilter(), new CargoTargetExtractor()),
        NOT_TARGETING(null, null);

        private Filter filter;
        private TargetExtractor targetExtractor;


        VisionMode(Filter filter, TargetExtractor targetExtractor) {
            this.filter = filter;
            this.targetExtractor = targetExtractor;

        }

        Filter getFilter() {
            return filter;
        }

        TargetExtractor getTargetExtractor() {
            return targetExtractor;
        }
    }

    public enum StreamCamera {
        CLAW_CAM(1, false),
        FORWARD_FIXED(0, false),
        REAR_FIXED(2, false),
        VISION_POST_PROCESSING(-1, false);

        private int cameraID;
        private boolean isFlipped;

        StreamCamera(int id, boolean isFlipped) {
            cameraID = id;
            this.isFlipped = isFlipped;
        }

        public int getCameraID() {
            return cameraID;
        }

        public boolean isFlipped() {
            return isFlipped;
        }

    }


    public enum ProcessCamera {
        FORWARD(0, new CameraCalculationInformation(1920, 1080, 64.4), false, new RigidTransform2d(new Translation2d(-1, 12), new Rotation2d().fromDegrees(0))),
        REVERSE(2, new CameraCalculationInformation(1920, 1080, 64.4), false, new RigidTransform2d(new Translation2d(0, 0), new Rotation2d().fromDegrees(180)));

        private int cameraID;
        private CameraCalculationInformation cameraCalculationInformation;
        private boolean isFlipped;
        private RigidTransform2d cameraOffset;

        ProcessCamera(int cameraID, CameraCalculationInformation cameraCalculationInformation, boolean isFlipped, RigidTransform2d cameraOffset) {
            this.cameraID = cameraID;
            this.cameraCalculationInformation = cameraCalculationInformation;
            this.isFlipped = isFlipped;
            this.cameraOffset = cameraOffset;
        }

        public int getCameraId() {
            return cameraID;
        }

        public CameraCalculationInformation getCameraCalculationInformation() {
            return cameraCalculationInformation;
        }

        boolean getIsFlipped() {
            return isFlipped;
        }

        public RigidTransform2d getCameraOffset() {
            return cameraOffset;
        }

    }


    private static VisionMode visionMode;
    private static ProcessCamera processCamera;
    private static StreamCamera streamCamera;
    private static int frame = 0;

    public static void main(String[] args) {
        visionMode = VisionMode.MODE_REFLECTIVE_TAPE;
        matA = new Mat();
        matB = new Mat();
        processCamera = ProcessCamera.FORWARD;
        streamCamera = StreamCamera.FORWARD_FIXED;
        cameraServer = CameraServer.getInstance();
        cameraServer.addServer("Stream");
        cvSource = cameraServer.putVideo("output", 320, 240);


        videoCaptureB = new VideoCapture();
        videoCaptureA = new VideoCapture();
        /* TODO: Network tables too slow, implement TCP comms

         */

        if (shouldRun) {
            try {
                while (!initCameras()) {
                    System.out.println("Camera Initialization Unsuccessful! Is everything plugged in?");
                    Thread.sleep(Constants.K_CAMERA_INIT_SLEEP);
                }
                System.out.println("Camera Initialization Successful!");
                visionMode.getFilter().configureCamera(videoCaptureA);

                //Start Threads
                mAcquisitionThread = new AcquisitionThread();
                new Thread(mAcquisitionThread).start();
                while(!newImageAStream)Thread.sleep(16);
                mProcThread = new ProcessThread();
                new Thread(mProcThread).start();
                while(!newImageB) Thread.sleep(16);
                mStreamThread = new StreamThread();
                new Thread(mStreamThread).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //		make sure the java process quits when the loop finishes
            releaseCameras();
            System.exit(0);
        }

    }


    public static void changeVisionMode(VisionMode newMode) {
        visionMode = newMode;
        visionMode.getFilter().configureCamera(videoCaptureA);
    }

    public static void changeProcessCamera(ProcessCamera processCamera) {
        ElectricEye.processCamera = processCamera;
        initCameras();
    }

    public static void changeStreamCamera(StreamCamera streamCamera) {
        ElectricEye.streamCamera = streamCamera;
        initCameras();
    }

    /**
     * initializes the cameras.
     *
     * @return True if successful, False if not.
     */
    private static boolean initCameras() {
        releaseCameras();
        if (videoCaptureA == null) {
            videoCaptureA = new VideoCapture();
        }
        if (videoCaptureB == null) {
            videoCaptureB = new VideoCapture();
        }
        if (processCamera.cameraID == streamCamera.cameraID) {
            videoCaptureA.open(processCamera.cameraID);
            if (videoCaptureA.isOpened()) {
                visionMode.getFilter().configureCamera(videoCaptureA);
            }
            return videoCaptureA.isOpened();
        } else {
            videoCaptureA.open(processCamera.cameraID);
            if (streamCamera != StreamCamera.VISION_POST_PROCESSING) {
                videoCaptureB.open(streamCamera.cameraID);
            }
            return videoCaptureA.isOpened() && (videoCaptureB.isOpened() || streamCamera == StreamCamera.VISION_POST_PROCESSING);
        }
    }


    private static void releaseCameras() {
        if (videoCaptureA != null && videoCaptureA.isOpened()) {
            videoCaptureA.release();
        }
        if (videoCaptureB != null && videoCaptureB.isOpened()) {
            videoCaptureB.release();
        }
    }

    private static boolean isEitherCaptureNull() {
        return videoCaptureA == null || videoCaptureB == null;
    }


}