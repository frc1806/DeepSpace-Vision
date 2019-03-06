package org.usfirst.frc.team1806.Vision;

import org.opencv.core.Size;

public class Constants {
    public static final int K_ROBOT_PORT = 8806;
    public static final String K_ROBOOT_PROXY_HOST = "roborio-frc-1806.local";
    public static final int K_CONNECTOR_SLEEP_MS = 100;
    public static final int K_THRESHOLD_HEARTBEAT = 800;
    public static final int K_SEND_HEARTBEAT_PERIOD = 100;

    public static final int K_CAMERA_INIT_SLEEP = 1500;

    public static Size K_STREAM_SIZE = new Size(320, 240);
}
