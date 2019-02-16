package org.usfirst.frc.team1806.Vision.VisionPipeline.Filter;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.videoio.VideoCapture;
import java.util.ArrayList;

public interface Filter {

    /**
     *
     * @param source0
     */
    void process(Mat source0);

    ArrayList<MatOfPoint> getOutput();

    /**
     *
     * @param camera
     */
    void configureCamera(VideoCapture camera);


}
