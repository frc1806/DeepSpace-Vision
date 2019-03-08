package VisionPipeline;

import ElectricEye.Main;
import Util.Target;
import VisionPipeline.Filter.ReflectiveTapeFilter;
import VisionPipeline.TargetExtractor.ReflectiveTapeTargetExtractor;
import edu.wpi.first.vision.VisionPipeline;
import org.opencv.core.Mat;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ReflectiveTapePipeline implements VisionPipeline{

    private ArrayList<Target> targets;

    @Override
    public void process(Mat mat) {
        Main.imageTimestamp = Main.getCurrentEstimatedTimestamp();
        ReflectiveTapeFilter tapeFilter = new ReflectiveTapeFilter();
        ReflectiveTapeTargetExtractor targetExtractor = new ReflectiveTapeTargetExtractor();
        tapeFilter.process(mat);
        targets = targetExtractor.processTargetInformation(tapeFilter.getOutput());
        Main.targetTimestamp = Main.imageTimestamp;
    }

    public ArrayList<Target> getTargets(){
        return targets;
    }
}