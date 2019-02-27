package org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.team1806.Vision.Util.CameraCalculationInformation;
import org.usfirst.frc.team1806.Vision.Util.RigidTransform2d;
import org.usfirst.frc.team1806.Vision.Util.Target;
import org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor.ReflectiveTapeTarget.PieceOfTape;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ReflectiveTapeTargetExtractor implements TargetExtractor {

    public ReflectiveTapeTargetExtractor(){

    }

    private ArrayList<PieceOfTape> tapeStrips = new ArrayList<>();
    private ArrayList<Target> bays = new ArrayList<>();

    private boolean wasLastLeft = false;

    @Override
    public ArrayList<Target> processTargetInformation(ArrayList<MatOfPoint> outputFromFilter, CameraCalculationInformation cameraInfo, RigidTransform2d cameraOffset){
        tapeStrips.clear();
        for(MatOfPoint contour: outputFromFilter){
            Point[]pointsInContour = contour.toArray();
            Point left = pointsInContour[0];
            Point top = pointsInContour[0];
            Point right = pointsInContour[0];
            for(Point point:pointsInContour){
                if(point.x < left.x){
                    left = point;
                }
                if(point.x > right.x){
                    right = point;
                }
                if(point.y < top.y){
                    top = point;
                }
            }
            tapeStrips.add(new PieceOfTape(left, top, right));
        }

        if(tapeStrips.size() > 0){
            System.out.println("Found: " + tapeStrips.size() + " targets");
        }

        return new ArrayList<Target>();
    }



}
