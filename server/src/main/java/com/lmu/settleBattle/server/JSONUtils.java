package com.lmu.settleBattle.server;


import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

  // flags to identify the kind of json response on client side
  private static final String HALLO = "Hallo",
                              WILLKOMMEN = "Willkommen",
                              STATUSUPDATE = "Statusupdate",
                              SPIELER = "Spieler";



  public JSONUtils() {
  }

  // creates JSON Strings in protocol conform matter
  private JSONObject createJSON(String type, HashMap information) {
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

  public String initialJSON(){
    HashMap<String, String> payload = new HashMap<>();
    payload.put("Version", "0.1");
    payload.put("Protokoll", "1.0");
    return createJSON(HALLO, payload).toString();
  }

  /**
   * Json when client needs it's own session details
   * */
  public String assignPlayerId(String id) {
    HashMap<String, String> payload = new HashMap<>();
    payload.put("id", id);
    return createJSON(WILLKOMMEN, payload).toString();
  }
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
