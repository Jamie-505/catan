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


//
//
//  /**
//   * Json to notify all the clients about new person joined
//   * */
//  public String getNewClientJson(String sessionId, String name, String message, int onlineCount) {
//    String json = null;
//
//    try {
//      JSONObject jObj = createJSON(FLAG_NEW, sessionId, message);
//      jObj.put("name", name);
//      jObj.put("onlineCount", onlineCount);
//      json = jObj.toString();
//    } catch (JSONException e) {
//      e.printStackTrace();
//    }
//
//    return json;
//  }
//
//  /**
//   * Json when the client exits the socket connection
//   * */
//  public String getClientExitJson(String sessionId, String name, String message, int onlineCount) {
//    String json = null;
//
//    try {
//      JSONObject jObj = createJSON(FLAG_EXIT, sessionId, message);
//      jObj.put("name", name);
//      jObj.put("onlineCount", onlineCount);
//      json = jObj.toString();
//    } catch (JSONException e) {
//      e.printStackTrace();
//    }
//
//    return json;
//  }
//
//  /**
//   * JSON when message needs to be sent to all the clients
//   * */
//  public String getSendAllMessageJson(String sessionId, String fromName, String message) {
//    String json = null;
//
//    try {
//      JSONObject jObj = createJSON(FLAG_MESSAGE, sessionId, message);
//      jObj.put("name", fromName);
//
//      json = jObj.toString();
//    } catch (JSONException e) {
//      e.printStackTrace();
//    }
//
//    return json;
//  }
}
