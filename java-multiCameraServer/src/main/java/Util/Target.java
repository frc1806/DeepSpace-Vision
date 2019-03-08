package Util;

import com.google.gson.JsonObject;
import VisionPipeline.TargetExtractor.ReflectiveTapeTarget.PieceOfTape;

public class Target {
    private double distance;
    private double robotToTarget;
    private double targetHeadingOffset; // Always 0 if target doesn't have a heading i.e. Cargo

    public PieceOfTape getLeftTarget() {
        return leftTarget;
    }

    public PieceOfTape getRightTarget() {
        return rightTarget;
    }

    private PieceOfTape leftTarget, rightTarget;
    public Target(PieceOfTape leftTarget, PieceOfTape rightTarget){
        this.leftTarget = leftTarget;
        this.rightTarget = rightTarget;
        //System.out.println("AM i BeIng trashed?");
    }

    public JsonObject getTargetJson(){
        //TODO place into JSON
        return new JsonObject();
    }

    public int getMiddle() {
        return (int) ((leftTarget.getmTop().x + rightTarget.getmTop().x) / 2);
    }



    public RigidTransform2d getTargetPosition(RigidTransform2d robotPosition){
        double angleForConversion = robotPosition.getRotation().getDegrees() + robotToTarget;
        double targetX = distance * Math.cos(Math.toRadians(angleForConversion));
        double targetY = distance * Math.sin(Math.toRadians(angleForConversion));
        double targetHeading = robotPosition.getRotation().getDegrees() + robotToTarget + targetHeadingOffset ;
        return new RigidTransform2d(new Translation2d(targetX,targetY), new Rotation2d().fromDegrees(targetHeading));
    }

}
