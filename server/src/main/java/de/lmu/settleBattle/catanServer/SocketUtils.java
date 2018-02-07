package de.lmu.settleBattle.catanServer;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

import static de.lmu.settleBattle.catanServer.Constants.*;

public class SocketUtils {

    // set to store all live sessions
    private GameController gameCtrl;
    public static Gson gson = new Gson();
    public List<TradeRequest> tradeRequests = new ArrayList<>();

    public List<TradeRequest> getTradeRequests() {
        return tradeRequests;
    }

    public SocketUtils() {
        this.gameCtrl = new GameController();
    }

    public GameController getGameCtrl() {
        return gameCtrl;
    }

    public static int toInt(String string) {
        return Integer.parseInt(string, 16);
    }

    //region build
    public boolean build(int id, TextMessage message) throws CatanException {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(BUILD);

        if (!payload.has(TYPE) || !payload.has(PLACE)) {
            Player player = gameCtrl.getPlayer(id);
            if (player.isRCActive()) {
                player.setStatus(TRADE_OR_BUILD);
                return true;
            }
            return false;
        }


        Building building = gson.fromJson(JSONUtils.createJSON(message)
                .getJSONObject(Constants.BUILD).toString(), Building.class);
        if (building.getOwner() == id)
            return this.gameCtrl.placeBuilding(building, true);
        else throw new CatanException(
                String.format("Spieler %s kann kein Gebäude mit der ID %s bauen.", id, building.getOwner()), true);
    }
    //endregion

    //region performHandshake
    public boolean performHandshake(WebSocketSession session) throws IOException {
        TextMessage idAllocation = CatanMessage.welcomeNewPlayer(toInt(session.getId()));
        session.sendMessage(idAllocation);

        //update status of new player
        Player player = gameCtrl.getPlayer(session.getId());
        player.setStatus(Constants.START_GAME);
        return true;
    }
    //endregion

    //region assignPlayerData
    public void assignPlayerData(WebSocketSession session, TextMessage message)
            throws CatanException {
        JSONObject json = JSONUtils.createJSON(message);
        Player gPlayer = gson.fromJson(json.get(PLAYER).toString(), Player.class);
        Color color = gPlayer.getColor();
        String name = gPlayer.getName();

        boolean validName = gameCtrl.isValidName(name);
        boolean validColor = gameCtrl.isValidColor(color);

        if (validColor && validName) {
            Player player = gameCtrl.getPlayer(session.getId());
            player.setName(name);
            player.setColor(color);

        } else {
            if (!validName)
                throw new CatanException(Constants.NAME_ALREADY_ASSIGNED, true);

            if (!validColor)
                throw new CatanException(Constants.COLOR_ALREADY_ASSIGNED, true);

        }
    }
    //endregion

    //region handleStartGameMessage
    public void handleStartGameMessage(WebSocketSession session, TextMessage message)
            throws CatanException {
        JSONObject json = (JSONUtils.createJSON(message)).
                getJSONObject(Constants.START_GAME);

        if (json.length() != 0) throw new CatanException("Die Nachricht ist falsch formatiert.");

        setStatus(session.getId(), Constants.WAIT_FOR_GAME_START);
    }
    //endregion

    //region setStatus
    public void setStatus(String id, String status) {
        Player player = gameCtrl.getPlayer(id);
        player.setStatus(status);
    }
    //endregion

    public void buyDevCard(WebSocketSession session) throws CatanException {
        Player player = gameCtrl.getPlayer(session.getId());
        gameCtrl.buyDevelopmentCard(player);
    }

    //region trading
    public void seatrade(WebSocketSession session, TextMessage message) throws CatanException {
        TradeRequest tradeRequest = CatanMessage.seatradeToTradeRequest(SEA_TRADE, message);

        //at least one raw material must be offered/requested
        if (tradeRequest.getOffer().getTotalCount() < 1 || tradeRequest.getRequest().getTotalCount() < 1)
            throw new CatanException("Das Angebot und die Nachfrage müssen mehr als einen Rohstoff enthalten.", true);

        Player player = gameCtrl.getPlayer(session.getId());
        gameCtrl.seaTrade(player, tradeRequest);
    }

    public TradeRequest tradeOffer(WebSocketSession session, TextMessage message) {
        TradeRequest tr = CatanMessage.seatradeToTradeRequest(TRD_REQ, message);
        TradeRequest tradeRequest = new TradeRequest(tr.getOffer(), tr.getRequest());
        tradeRequest.setPlayerId(toInt(session.getId()));

        return tradeRequest;
    }

