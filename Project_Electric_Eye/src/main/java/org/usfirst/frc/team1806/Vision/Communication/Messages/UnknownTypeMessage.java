package org.usfirst.frc.team1806.Vision.Communication.Messages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class UnknownTypeMessage extends VisionMessage {

    String jsonMessage;
    boolean isValid;
    String type;

    public UnknownTypeMessage(String jsonMessage){
        isValid = true;
        this.jsonMessage = jsonMessage;

        JsonElement messageElement;
        JsonParser messageParser = new JsonParser();
        try {
            messageElement = messageParser.parse(jsonMessage);
            JsonObject messageObject = messageElement.getAsJsonObject();

            type = messageObject.get("type").getAsJsonPrimitive().getAsString();
        }
        catch(JsonParseException parseException){
            System.out.println("received invalid message");
            this.isValid = false;
        }
    }

    public String getType(){
        return type;
    }

    public String getMessage(){
        return jsonMessage;
    }

    public boolean isValid(){
        return isValid;
    }
}
