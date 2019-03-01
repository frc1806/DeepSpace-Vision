package org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor.ReflectiveTapeTarget;

import org.opencv.core.Point;

public class PieceOfTape {

    public enum TapeType{
        UNKNOWN,
        LEFT,
        RIGHT
    }

    Point mOuter = new Point(0,0);
    Point mTop = new Point(0,0);
    Point mInner = new Point(0,0);
    
    private double leftLineDistance;
    private double rightLineDistance;

    public TapeType mTapeType = TapeType.UNKNOWN;

    public PieceOfTape(Point left, Point top, Point right){
     leftLineDistance = Math.sqrt(Math.pow(top.x-left.x, 2)+ Math.pow(top.y - left.y, 2));
     if(left.y < right.y) {
         mTapeType = TapeType.LEFT;
         System.out.println("POT left");
         mOuter = left;
         mTop = top;
         mInner = right;
     }
     else if (left.y > right.y){
         mTapeType = TapeType.RIGHT;
         System.out.println("POT right");

         mOuter = right;
         mTop = top;
         mInner = left;
     }
     else {
         mTapeType = TapeType.UNKNOWN;
         System.out.println("POT unknown");
     }
    }


    public double getX() {
        return mOuter.x;
    }

}
