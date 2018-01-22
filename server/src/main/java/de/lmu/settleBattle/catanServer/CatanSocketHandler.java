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

        utils.getGameCtrl().addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals(ROBBER_AT)) {
                System.out.println("Robber was moved");
                sendMessageToAll((TextMessage) e.getNewValue());
            }
        });

        this.sessions = Collections.synchronizedSet(new HashSet<WebSocketSession>());
    }

    public SocketUtils getUtils() {
        return utils;
    }

    public GameController getGameCtrl() {
        return utils.getGameCtrl();
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
        } catch (CatanException e) {
            if (e.sendToClient()) sendError(session, e.getMessage());
            e.printStackTrace();
        }
    }
    //endregion

    //region handleTextMessage
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

        System.out.println("Message from " + session.getId() + ": " + message.getPayload());
        boolean OK = true;
        String errorMessage = "";
        Object errorObject = null;

        try {
            String type = JSONUtils.getMessageType(message);

            switch (type) {

                case CHAT_OUT:
                    sendMessageToAll(CatanMessage.chatMessage(
                        SocketUtils.toInt(session.getId()), message)
                    );
                    break;

                case HANDSHAKE:
                    if (utils.performHandshake(session)) sendOpponents(session);
                    break;

                case PLAYER:
                    utils.assignPlayerData(session, message);
                    break;

                case START_GAME:
                    utils.handleStartGameMessage(session, message);
                    if (utils.getGameCtrl().readyToStartGame()) {
                        sendMessageToAll(CatanMessage.startGame(utils.getGameCtrl().getBoard()));
                        getGameCtrl().startGame();
                    }
                    break;

                case BUILD:
                    OK = utils.build(SocketUtils.toInt(session.getId()), message);
                    if (!OK) errorMessage = "Das Gebäude kann nicht gebaut werden.";
                    break;

                case ROLL_DICE:
                    getGameCtrl().dice(SocketUtils.toInt(session.getId()));
                    break;

                case CARD_MONOPOLY:
                    sendMessageToAll(CatanMessage.monopoleCard(SocketUtils.toInt(session.getId()), message));
                    utils.applyMonopolyCard(session, message);
                    break;

                case CARD_INVENTION:
                    sendMessageToAll(CatanMessage.inventionCard(SocketUtils.toInt(session.getId()), message));
                    utils.applyInventionCard(session, message);
                    break;

                case CARD_KNIGHT:
                    sendMessageToAll(CatanMessage.knightCard(SocketUtils.toInt(session.getId()), message));
                    utils.applyKnightCard(session, message);
                    break;

                case CARD_RD_CON:
                    sendMessageToAll(CatanMessage.roadConstructionCard(SocketUtils.toInt(session.getId()), message));
                    utils.applyRoadConstructionCard(session, message);
                    break;

                case ROBBER_TO:
                    utils.moveRobber(session, message);
                    if (getGameCtrl().realPlayersAreReady())
                        getGameCtrl().activateKIs();
                    break;

                case TOSS_CARDS:
                    utils.tossRawMaterials(session, message);
                    if (getGameCtrl().realPlayersAreReady())
                        getGameCtrl().activateKIs();
                    break;

                case CARD_BUY:
                    utils.buyDevCard(session);
                    break;

                case SEA_TRADE:
                    utils.seatrade(session, message);
                    break;

                case TRD_REQ:
                    TradeRequest tradeRequest = utils.tradeOffer(session, message);
                    addTradeRequest(tradeRequest);
                    break;

                case TRD_RES:
                    utils.tradeAccepted(session, message);
                    break;

                case TRD_SEL:
                    utils.tradeConduction(session, message);
                    break;

                case TRD_REJ:
                    utils.tradeCancelled(message);
                    break;

                case END_TURN:
                    nextMove();
                    break;

                case ADD_KI:
                    Player newPlayer = getGameCtrl().addKI();
                    newPlayer.addPropertyChangeListener(this);
                    newPlayer.setStatus(WAIT_FOR_GAME_START);

                    if (utils.getGameCtrl().readyToStartGame()) {
                        sendMessageToAll(CatanMessage.startGame(utils.getGameCtrl().getBoard()));
                        getGameCtrl().startGame();
                    }
                    break;
            }


        } catch (CatanException ex) {

            if (ex.sendToClient()) {
                errorMessage = ex.getMessage();
            } else {
                ex.printStackTrace();
                errorMessage = "";
            }

            OK = false;
            errorObject = ex.getErrorObject();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (OK) sendOK(session);
            else if (!errorMessage.equals("")) {
                System.out.printf("Send error to: %s %s\n", session.getId(), errorMessage);
                sendError(session, errorMessage);
            }

            if (errorObject instanceof TradeRequest) {
                TradeRequest tr = (TradeRequest) errorObject;
                tr.accept(false, SocketUtils.toInt(session.getId()));
            }
        }
    }

    private void nextMove() {
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
        System.out.printf("Session %s has ended\n", session.getId());

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

    private void sendOpponents(WebSocketSession session) {
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
                System.out.printf("Send Message to %s: %s\n", SocketUtils.toInt(session.getId()), message.getPayload());
                session.sendMessage(message);
            }

        } catch (IOException ex) {
            System.out.printf("Send error to: %s %s\n", session.getId(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    //region sendError
    private void sendError(WebSocketSession session, String errorDescription) {
        sendSynchronizedMessage(session, CatanMessage.error(errorDescription));
    }
    //endregion

    //region trading
    public boolean addTradeRequest(TradeRequest tradeRequest) throws CatanException {

        //at least one raw material must be requested/offered
        if (tradeRequest.getOffer().getTotalCount() < 1 || tradeRequest.getRequest().getTotalCount() < 1)
            throw new CatanException("Angebot/Nachfrage müssen je mindestens einen Rohstoff enthalten.", true);

        //has player offered raw materials ?
        Player player = utils.getGameCtrl().getPlayer(tradeRequest.getPlayerId());
        if (!player.canAfford(tradeRequest.getOffer()))
            throw new CatanException("Du hast nicht genug Rohstoffe, um diesen Handel anzubieten.", true);

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
        Player player = (Player) evt.getNewValue();

        boolean sendStatusUpdate;

        System.out.printf("Property '%s': '%s' -> '%s'%n",
                evt.getPropertyName(), evt.getOldValue(), player.getStatus());

        sendStatusUpdate = handlePlayerPropertyChange(evt, player);

        try {
            if (sendStatusUpdate) {
                System.out.println("Send status update");
                sendStatusUpdate(player);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (player.isKI()) {
            handleKIPropertyChange(evt, player);
        }
    }

    //endregion

    private void handleKIPropertyChange(PropertyChangeEvent evt, Player ki) {
        if (!ki.isKI() || !evt.getPropertyName().equals(STATUS_UPD)) return;

        try {
            switch (ki.getStatus()) {
                case START_GAME:
                    ki.setStatus(WAIT_FOR_GAME_START);
                    break;

                case EXTRACT_CARDS_DUE_TO_ROBBER:
                    //KI has to extract cards
                    getGameCtrl().getBoard().getRobber().robPlayer(ki);

                    if (!ki.getNextStatus().equals(WAIT) && !ki.getNextStatus().equals(""))
                        getGameCtrl().deactivateKI(ki);
                    else { ki.activateNextStatus(); }
                    break;

                case DICE:
                    getGameCtrl().dice(ki.getId());
                    break;

                case BUILD_SETTLEMENT:
                    Building settlement = new Building(ki.getId(),
                            BuildingType.SETTLEMENT, getGameCtrl().getBoard().getRandomFreeSettlementLoc());
                    getGameCtrl().placeBuilding(settlement, true);
                    break;

                case BUILD_STREET:
                    Building street = new Building(ki.getId(), BuildingType.ROAD,
                            getGameCtrl().getBoard().getFreeRoadLoc(ki, getGameCtrl().isInitialPhaseActive()));
                    getGameCtrl().placeBuilding(street, true);
                    break;

                case TRADE_OR_BUILD:
                    if (ki.canAfford(BuildingType.SETTLEMENT)) {
                        Building s = new Building(ki.getId(), BuildingType.SETTLEMENT, getGameCtrl().getBoard().getRandomFreeSettlementLoc());
                        getGameCtrl().placeBuilding(s, true);
                    } else if (ki.canAfford(BuildingType.ROAD)) {
                        Building r = new Building(ki.getId(), BuildingType.ROAD, getGameCtrl().getBoard().getFreeRoadLoc(ki, getGameCtrl().isInitialPhaseActive()));
                        getGameCtrl().placeBuilding(r, true);
                    } else if (ki.canAffordDevCard()) {
                        getGameCtrl().buyDevelopmentCard(ki);
                    }

                    nextMove();

                    break;

                case ROBBER_TO:
                    boolean activated;
                    try {
                        getGameCtrl().activateRobber(ki.getId(), -1, getGameCtrl().getBoard().getRandomFieldLoc());
                        activated = true;
                    } catch (CatanException ex) {
                        System.out.println(ex.getMessage());
                        activated = false;
                    }

                    //try second time
                    if (!activated)
                        getGameCtrl().activateRobber(ki.getId(), -1, getGameCtrl().getBoard().getRandomFieldLoc());

                    break;

            }
        } catch (CatanException ex) {

            //initialize next move if exception was thrown during TRADE_OR_BUILD_STATUS
            if ((ki.getStatus().equals(TRADE_OR_BUILD) || ki.getStatus().equals(ROBBER_TO)))
                nextMove();
        }
    }

    private boolean handlePlayerPropertyChange(PropertyChangeEvent evt, Player player) {
        boolean sendStatusUpdate = true;

        try {
            switch (evt.getPropertyName()) {

                case TRD_DECR_NO_STAT_UPD:
                    sendStatusUpdate = false;
                case TRD_DECR:
                    RawMaterialOverview costs = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(CatanMessage.costs(getGameCtrl().getCurrent().getId(), costs, false));
                    break;

                case TRD_INCR:
                    RawMaterialOverview harvest = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(CatanMessage.harvest(getGameCtrl().getCurrent().getId(), harvest, false));
                    break;

                case RMO_INCR_NO_STAT_UPD:
                    sendStatusUpdate = false;
                    //if there was a decrease/increase of raw materials send harvest/cost messages
                case RMO_INCR:
                    RawMaterialOverview harvest2 = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(Integer.toHexString(player.getId()),   //parse id back to hex string (session id)
                            CatanMessage.harvest(player.getId(), harvest2, false),
                            CatanMessage.harvest(player.getId(), harvest2, true));
                    break;

                case RMO_DECR_NO_STAT_UPD:
                    sendStatusUpdate = false;
                case RMO_DECR:
                    RawMaterialOverview costs2 = (RawMaterialOverview) evt.getOldValue();
                    sendMessageToAll(Integer.toHexString(player.getId()),   //parse id back to hex string (session id)
                            CatanMessage.costs(player.getId(), costs2, false),
                            CatanMessage.costs(player.getId(), costs2, true));
                    break;

                case DICE:
                    sendMessageToAll(CatanMessage.throwDice(player.getId(), (int[]) evt.getOldValue()));
                    sendStatusUpdate = false;
                    System.out.println("Dice: send no status update");
                    break;

                case DEV_CARD_BUY:
                    TextMessage msg1 = CatanMessage.developmentCardBought(player.getId(), (DevCardType) evt.getOldValue());
                    TextMessage msg2 = CatanMessage.developmentCardBought_Public(player.getId());
                    sendMessageToAll(Integer.toHexString(player.getId()), msg1, msg2);
                    break;

                case VP_INCR:
                case END_TURN:
                case STATUS_UPD:
                    sendStatusUpdate = true;
                    break;
                default:
                    sendStatusUpdate = false;
            }
        } catch (CatanException ex) {
            ex.printStackTrace();

            if (ex.sendToClient())
                sendMessageToAll(CatanMessage.error(ex.getMessage()));
        }

        return sendStatusUpdate;
    }
}