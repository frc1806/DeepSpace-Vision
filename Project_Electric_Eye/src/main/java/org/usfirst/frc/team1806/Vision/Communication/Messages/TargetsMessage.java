package org.usfirst.frc.team1806.Vision.Communication.Messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.usfirst.frc.team1806.Vision.Util.Target;

import java.util.ArrayList;

public class TargetsMessage extends VisionMessage {

    ArrayList<Target> targets;
    double timestamp;

    public TargetsMessage(ArrayList<Target> targets, double timestamp){

        this.targets = targets;
        this.timestamp = timestamp;
    }

    public String getType(){
        return "targets";
    }

    public String getMessage(){
        JsonObject message = new JsonObject();
        message.add("timestamp", new JsonPrimitive(timestamp));
        JsonArray targetsArray = new JsonArray();
        for(Target target:targets){
            targetsArray.add(target.getTargetJson());
        }
        message.add("targets",targetsArray);
        return message.getAsString();
    }

}
