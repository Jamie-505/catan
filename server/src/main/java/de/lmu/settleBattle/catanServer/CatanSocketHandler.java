package de.lmu.settleBattle.catanServer;
import java.io.IOException;
import java.util.*;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.yaml.snakeyaml.scanner.Constant;

@Component
public class CatanSocketHandler extends TextWebSocketHandler {

    private GameController gameCtrl = new GameController();

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
        Player player = new Player(Integer.parseInt(session.getId()));
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
                    String idAllocation = jsonUtils.welcomeNewPlayer(Integer.parseInt(session.getId()));
                    session.sendMessage(new TextMessage(idAllocation));

                    //update status of new player
                    Player player = gameCtrl.getPlayer(session.getId());
                    player.setStatus(Constants.START_GAME);

                    sendMessageToAll(jsonUtils.statusUpdate(player));
                    break;
                case Constants.PLAYER:
                    JSONObject json = new JSONObject(message);
                    boolean validData = true;
                    if (json.has(Constants.PLAYER_NAME) && json.has(Constants.PLAYER_COLOR) && json.length() == 2) {
                        for (Player player1 : gameCtrl.getPlayers()) {
                            //if name or color are already taken --> this choice is invalid for new player
                            if (player1.getName().equals(json.get(Constants.PLAYER_NAME)) ||
                                    player1.getColor().equals(json.get(Constants.PLAYER_COLOR)))
                                validData = false;
                        }
                    }

                    if (validData) {
                        Player player1 = gameCtrl.getPlayer(session.getId());
                        player1.setName(json.get(Constants.PLAYER_NAME).toString());
                        player1.setColor((Color)json.get(Constants.PLAYER_COLOR));

                        sendMessageToAll(jsonUtils.statusUpdate(player1));
                    }
                    else {
                        session.sendMessage(new TextMessage(jsonUtils.errorMessage(Constants.PLAYER_DATA_INVALID)));
                    }

                    break;
                case Constants.DICE_RESULT:
                    int[] dice = Player.throwDice();
                    String diceMessage = jsonUtils.throwDice(
                            gameCtrl.getPlayer(session.getId()).getId(), dice);

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
        Player player = gameCtrl.getPlayer(session.getId());
        gameCtrl.removePlayer(player);

        // remove the session from sessions list
        sessions.remove(session);

        // TODO: Replace player with KI !
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