package org.usfirst.frc.team1806.Vision.VisionPipeline.TargetExtractor.Common;

import org.usfirst.frc.team1806.Vision.Util.CameraCalculationInformation;

public class CameraMath {

    /**
     * Gets an angle to an object at a horizontal pixel
     * @param pixel horizontal pixel that an angle is desired for.
     * @param cameraInfo the informaiton about the camera that allows for more accurate angle calculations.
     * @return the angle to the pixel.
     */
    public static double getHorizontalAngleToPixel(int pixel, CameraCalculationInformation cameraInfo){
        return Math.toDegrees(Math.atan((cameraInfo.getHorizontalResolution()-pixel)/cameraInfo.getFocalLength()));
    }
}
