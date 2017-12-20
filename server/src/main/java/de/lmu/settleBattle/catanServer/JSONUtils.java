package de.lmu.settleBattle.catanServer;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

public class JSONUtils {

    public static JSONObject setJSONType(String type, JSONObject json) {
        JSONObject ret = new JSONObject();
        ret.put(type, json);
        return ret;
    }

    public static JSONObject createJSON(TextMessage message) {
        return new JSONObject(message.getPayload());
    }

    //region getMessageType

    /**
     * determines the message type of the received message
     *
     * @param message
     * @return ClientMessageType or null
     */
    public static String getMessageType(TextMessage message) {
        JSONObject json = new JSONObject(message.getPayload());
        String type = json.keys().next();

        return type;
    }
    //endregion
}
