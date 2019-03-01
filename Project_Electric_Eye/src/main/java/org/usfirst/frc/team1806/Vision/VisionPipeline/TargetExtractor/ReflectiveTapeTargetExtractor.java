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
import java.util.Comparator;

public class ReflectiveTapeTargetExtractor implements TargetExtractor {

    public ReflectiveTapeTargetExtractor(){

    }

    private ArrayList<PieceOfTape> tapeStrips = new ArrayList<>();
    private ArrayList<Target> bays = new ArrayList<>();


    @Override
    public ArrayList<Target> processTargetInformation(ArrayList<MatOfPoint> outputFromFilter, CameraCalculationInformation cameraInfo, RigidTransform2d cameraOffset){
        tapeStrips.clear();
        bays.clear();
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
        tapeStrips.sort(new sortByX());

        for(int i = 0; i < tapeStrips.size(); i += 2) {
            System.out.println("SSS " + tapeStrips.get(i).mTapeType);
            if(tapeStrips.get(i).mTapeType.equals(PieceOfTape.TapeType.LEFT) && tapeStrips.size() > i + 1) {
                bays.add(new Target(tapeStrips.get(i), tapeStrips.get(i + 1)));
            }
            else {
                i++;
            }
        }


        System.out.println("tapeStrips: " + tapeStrips.size());
        System.out.println("bays: " + bays.size());


        return bays;
    }

    class sortByX implements Comparator<PieceOfTape>
    {
        @Override
        public int compare(PieceOfTape o1, PieceOfTape o2) {
            return (int) (o1.getX() - o2.getX());
        }
    }


}
