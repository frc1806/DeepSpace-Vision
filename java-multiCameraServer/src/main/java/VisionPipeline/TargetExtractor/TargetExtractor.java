package VisionPipeline.TargetExtractor;

import org.opencv.core.MatOfPoint;
import Util.CameraCalculationInformation;
import Util.RigidTransform2d;
import Util.Target;
import Util.Translation2d;

import java.util.ArrayList;

public interface TargetExtractor {

    /**
     *
     * @param outputFromFilter
     * @return
     */
    ArrayList<Target> processTargetInformation(ArrayList<MatOfPoint> outputFromFilter);

}
