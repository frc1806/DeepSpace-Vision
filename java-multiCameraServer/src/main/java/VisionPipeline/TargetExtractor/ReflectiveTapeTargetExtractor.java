package VisionPipeline.TargetExtractor;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import Util.CameraCalculationInformation;
import Util.RigidTransform2d;
import Util.Target;
import VisionPipeline.TargetExtractor.ReflectiveTapeTarget.PieceOfTape;

import java.util.ArrayList;
import java.util.Comparator;

public class ReflectiveTapeTargetExtractor implements TargetExtractor {

    public ReflectiveTapeTargetExtractor(){

    }

    private ArrayList<PieceOfTape> tapeStrips = new ArrayList<>();
    private ArrayList<Target> bays = new ArrayList<>();


    @Override
    public ArrayList<Target> processTargetInformation(ArrayList<MatOfPoint> outputFromFilter){
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
            PieceOfTape proposedPieceOfTape = new PieceOfTape(left, top, right);
            if(proposedPieceOfTape.mTapeType != PieceOfTape.TapeType.UNKNOWN){
                tapeStrips.add(proposedPieceOfTape);
            }
        }
        tapeStrips.sort(new TapeSortByX());

        for(PieceOfTape strip:tapeStrips) {
            /*System.out.println("inner (" + strip.getmInner().x + ", " + strip.getmInner().y + ")");
            System.out.println("outer (" + strip.getmOuter().x + ", " + strip.getmOuter().y + ")");
            System.out.println("top (" + strip.getmTop().x + ", " + strip.getmTop().y + ")");*/
        }
        int i = 0;
        while( i < tapeStrips.size()) {
            //System.out.println("SSS " + tapeStrips.get(i).mTapeType);
            if(tapeStrips.get(i).mTapeType == PieceOfTape.TapeType.LEFT && tapeStrips.size() > i + 1) {
                bays.add(new Target(tapeStrips.get(i), tapeStrips.get(i + 1)));
                i+=2;
            }
            else {
                i++;
            }
        }

        /*
        System.out.println("tapeStrips: " + tapeStrips.size());
        System.out.println("bays: " + bays.size());
        */
        bays.sort(new TargetSortByDistance());
        if(!bays.isEmpty()){
            System.out.println(bays.get(0).toString());
        }


        return bays;
    }

    class TapeSortByX implements Comparator<PieceOfTape>
    {
        @Override
        public int compare(PieceOfTape o1, PieceOfTape o2) {
            return (int) (o1.getmTop().x - o2.getmTop().x);
        }
    }class BaySortByX implements Comparator<Target>
    {
        @Override
        public int compare(Target o1, Target o2) {
            return o1.getMiddle() - o2.getMiddle();
        }
    }

    class TargetSortByDistance implements Comparator<Target>{
        @Override
        public int compare(Target o1, Target o2) {return Double.compare(o1.getDistance(), o2.getDistance());}
    }


}
