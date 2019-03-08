package VisionPipeline.TargetExtractor;

import org.opencv.core.MatOfPoint;
import Util.Target;

import java.util.ArrayList;

public class CargoTargetExtractor implements TargetExtractor {

    public CargoTargetExtractor(){};

    @Override
    public ArrayList<Target> processTargetInformation(ArrayList<MatOfPoint> outputFromFilter) {
        return new ArrayList<>();
    }
}
