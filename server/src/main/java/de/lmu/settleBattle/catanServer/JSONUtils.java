package de.lmu.settleBattle.catanServer;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

public class JSONUtils {

    public JSONUtils() { }

    //region createJSON
    /**
     * creates JSON Strings in protocol conform matter
     * @param type message type
     * @param information payload of message
     * @return JSONObject containing type and information
     */
    public JSONObject createJSON(String type, HashMap information) {
        try {
            JSONObject jObj = new JSONObject();
            JSONObject payload = new JSONObject(information);

            jObj.put(type, payload);
            return jObj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //endregion

    //region initialJSON
    /**
     * Creates message containing version and protocol
     * @return JSON structured String Object
     */
    public String initialJSON() {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("Version", "0.1");
        payload.put("Protokoll", "1.0");
        return createJSON(Constants.HANDSHAKE, payload).toString();
    }
    //endregion


    //region assignPlayerId
    /**
     * Json when client needs it's own session details
     * @param id
     * @return JSON structured String
     */
    public String assignPlayerId(String id) {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("id", id);
        return createJSON(Constants.GET_ID, payload).toString();
    }
    //endregion

    //region throwDices
    /**
     * creates message to send to all players when a player throws the dices
     * @param id
     * @return JSON structured String containing the dice result
     */
    public String throwDice(String id, int[] diceResult) {

        if (diceResult.length != 2)
            return null;

        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.PLAYER, id);
        map.put(Constants.DICE_THROW, diceResult);

        return createJSON(Constants.DICE_RESULT, map).toString();
    }
    //endregion

    //region getMessageType
    /**
     * determines the message type of the received message
     * @param message
     * @return ClientMessageType or null
     */
    public String getMessageType(TextMessage message) {
        JSONObject json = new JSONObject(message);
        String type = json.keys().next();

        return type;
    }
    //endregion

    /*


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
