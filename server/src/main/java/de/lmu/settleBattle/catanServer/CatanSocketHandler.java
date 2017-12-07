package de.lmu.settleBattle.catanServer;
import java.io.IOException;
import java.util.*;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CatanSocketHandler extends TextWebSocketHandler {

    private GameController gameCtrl = new GameController();

    //for creating unique ids for new players. The sessionId will be stored in
    //the player objects so we know which player needs to be removed if a player
    //is disconnected e.g.
    private int id = 0;
    private int getUniqueId() { return id++; }

    // set to store all live sessions
    private static final Set<WebSocketSession> sessions =
            Collections.synchronizedSet(new HashSet<WebSocketSession>());

    private JSONUtils jsonUtils = new JSONUtils();

    // Mapping between session and client name
//  private static final HashMap<String, String> nameSessionPair = new HashMap<>();

    //region afterConnectionEstablished
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(session.getId() + " has opened a connection");
        super.afterConnectionEstablished(session);

        // Add session to session list
        sessions.add(session);
        Player player = new Player(session.getId(), getUniqueId());
        gameCtrl.addPlayer(player);

        try {
            // sending session id to the client that just connected
            String verifyProtocol = jsonUtils.initialJSON();
            session.sendMessage(new TextMessage(verifyProtocol));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region handleTextMessage
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {

        System.out.println("Message from " + session.getId() + ": " + message.getPayload());

        try {
            String type = jsonUtils.getMessageType(message);

            System.out.println(type.toString());

            switch (type) {
                case Constants.HANDSHAKE:
                    String idAllocation = jsonUtils.assignPlayerId(session.getId());
                    session.sendMessage(new TextMessage(idAllocation));

                    //TODO: sendMessageToAll(jsonUtils.sendStatusUpdate());
                    break;
                case Constants.DICE_RESULT:
                    int[] dice = Player.throwDice();
                    String diceMessage = jsonUtils.throwDice(session.getId(), dice);

                    for (WebSocketSession socketSession : sessions) {
                        socketSession.sendMessage(new TextMessage(diceMessage));
                    }
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //endregion

    //region afterConnectionClosed
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        System.out.println("Session " + session.getId() + " has ended");

        //remove player from GameController
        gameCtrl.removePlayer(gameCtrl.getPlayer(session.getId()));

        // remove the session from sessions list
        sessions.remove(session);

        // Notify all the clients about person exit
        //TODO: sendMessageToAll(session.getId(), name, " left conversation!", false,true);
    }
    //endregion

    //region sendMessageToAll
    /**
     * Method to send message to all connected clients
     *
     * @param message     message to be sent to clients
     */
    private void sendMessageToAll(String message) {

        // Looping through all the sessions and sending the message individually
        for (WebSocketSession s : sessions) {

            try {
                System.out.println("Sending Message To: " + s.getId() + ", "
                        + message);

                s.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.out.println("error in sending. " + s.getId() + ", "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    //endregion

}

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