package org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor;

import org.opencv.core.MatOfPoint;
import org.usfirst.frc.team1806.Vision.Util.CameraCalculationInformation;
import org.usfirst.frc.team1806.Vision.Util.RigidTransform2d;
import org.usfirst.frc.team1806.Vision.Util.Target;
import org.usfirst.frc.team1806.Vision.Util.Translation2d;

import java.util.ArrayList;

public interface TargetExtractor {

    /**
     *
     * @param outputFromFilter
     * @return
     */
    ArrayList<Target> processTargetInformation(ArrayList<MatOfPoint> outputFromFilter, CameraCalculationInformation cameraInfo, RigidTransform2d cameraOffset);

}
