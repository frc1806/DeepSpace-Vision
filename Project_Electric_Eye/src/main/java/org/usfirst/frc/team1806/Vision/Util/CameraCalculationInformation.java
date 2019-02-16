package org.usfirst.frc.team1806.Vision.Util;

public class CameraCalculationInformation {
    private int horizontalResolution;
    private int verticalResolution;
    private double focalLength;
    private double cameraFOV;

    /**
     *
     * @param horizontalResolution horizontal resolution of the camera in pixel
     * @param verticalResolution vertical resolution of the camera in pixels.
     * @param cameraFOV field of view of the camera in degrees.
     */
    public CameraCalculationInformation(int horizontalResolution, int verticalResolution, double cameraFOV) {
        this.horizontalResolution = horizontalResolution;
        this.verticalResolution = verticalResolution;
        this.cameraFOV = cameraFOV;
        this.focalLength = horizontalResolution / (2* Math.tan(Math.toRadians(cameraFOV)/2));
    }

    public int getVerticalResolution() {
        return verticalResolution;
    }

    public void setVerticalResolution(int verticalResolution) {
        this.verticalResolution = verticalResolution;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(double focalLength) {
        this.focalLength = focalLength;
    }

    public double getCameraFOV() {
        return cameraFOV;
    }

    public void setCameraFOV(double cameraFOV) {
        this.cameraFOV = cameraFOV;
    }

    public int getHorizontalResolution() {
        return horizontalResolution;
    }

    public void setHorizontalResolution(int horizontalResolution) {
        this.horizontalResolution = horizontalResolution;
    }


}
