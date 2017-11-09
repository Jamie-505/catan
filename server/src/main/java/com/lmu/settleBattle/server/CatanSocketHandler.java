package com.lmu.settleBattle.server;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CatanSocketHandler extends TextWebSocketHandler {

  // set to store all live sessions
  private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<WebSocketSession>());

  // Mapping between session and client name
//  private static final HashMap<String, String> nameSessionPair = new HashMap<>();

  private JSONUtils jsonUtils = new JSONUtils();

//  // Getting query params
//  public static Map<String, String> getQueryMap(URI uri) {
//    String uriString = uri.toString();
//    Map<String, String> map = Maps.newHashMap();
//    if (uri != null) {
//      int queryStart = uriString.indexOf("?")+1;
//      String query = uriString.substring(queryStart);
//
//      String[] params = query.split("&");
//
//      for (String param : params) {
//        String[] nameval = param.split("=");
//        map.put(nameval[0], nameval[1]);
//      }
//    }
//    return map;
//  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    System.out.println(session.getId() + " has opened a connection");
    super.afterConnectionEstablished(session);

    // Add session to session list
    sessions.add(session);

    try {
      // sending session id to the client that just connected
      String verifyProtocol = jsonUtils.initialJSON();
      session.sendMessage(new TextMessage(verifyProtocol));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Notify all the clients about newly joined client
//    sendMessageToAll(session.getId(), name, " joined conversation!", true, false);
  }


  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message)
      throws InterruptedException, IOException {

    System.out.println("Message from " + session.getId() + ": " + message.getPayload());

    try {
      String idAllocation = jsonUtils.assignPlayerId(session.getId());
      session.sendMessage(new TextMessage(idAllocation));
    } catch (IOException e) {
      e.printStackTrace();
    }

//    String msg = null;
//
//    // Parsing the json and getting message
//    try {
//      JSONObject jObj = new JSONObject(message.getPayload());
//      msg = jObj.getString("message");
//    } catch (JSONException e) {
//      e.printStackTrace();
//    }

    // Sending the message to all clients
//    sendMessageToAll(session.getId(), nameSessionPair.get(session.getId()),
//        msg, false, false);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    super.afterConnectionClosed(session, status);
    System.out.println("Session " + session.getId() + " has ended");

    // Getting the client name that exited

    // removing the session from sessions list
    sessions.remove(session);

    // Notifying all the clients about person exit
//    sendMessageToAll(session.getId(), name, " left conversation!", false,true);
  }

  /**
   * Method to send message to all connected clients
   * @param sessionId session id of client
   * @param name name of client
   * @param message message to be sent to clients
   * @param isNewClient flag to identify that message is about new join
   * @param isExit flag to identify that a client left the conversation
   * */

//  private void sendMessageToAll(String sessionId, String name, String message,
//      boolean isNewClient, boolean isExit) {
//
//    // Looping through all the sessions and sending the message individually
//    for (WebSocketSession s : sessions) {
//      String json = null;
//
//      // Checking if the message is about new client joined
//      if (isNewClient) {
//        json = jsonUtils.getNewClientJson(sessionId, name, message,
//            sessions.size());
//
//      } else if (isExit) {
//        // Checking if the person left the conversation
//        json = jsonUtils.getClientExitJson(sessionId, name, message,
//            sessions.size());
//      } else {
//        // Normal chat conversation message
//        json = jsonUtils
//            .getSendAllMessageJson(sessionId, name, message);
//      }
//
//      try {
//        System.out.println("Sending Message To: " + sessionId + ", "
//            + json);
//
//        s.sendMessage(new TextMessage(json));
//      } catch (IOException e) {
//        System.out.println("error in sending. " + s.getId() + ", "
//            + e.getMessage());
//        e.printStackTrace();
//      }
//    }
//  }

}
