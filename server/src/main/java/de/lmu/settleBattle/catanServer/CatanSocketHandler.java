package de.lmu.settleBattle.catanServer;

import static de.lmu.settleBattle.catanServer.Constants.*;

import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CatanSocketHandler extends TextWebSocketHandler {

    private Gson gson;
    private SocketUtils utils;

    // set to store all live sessions
    private Set<WebSocketSession> sessions;

    public CatanSocketHandler() {
        super();

        utils = new SocketUtils();

        utils.getGameCtrl().getBoard().addPropertyChangeListener(e -> {
                    sendMessageToAll(CatanMessage.newBuilding((Building) e.getNewValue()));
                    System.out.printf("A new building has been built: %s %n",
                            ((Building) e.getNewValue()).toJSONString());
                }
        );

        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        this.gson = builder.create();
        this.sessions = Collections.synchronizedSet(new HashSet<WebSocketSession>());
    }

    public SocketUtils getUtils() {
        return utils;
    }

    public GameController getGameCtrl() {
        return getUtils().getGameCtrl();
    }

    //region afterConnectionEstablished
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(session.getId() + " has opened a connection");
        super.afterConnectionEstablished(session);

        // Add session to session list
        sessions.add(session);
        Player player = new Player(utils.toInt(session.getId()));

        //add change listener for status property so every time the status changes a message will be sent
        player.addPropertyChangeListener(e -> {
            try {
                Player p = (Player) e.getNewValue();
                sendStatusUpdate(p);
                System.out.printf("Property '%s': '%s' -> '%s'%n",
                        e.getPropertyName(), e.getOldValue(), p.getStatus());

                switch (e.getPropertyName()) {
                    //if there was a decrease/increase of raw materials send harvest/cost messages
                    case "RawMaterialIncrease":
                        RawMaterialOverview harvest = (RawMaterialOverview) e.getOldValue();
                        sendMessageToAll(Integer.toHexString(player.getId()),   //parse id back to hex string (session id)
                                CatanMessage.harvest(player.getId(), harvest, false),
                                CatanMessage.harvest(player.getId(), harvest, true));
                        break;

                    case "RawMaterialDecrease":
                        RawMaterialOverview costs = (RawMaterialOverview) e.getOldValue();
                        sendMessageToAll(Integer.toHexString(player.getId()),   //parse id back to hex string (session id)
                                CatanMessage.costs(player.getId(), costs, false),
                                CatanMessage.costs(player.getId(), costs, true));
                        break;
                }

            } catch (IOException ex) {
                System.out.printf("An exception occured %s", ex.getMessage());
            }
        });

        try {
            utils.getGameCtrl().addPlayer(player);
            // sending session id to the client that just connected
            TextMessage verifyProtocol = CatanMessage.serverProtocol();
            System.out.printf("Send Message to %s: %s", player.getId(), verifyProtocol.toString());
            session.sendMessage(verifyProtocol);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            session.sendMessage(new TextMessage("Cannot join this game"));
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
            String type = JSONUtils.getMessageType(message);
            boolean OK = false;
            String errorMessage = "";

            switch (type) {
                case HANDSHAKE:
                    if (utils.performHandshake(session))
                        sendFellowPlayers(session);
                    break;

                case PLAYER:
                    utils.assignPlayerData(session, message);
                    break;

                case START_GAME:
                    if (utils.handleStartGameMessage(session, message)) {
                        sendMessageToAll(CatanMessage.startGame(utils.getGameCtrl().getBoard()));
                        utils.startGame();
                    }
                    break;

                case BUILD:
                    OK = utils.build(session, message);
                    if (!OK) errorMessage = "Das Gebäude kann nicht gebaut werden.";
                    break;

                case DICE_RESULT:
                    this.dice(session);
                    break;

                case TOSS_CARDS:
                    OK = utils.tossRawMaterials(session, message);
                    if (!OK) errorMessage = "Die Rohstoffe konnten nicht reduziert werden.";
                    break;

                case END_TURN:
                    Player next = utils.getGameCtrl().getNext();
                    utils.nextMove(next.getId(), DICE);
                    break;
            }

            if (OK) sendOK(session);
            else if (!errorMessage.equals(""))
                sendError(session, errorMessage);

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
        Player player = utils.getGameCtrl().getPlayer(session.getId());
        utils.getGameCtrl().removePlayer(player);

        // remove the session from sessions list
        sessions.remove(session);

        // TODO: Replace player with KI !
    }
    //endregion

    //region dice
    public void dice(WebSocketSession session) throws IOException {
        int[] dice = Player.throwDice();
        TextMessage diceMessage = CatanMessage.throwDice(
                utils.getGameCtrl().getPlayer(session.getId()).getId(), dice);

        sendMessageToAll(diceMessage);

        int sum = dice[0] + dice[1];

        //the robber is activated
        if (sum == 7) utils.extractCardsDueToRobber();

            //distribute raw materials
        else {
            Map<Integer, RawMaterialOverview> distribution =
                    utils.getGameCtrl().mapOwnerWithHarvest(sum);

            utils.getGameCtrl().distributeRawMaterial(distribution);
        }
    }
    //endregion

    //region getSession
    public WebSocketSession getSession(String sessionId) {
        for (WebSocketSession session : sessions) {
            if (session.getId().equals(sessionId))
                return session;
        }
        return null;
    }
    //endregion

    //region sendStatusUpdate
    private void sendStatusUpdate() throws IOException {

        //for every player send status update to everybody
        for (Player player : utils.getGameCtrl().getPlayers()) {
            for (WebSocketSession session : sessions) {
                TextMessage message;

                if (utils.toInt(session.getId()) == player.getId()) {
                    message = CatanMessage.statusUpdate(player);
                } else message = CatanMessage.statusUpdatePublic(player);

                System.out.printf("Send Message to %s: %s", utils.toInt(session.getId()), message.toString());
                session.sendMessage(message);
            }
        }
    }

    private void sendStatusUpdate(Player player) throws IOException {
        for (WebSocketSession session : sessions) {
            TextMessage message;
            if (utils.toInt(session.getId()) == player.getId()) {
                message = CatanMessage.statusUpdate(player);
            } else message = CatanMessage.statusUpdatePublic(player);

            System.out.printf("Send Message to %s: %s", utils.toInt(session.getId()), message.toString());
            session.sendMessage(message);
        }
    }
    //endregion

    //region sendMessageToAll

    /**
     * Method to send message to all connected clients
     *
     * @param message message to be sent to clients
     */
    public void sendMessageToAll(TextMessage message) {

        // Looping through all the sessions and sending the message individually
        for (WebSocketSession s : sessions) {

            try {
                System.out.println("Sending Message To: " + s.getId() + ", "
                        + message);

                s.sendMessage(message);
            } catch (IOException e) {
                System.out.println("error in sending. " + s.getId() + ", "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * sends normal message to current player and hidden message to everyone else
     *
     * @param sessionId
     * @param privateMessage
     * @param publicMessage
     */
    public void sendMessageToAll(String sessionId, TextMessage privateMessage, TextMessage publicMessage) {
        // Looping through all the sessions and sending the message individually
        for (WebSocketSession s : sessions) {

            try {
                TextMessage sentMessage = s.getId().equals(sessionId) ? privateMessage : publicMessage;

                System.out.println("Sending Message To: " + s.getId() + ", "
                        + sentMessage);
                s.sendMessage(sentMessage);

            } catch (IOException e) {
                System.out.println("error in sending. " + s.getId() + ", "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    //endregion

    private void sendFellowPlayers(WebSocketSession session) throws IOException {
        for (Player player : utils.getGameCtrl().getPlayers())
            if (player.getId() != utils.toInt(session.getId()))
                session.sendMessage(CatanMessage.statusUpdate(player));
    }

    //region sendOK
    private void sendOK(WebSocketSession session) {
        try {
            TextMessage message = CatanMessage.OK();
            session.sendMessage(message);
            System.out.printf("Send Message to %s: %s", utils.toInt(session.getId()), message.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region sendError
    private void sendError(WebSocketSession session, String errorDescription) {
        try {
            session.sendMessage(CatanMessage.error(errorDescription));
            System.out.printf("Send Message to %s: %s", utils.toInt(session.getId()), errorDescription);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region sendCostMessages
    public void sendCostMessages(WebSocketSession session, TextMessage message) {
        Building building = gson.fromJson(JSONUtils.createJSON(message)
                .getJSONObject(Constants.BUILD).toString(), Building.class);

        sendMessageToAll(session.getId(),
                CatanMessage.costs(utils.toInt(session.getId()), building.getCost(), false),
                CatanMessage.costs(utils.toInt(session.getId()), building.getCost(), true));
    }
    //endregion

}