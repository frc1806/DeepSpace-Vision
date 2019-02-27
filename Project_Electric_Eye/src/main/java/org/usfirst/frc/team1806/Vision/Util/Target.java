package org.usfirst.frc.team1806.Vision.Util;

import com.google.gson.JsonObject;
import org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor.ReflectiveTapeTarget.PieceOfTape;

public class Target {
    private double distance;
    private double robotToTarget;
    private double targetHeadingOffset; // Always 0 if target doesn't have a heading i.e. Cargo

    public Target(PieceOfTape leftTarget, PieceOfTape rightTarget){
        
    }

    public JsonObject getTargetJson(){
        //TODO place into JSON
        return new JsonObject();
    }



    public RigidTransform2d getTargetPosition(RigidTransform2d robotPosition){
        double angleForConversion = robotPosition.getRotation().getDegrees() + 90 + robotToTarget;
        double targetX = distance * Math.cos(angleForConversion);
        double targetY = distance * Math.sin(angleForConversion);
        double targetHeading = robotPosition.getRotation().getDegrees() + robotToTarget + targetHeadingOffset ;
        return new RigidTransform2d(new Translation2d(targetX,targetY), new Rotation2d().fromDegrees(targetHeading));
    }

}