    public void tradeAccepted(WebSocketSession session, TextMessage message) throws CatanException {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(TRD_RES);
        int tradeId = (Integer) payload.get(TRADE_ID);
        boolean accept = (Boolean) payload.get(ACCEPT);

        TradeRequest tr = getTradeRequest(tradeId);

        if (tr == null)
            throw new CatanException(String.format(INVALID_TRD_ID, tradeId), true);

        //player can only accept trade offer if he can afford the request
        Player player = gameCtrl.getPlayer(session.getId());

        if (!player.canAfford(tr.getRequest()))
            throw new CatanException("Du besitzt die geforderten Rohstoffe nicht.", true, tr);

        tr.accept(accept, toInt(session.getId()));
    }

    public void tradeCancelled(TextMessage message) throws CatanException {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(TRD_REJ);
        int tradeId = (Integer) payload.get(TRADE_ID);
        TradeRequest tr = getTradeRequest(tradeId);

        if (tr == null)
            throw new CatanException(String.format(INVALID_TRD_ID, tradeId), true);

        tr.cancel();
        tradeRequests.remove(tr);
    }

    public void tradeConduction(WebSocketSession session, TextMessage message) throws CatanException {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(TRD_SEL);
        Integer tradeId = (Integer) payload.get(TRADE_ID);
        Integer fellowId = (Integer) payload.get(FELLOW_PLAYER);
        TradeRequest tr = getTradeRequest(tradeId);

        if (tr == null)
            throw new CatanException(String.format(INVALID_TRD_ID, tradeId), true);

        gameCtrl.domesticTrade(tr, toInt(session.getId()), fellowId);
        tradeRequests.remove(tr);
    }

    public TradeRequest getTradeRequest(int tradeId) {
        for (TradeRequest tr : tradeRequests) {
            if (tr.getId() == tradeId)
                return tr;
        }
        return null;
    }
    //endregion

    //region robber functions
    public void moveRobber(WebSocketSession session, TextMessage message) throws CatanException {
        moveRobber(session, message, ROBBER_TO);
    }

    public void moveRobber(WebSocketSession session, TextMessage message, String type) throws CatanException {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(type);
        Location loc = gson.fromJson(payload.getJSONObject(PLACE).toString(), Location.class);

        int id = payload.has(DESTINATION) ? (Integer) payload.get(DESTINATION) : -1;

        gameCtrl.activateRobber(toInt(session.getId()), id, loc);
    }

    /**
     * a player has to toss raw materials
     *
     * @param session
     * @param message
     * @return
     */
    public void tossRawMaterials(WebSocketSession session, TextMessage message) throws CatanException {
        JSONObject rawMaterialJSON = JSONUtils.createJSON(message).getJSONObject(TOSS_CARDS);
        RawMaterialOverview overview = gson.fromJson(rawMaterialJSON.toString(), RawMaterialOverview.class);

        gameCtrl.tossCardsAndUpdateStatus(toInt(session.getId()), overview);
    }

    public boolean endGame(WebSocketSession session) {

        return gameCtrl.endGame(gameCtrl.getPlayer(session.getId()));
    }

    public void applyInventionCard(WebSocketSession session, TextMessage message) throws Exception {
        Player player = gameCtrl.getPlayer(session.getId());

        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(INVENTION);
        JSONObject rawMaterialJSON = payload.getJSONObject(Constants.RAW_MATERIALS);
        RawMaterialOverview overview = gson.fromJson(rawMaterialJSON.toString(), RawMaterialOverview.class);

        gameCtrl.applyInventionCard(player, overview);
    }

    public void applyMonopoleCard(WebSocketSession session, TextMessage message) throws Exception {
        Player monoPlayer = gameCtrl.getPlayer(session.getId());

        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(MONOPOLE);
        RawMaterialType type = gson.fromJson(payload.getString(RAW_MATERIAL), RawMaterialType.class);

        gameCtrl.applyMonopoleCard(monoPlayer, type);
    }

    public void applyKnightCard(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(CARD_KNIGHT);

        int opponentId = -1;
        if (payload.has(DESTINATION))
            opponentId = payload.getInt(DESTINATION);

        Location newRobberLoc = gson.fromJson(payload.getJSONObject(PLACE).toString(), Location.class);

        gameCtrl.applyKnightCard(toInt(session.getId()), opponentId, newRobberLoc);
    }

    public void setFirstRoadStatus(WebSocketSession session) throws CatanException {
        Player p = gameCtrl.getPlayer(session.getId());

        if (!p.hasRoadConstructionCard())
            throw new CatanException("Du hast keine Strassenbaukarte", true);

        p.setStatus(FIRST_STREET);
    }

    //endregion

}
