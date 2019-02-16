package org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team1806.Vision.Util.CameraCalculationInformation;
import org.usfirst.frc.team1806.Vision.Util.RigidTransform2d;
import org.usfirst.frc.team1806.Vision.Util.Target;

import java.util.ArrayList;

public class ReflectiveTapeTargetExtractor implements TargetExtractor {



    public ReflectiveTapeTargetExtractor(){

    }

    @Override
    public ArrayList<Target> processTargetInformation(ArrayList<MatOfPoint> outputFromFilter, CameraCalculationInformation cameraInfo, RigidTransform2d cameraOffset){
        return new ArrayList<Target>();
    }



}
