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

        initializeGame();
        this.sessions = Collections.synchronizedSet(new HashSet<WebSocketSession>());
    }

    private void initializeGame() {
        utils = new SocketUtils();

        utils.getGameCtrl().getBoard().addPropertyChangeListener(e -> {
                    if ("new building".equals(e.getPropertyName())) {
                        System.out.printf("A new building has been built: %s %n",
                                ((Building) e.getNewValue()).toJSONString());
                        sendMessageToAll(CatanMessage.newBuilding((Building) e.getNewValue()));
                    }
                }
        );

        utils.getGameCtrl().addPropertyChangeListener(e -> {
            TextMessage message = null;
            boolean sendStatusUpdate = false;
            Player player = ROBBER_AT.equals(e.getPropertyName()) ? null : (Player) e.getNewValue();

            switch (e.getPropertyName()) {
                case ROBBER_AT:
                    System.out.println("Robber was moved");
                    sendMessageToAll((TextMessage) e.getNewValue());
                    break;

                case KNIGHT_PLAYED:
                    sendStatusUpdate = true;
                    Object[] knightData = (Object[]) e.getOldValue();
                    message = CatanMessage.knightCard(player.getId(), (int) knightData[0], (Location) knightData[1]);
                    break;

                case MONOPOLE_PLAYED:
                    sendStatusUpdate = true;
                    message = CatanMessage.monopoleCard(player.getId(), (RawMaterialType) e.getOldValue());
                    break;

                case INVENTION_PLAYED:
                    sendStatusUpdate = true;
                    message = CatanMessage.inventionCard(player.getId(), (RawMaterialOverview) e.getOldValue());
                    break;

                case RC_PLAYED:
                    sendStatusUpdate = true;
                    message = CatanMessage.roadConstructionCard(player.getId());
                    break;

                case GAME_OVER:
                    message = CatanMessage.endGame(player);

                    break;
            }

            if (message != null) {
                try {
                    sendMessageToAll(message);
                    if(sendStatusUpdate)this.sendStatusUpdate(player);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
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

        try {
            Player player = new Player(SocketUtils.toInt(session.getId()));
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
                        startGame();
                        getGameCtrl().moveKIs();
                    }
                    break;

                case BUILD:
                    OK = utils.build(SocketUtils.toInt(session.getId()), message);
                    if (!OK) errorMessage = "Das Gebäude kann nicht gebaut werden.";
                    break;

                case ROLL_DICE:
                    getGameCtrl().dice(SocketUtils.toInt(session.getId()));
                    break;

                case CARD_MONOPOLE:
                    utils.applyMonopoleCard(session, message);
                    break;

                case CARD_INVENTION:
                    utils.applyInventionCard(session, message);
                    break;

                case CARD_KNIGHT:
                    utils.applyKnightCard(session, message);
                    break;

                case CARD_RD_CON:
                    utils.setFirstRoadStatus(session);
                    break;

                case ROBBER_TO:
                    utils.moveRobber(session, message);
                    break;

                case TOSS_CARDS:
                    utils.tossRawMaterials(session, message);
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
                    getGameCtrl().nextMove();
                    break;

                case ADD_KI:
                    Player newPlayer = getGameCtrl().addKI();
                    newPlayer.addPropertyChangeListener(this);
                    newPlayer.setStatus(WAIT_FOR_GAME_START);

                    if (utils.getGameCtrl().readyToStartGame()) {
                        startGame();
                        getGameCtrl().moveKIs();
                    }
                    break;

                default:
                    OK = false;
                    errorMessage = String.format("Der Nachrichtentyp %s ist uns nicht bekannt.", type);
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
            //the game is over and will be reinitialized
            if (getGameCtrl().isGameOver()) {
                if (sessions.size() == 0) initializeGame();
            } else {
                //send OK message
                if (OK) sendOK(session);

                    //send error to client
                else if (!errorMessage.equals("")) {
                    System.out.printf("Send error to: %s %s\n", session.getId(), errorMessage);
                    sendError(session, errorMessage);
                }

                if (errorObject instanceof TradeRequest) {
                    TradeRequest tr = (TradeRequest) errorObject;
                    tr.accept(false, SocketUtils.toInt(session.getId()));
                }

                Player current = getGameCtrl().getCurrent();
                if (current.isKI() && (current.isActive() || current.getStatus().equals(WAIT_FOR_ALL_TO_EXTRACT_CARDS))) {
                    getGameCtrl().moveKIs();
                }

                if (!current.isKI() && current.getStatus().equals(WAIT_FOR_ALL_TO_EXTRACT_CARDS))
                    getGameCtrl().updateStatusAfterCardExtraction(current);
            }
        }
    }

    private void startGame() {
        getGameCtrl().defineTurnOrder();

        int[] ids = new int[getGameCtrl().getPlayers().size()];

        for (int i = 0; i < getGameCtrl().getPlayers().size(); i++) {
            ids[i] = getGameCtrl().getPlayers().get(i).getId();
        }

        sendMessageToAll(CatanMessage.startGame(utils.getGameCtrl().getBoard(), ids));
        getGameCtrl().startGame();
    }
    //endregion

    //region afterConnectionClosed
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        String id = session.getId();
        System.out.printf("Session %s has ended\n", id);

        // remove the session from sessions list
        sessions.remove(session);
        sendMessageToAll(CatanMessage.playerLeft(SocketUtils.toInt(id)));

        //replace player by KI
        if (sessions.size() > 0) {
            Player player = getGameCtrl().getPlayer(id);

            if (player == null)
                throw new CatanException(String.format("Es konnte kein Spieler mit der ID %s gefunden werden.", id), true);

            player.setKI(true);
            if (player.equals(getGameCtrl().getCurrent()))
                getGameCtrl().moveKIs();
        }

        //no real player left --> reinitialize game
        else {
            initializeGame();
            this.sessions = Collections.synchronizedSet(new HashSet<WebSocketSession>());
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

        // the request cant have the same elements as the offer
        if (tradeRequest.getRequest().hasSameMaterial(tradeRequest.getOffer()))
            throw new CatanException("Angebot und Nachfrage können nicht die gleichen Rohstoffe enthalten.", true);

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

        getGameCtrl().makeKIsRespondToTr(tradeRequest);

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
    }

    //endregion

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

                case VICTORY_PTS:
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
