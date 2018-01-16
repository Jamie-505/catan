package de.lmu.settleBattle.catanServer;

import static de.lmu.settleBattle.catanServer.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CatanSocketHandler extends TextWebSocketHandler implements PropertyChangeListener {

    private SocketUtils utils;

    // set to store all live sessions
    private Set<WebSocketSession> sessions;

    public CatanSocketHandler() {
        super();

        initialize();
    }

    private void initialize() {
        utils = new SocketUtils();

        utils.getGameCtrl().getBoard().addPropertyChangeListener(e -> {
                    if (e.getPropertyName().equals("new building")) {
                        System.out.printf("A new building has been built: %s %n",
                                ((Building) e.getNewValue()).toJSONString());
                        sendMessageToAll(CatanMessage.newBuilding((Building) e.getNewValue()));
                    }
                }
        );

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

        Player player = new Player(SocketUtils.toInt(session.getId()));

        try {
            utils.getGameCtrl().addPlayer(player);

            //add change listener for status property so every time the status changes a message will be sent
            player.addPropertyChangeListener(this);

            sessions.add(session);

            // sending session id to the client that just connected
            TextMessage verifyProtocol = CatanMessage.serverProtocol();
            sendSynchronizedMessage(session, verifyProtocol);
        } catch (IllegalAccessException e) {
            sendError(session, "Cannot join this game");
            e.printStackTrace();
        }
    }
    //endregion

    //region handleTextMessage
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

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
                    try {
                        utils.assignPlayerData(session, message);
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                        errorMessage = ex.getMessage();
                        OK = false;
                    }
                    break;

                case START_GAME:
                    utils.handleStartGameMessage(session, message);
                    if (utils.getGameCtrl().readyToStartGame()) {
                        sendMessageToAll(CatanMessage.startGame(utils.getGameCtrl().getBoard()));
                        getGameCtrl().startGame();
                    }
                    break;

                case BUILD:
                    try {
                        OK = utils.build(SocketUtils.toInt(session.getId()), message);
                        if (!OK) errorMessage = "Das Gebäude kann nicht gebaut werden.";
                    } catch (IllegalAccessException ex) {
                        errorMessage = ex.getMessage();
                        OK = false;
                    }
                    break;

                case ROLL_DICE:
                    OK = getGameCtrl().dice(SocketUtils.toInt(session.getId()));
                    if (!OK) errorMessage = "Du bist nicht am Zug";
                    break;

                case CARD_MONOPOLY:
                    sendMessageToAll(CatanMessage.monopoleCard(SocketUtils.toInt(session.getId()), message));
                    OK = utils.applyMonopolyCard(session, message);
                    if (!OK) errorMessage = "Die Monopolkarte konnte nicht ausgespielt werden.";
                    break;

                case CARD_INVENTION:
                    sendMessageToAll(CatanMessage.inventionCard(SocketUtils.toInt(session.getId()), message));
                    OK = utils.applyInventionCard(session, message);
                    if (!OK) errorMessage = "Die Karte Erfindung kann nicht ausgespielt werden.";
                    break;

                case CARD_KNIGHT:
                    sendMessageToAll(CatanMessage.knightCard(SocketUtils.toInt(session.getId()), message));
                    OK = utils.applyKnightCard(session, message);
                    if (!OK) errorMessage = "Die Ritterkarte konnte nicht ausgespielt werden.";
                    break;

                case CARD_RD_CON:
                    sendMessageToAll(CatanMessage.roadConstructionCard(SocketUtils.toInt(session.getId()), message));
                    OK = utils.applyRoadConstructionCard(session, message);
                    if (!OK) errorMessage = "Die Straßenkonstruktionskarte konnte nicht ausgespielt werden.";
                    break;

                case ROBBER_TO:
                    OK = utils.moveRobber(session, message);
                    if (!OK) errorMessage = "Der Räuber kann nicht versetzt werden.";
                    break;

                case TOSS_CARDS:
                    OK = utils.tossRawMaterials(session, message);
                    if (!OK) errorMessage = "Die Rohstoffe konnten nicht reduziert werden.";
                    break;

                case CARD_BUY:
                    OK = utils.buyDevCard(session);
                    if (!OK) errorMessage = "Es konnte keine Entwicklungskarte gekauft werden.";
                    break;

                case SEA_TRADE:
                    OK = utils.seatrade(session, message);
                    if (!OK) errorMessage = "Der Seehandel kann nicht durchgeführt werden";
                    break;

                case TRD_REQ:
                    TradeRequest tradeRequest = utils.tradeOffer(session, message);
                    OK = addTradeRequest(tradeRequest);
                    if (!OK) errorMessage = "Das Handelsangebot ist ungültig.";
                    break;

                case TRD_RES:
                    OK = utils.tradeAccepted(session, message);
                    if (!OK) errorMessage =
                            "Der Spieler kann das Handelsangebot nicht annehmen, da er entweder " +
                                    "die geforderten Rohstoffe nicht besitzt oder die Handels-ID ungültig ist.";
                    break;

                case TRD_SEL:
                    OK = utils.tradeConduction(session, message);
                    if (!OK)
                        errorMessage = "Der Handel konnte nicht durchgeführt werden. Haben beide Parteien genügend Rohstoffe?";
                    break;

                case TRD_REJ:
                    OK = utils.tradeCancelled(message);
                    if (!OK) errorMessage = "Die Handels-ID exitsiert nicht.";
                    break;

                case END_TURN:
                    nextMove();
                    break;

                case ADD_KI:
                    try {
                        Player newPlayer = getGameCtrl().addKI();
                        newPlayer.addPropertyChangeListener(this);
                        newPlayer.setStatus(WAIT_FOR_GAME_START);
                        sendOK(session);

                        if (utils.getGameCtrl().readyToStartGame()) {
                            sendMessageToAll(CatanMessage.startGame(utils.getGameCtrl().getBoard()));
                            getGameCtrl().startGame();
                        }

                    } catch (Exception ex) {
                        OK = false;
                        errorMessage = "Es konnte keine KI hinzugefügt werden.";
                    }
                    break;
            }

            if (OK) sendOK(session);
            else if (!errorMessage.equals(""))
                sendError(session, errorMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextMove() throws IOException {
        Player current = utils.getGameCtrl().getCurrent();

        //current player has won the game
        if (current.hasWon()) {
            utils.getGameCtrl().endGame(current);
            sendMessageToAll(CatanMessage.endGame(current));
        } else {
            utils.getGameCtrl().nextMove();
        }
    }
    //endregion

    //region afterConnectionClosed
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        System.out.println("Session " + session.getId() + " has ended");

        // remove the session from sessions list
        sessions.remove(session);
        sendMessageToAll(CatanMessage.playerLeft(session.getId()));

        if (!utils.getGameCtrl().isGameOver()) {

            //replace player by KI
            if (sessions.size() > 0) {
                Player player = getGameCtrl().getPlayer(session.getId());
                getGameCtrl().setKI(player);
            }

            //no real player left --> reinitialize game
            else {
                initialize();
            }
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
    private void sendStatusUpdateToMe(Player player) {
        sendSynchronizedMessage(getSession(Integer.toHexString(player.getId())),
                CatanMessage.statusUpdate(player));
    }

    private void sendStatusUpdate(Player player) throws IOException {
        for (WebSocketSession session : sessions) {
            TextMessage message;
            if (SocketUtils.toInt(session.getId()) == player.getId()) {
                message = CatanMessage.statusUpdate(player);
            } else message = CatanMessage.statusUpdatePublic(player);

            sendSynchronizedMessage(session, message);
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
            sendSynchronizedMessage(s, message);
        }
    }

    /**
     * sends normal message to current player and hidden message to everyone else
     *
     * @param sessionId      id of session
     * @param privateMessage message sent to himself
     * @param publicMessage  message sent to everybody else
     */
    public void sendMessageToAll(String sessionId, TextMessage privateMessage, TextMessage publicMessage) {
        // Looping through all the sessions and sending the message individually
        for (WebSocketSession s : sessions) {
            TextMessage message = s.getId().equals(sessionId) ? privateMessage : publicMessage;
            sendSynchronizedMessage(s, message);
        }
    }
    //endregion

    private void sendFellowPlayers(WebSocketSession session) throws IOException {
        for (Player player : utils.getGameCtrl().getPlayers())
            if (player.getId() != SocketUtils.toInt(session.getId()))
                sendSynchronizedMessage(session, CatanMessage.statusUpdate(player));
    }

    //region sendOK
    private void sendOK(WebSocketSession session) {
        TextMessage message = CatanMessage.OK();
        sendSynchronizedMessage(session, message);
    }
    //endregion

    /**
     * send message synchronized
     *
     * @param session web socket session
     * @param message message to be sent
     */
    private void sendSynchronizedMessage(WebSocketSession session, TextMessage message) {

        try {
            synchronized (session) {
                System.out.printf("Send Message to %s: %s", SocketUtils.toInt(session.getId()), message.getPayload());
                session.sendMessage(message);
            }

        } catch (IOException ex) {
            System.out.printf("Send error to: %s %s ", session.getId(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    //region sendError
    private void sendError(WebSocketSession session, String errorDescription) {
        sendSynchronizedMessage(session, CatanMessage.error(errorDescription));
    }
    //endregion

    //region trading
    public boolean addTradeRequest(TradeRequest tradeRequest) {


        //at least one raw material must be requested/offered
        if (tradeRequest.getOffer().getTotalCount() < 1 || tradeRequest.getRequest().getTotalCount() < 1)
            return false;

        //has player offered raw materials ?
        Player player = utils.getGameCtrl().getPlayer(tradeRequest.getPlayerId());
        if (!player.canAfford(tradeRequest.getOffer())) return false;

        tradeRequest.addPropertyChangeListener(evt -> {
            TradeRequest trChanged = (TradeRequest) evt.getNewValue();
            switch (evt.getPropertyName()) {
                case TDR_REQ_ACC:
                    Integer id = (Integer) evt.getOldValue();
                    if (id != null) {
                        sendMessageToAll(CatanMessage.tradeAccept(trChanged, id));
                    }
                    break;

                case TDR_REQ_CANCEL:
                    sendMessageToAll(CatanMessage.trade(trChanged, TRD_ABORTED));
                    break;

                case TDR_REQ_EXE:
                    sendMessageToAll(CatanMessage.trade(trChanged, TRD_FIN));
                    break;
            }
        });

        utils.getTradeRequests().add(tradeRequest);
        sendMessageToAll(CatanMessage.trade(tradeRequest, TRD_OFFER));

        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            Player player = (Player) evt.getNewValue();
            boolean sendStatusUpdate = true;

            System.out.printf("Property '%s': '%s' -> '%s'%n",
                    evt.getPropertyName(), evt.getOldValue(), player.getStatus());

            switch (evt.getPropertyName()) {

                case SU_ONLY_TO_ME:
                    sendStatusUpdateToMe(player);
                    sendStatusUpdate = false;
                    break;
                case TRD_DECR:
                    RawMaterialOverview costs = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(CatanMessage.costs(getGameCtrl().getCurrent().getId(), costs, false));
                    break;
                case TRD_INCR:
                    RawMaterialOverview harvest = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(CatanMessage.harvest(getGameCtrl().getCurrent().getId(), harvest, false));
                    break;

                //if there was a decrease/increase of raw materials send harvest/cost messages
                case RMO_INCR:
                    RawMaterialOverview harvest2 = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(Integer.toHexString(player.getId()),   //parse id back to hex string (session id)
                            CatanMessage.harvest(player.getId(), harvest2, false),
                            CatanMessage.harvest(player.getId(), harvest2, true));
                    break;

                case RMO_DECR:
                    RawMaterialOverview costs2 = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(Integer.toHexString(player.getId()),   //parse id back to hex string (session id)
                            CatanMessage.costs(player.getId(), costs2, false),
                            CatanMessage.costs(player.getId(), costs2, true));
                    break;

                case DICE:
                    sendMessageToAll(CatanMessage.throwDice(player.getId(), (int[]) evt.getOldValue()));
                    sendStatusUpdate = false;
                    break;

                case VP_INCR:
                    sendStatusUpdate = true;
                    break;

                case END_TURN:
                    //TODO
                    break;
            }

            if (sendStatusUpdate) sendStatusUpdate(player);
        } catch (IOException ex) {
            System.out.printf("An exception occured %s", ex.getMessage());
        }

    }

    public void endGame(WebSocketSession session) {
        Player winner = utils.getGameCtrl().getPlayer(session.getId());
        if (winner.hasWon() && winner.getId() == this.utils.getGameCtrl().getPlayerWithHighestPoints().getId()) {
            sendMessageToAll(CatanMessage.endGame(winner));
            this.utils.endGame(session);
        }
    }


    //endregion

}