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
    public boolean build(int id, TextMessage message) throws IOException {
        Building building = gson.fromJson(JSONUtils.createJSON(message)
                .getJSONObject(Constants.BUILD).toString(), Building.class);
        return this.gameCtrl.placeBuilding(id, building.getLocations(), building.getType());
    }
    //endregion

    //region applyRoadConstructionCard
    public boolean applyRoadConstructionCard(WebSocketSession session, TextMessage message) throws Exception {

        //player id
        Player player = gameCtrl.getPlayer(session.getId());
        if (!player.hasRoadConstructionCard()) return false;

        //the first road
        Location[] locs1 = gson.fromJson(JSONUtils.createJSON(message)
                .getJSONObject(Constants.CARD_RD_CON).getJSONArray("Strasse 1").toString(), Location[].class);

        Building road1 = new Building(player.getId(), BuildingType.ROAD, locs1);

        //add roads to board directly
        boolean successful = this.gameCtrl.getBoard().placeBuilding
                (player.getId(), road1.getLocations(), road1.getType(), false);

        if (successful) {
            //the second road
            Location[] locs2 = gson.fromJson(JSONUtils.createJSON(message)
                    .getJSONObject(Constants.CARD_RD_CON).getJSONArray("Strasse 2").toString(), Location[].class);

            Building road2 = new Building(player.getId(), BuildingType.ROAD, locs2);

            successful = this.gameCtrl.getBoard().placeBuilding(player.getId(), road2.getLocations(),
                    road2.getType(), false);
        }

        //remove construction card
        if (successful) {
            try {
                this.gameCtrl.getPlayer(player.getId()).removeDevelopmentCard(DevCardType.ROAD_CONSTRUCTION);
                successful = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                successful = false;
            }
        }
        return successful;
    }
    //endregion

    //region performHandshake
    public boolean performHandshake(WebSocketSession session) throws InterruptedException, IOException {
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
            throws InterruptedException, IOException {
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
            if (!validName) {
                System.out.printf("Send Message to %s: %s", gPlayer.getId(), Constants.NAME_ALREADY_ASSIGNED);
                session.sendMessage(CatanMessage.error(Constants.NAME_ALREADY_ASSIGNED));
            }

            if (!validColor) {
                System.out.printf("Send Message to %s: %s", gPlayer.getId(), Constants.COLOR_ALREADY_ASSIGNED);
                session.sendMessage(CatanMessage.error(Constants.COLOR_ALREADY_ASSIGNED));
            }
        }
    }
    //endregion

    //region handleStartGameMessage
    public boolean handleStartGameMessage(WebSocketSession session, TextMessage message) throws IOException {
        boolean ready = false;
        JSONObject json = (JSONUtils.createJSON(message)).
                getJSONObject(Constants.START_GAME);

        if (json.length() == 0) {
            setStatus(session.getId(), Constants.WAIT_FOR_GAME_START);
            ready = gameCtrl.readyToStartGame();
        }
        return ready;
    }
    //endregion

    //region setStatus
    public void setStatus(String id, String status) {
        Player player = gameCtrl.getPlayer(id);
        player.setStatus(status);
    }
    //endregion

    //region trading
    public boolean seatrade(WebSocketSession session, TextMessage message) {
        TradeRequest tradeRequest = CatanMessage.seatradeToTradeRequest(SEA_TRADE, message);

        //at least one raw material must be offered/requested
        if (tradeRequest.getOffer().getTotalCount() < 1 || tradeRequest.getRequest().getTotalCount() < 1)
            return false;

        Player player = gameCtrl.getPlayer(session.getId());
        return gameCtrl.seaTrade(player, tradeRequest);
    }

    public TradeRequest tradeOffer(WebSocketSession session, TextMessage message) {
        TradeRequest tr = CatanMessage.seatradeToTradeRequest(TRD_REQ, message);
        TradeRequest tradeRequest = new TradeRequest(tr.getOffer(), tr.getRequest());
        tradeRequest.setPlayerId(toInt(session.getId()));

        return tradeRequest;
    }

    public boolean tradeAccepted(WebSocketSession session, TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(TRD_RES);
        int tradeId = (Integer)payload.get(TRADE_ID);
        boolean accept = (Boolean)payload.get(ACCEPT);

        TradeRequest tr = getTradeRequest(tradeId);

        if (tr != null) {

            //player can only accept trade offer if he can afford the request
            Player player = gameCtrl.getPlayer(session.getId());
            if (player.canAfford(tr.getRequest())) {
                tr.accept(accept, toInt(session.getId()));
                return true;
            }
        }
        return false;
    }

    public boolean tradeCancelled(TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(TRD_REJ);
        int tradeId = (Integer) payload.get(TRADE_ID);
        TradeRequest tr = getTradeRequest(tradeId);

        if (tr != null) {
            tr.cancel();
            tradeRequests.remove(tr);
            return true;
        }
        return false;
    }

    public boolean tradeConduction(WebSocketSession session, TextMessage message) {
        boolean conducted = false;

        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(TRD_SEL);
        Integer id = (Integer) payload.get(TRADE_ID);
        Integer fellowId = (Integer) payload.get(FELLOW_PLAYER);
        TradeRequest tr = getTradeRequest(id);

        if (tr != null) {
            conducted = gameCtrl.domesticTrade(tr, toInt(session.getId()), fellowId);
            if (conducted) tradeRequests.remove(tr);
        }
        return conducted;
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
    public boolean moveRobber(WebSocketSession session, TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(ROBBER_TO);
        Location loc = gson.fromJson(payload.getJSONObject(PLACE).toString(), Location.class);

        int id = payload.has(DESTINATION) ? (Integer) payload.get(DESTINATION) : -1;

        return gameCtrl.activateRobber(toInt(session.getId()), id, loc);
    }

    /**
     * a player has to toss raw materials
     *
     * @param session
     * @param message
     * @return
     */
    public boolean tossRawMaterials(WebSocketSession session, TextMessage message) {
        JSONObject json = JSONUtils.createJSON(message).getJSONObject(TOSS_CARDS).getJSONObject(TOSS);
        JSONObject rawMaterialJSON = json.getJSONObject(Constants.RAW_MATERIALS);
        RawMaterialOverview overview = gson.fromJson(rawMaterialJSON.toString(), RawMaterialOverview.class);

        return gameCtrl.tossRawMaterialCards(toInt(session.getId()), overview);
    }

    public boolean applyInventionCard(WebSocketSession session, TextMessage message) throws Exception {
        Player player = gameCtrl.getPlayer(session.getId());

        JSONObject json = JSONUtils.createJSON(message).getJSONObject(INVENTION);
        JSONObject rawMaterialJSON = json.getJSONObject(Constants.RAW_MATERIALS);
        RawMaterialOverview overview = gson.fromJson(rawMaterialJSON.toString(), RawMaterialOverview.class);

        return gameCtrl.applyInventionCard(player, overview);
    }

    public boolean applyMonopolyCard(WebSocketSession session, TextMessage message) throws Exception {
        Player monoPlayer = gameCtrl.getPlayer(session.getId());

        JSONObject json = JSONUtils.createJSON(message).getJSONObject(MONOPOLE);
        JSONObject rawMaterialJSON = json.getJSONObject(Constants.RAW_MATERIAL);
        RawMaterialType type = gson.fromJson(rawMaterialJSON.toString(), RawMaterialType.class);

        return gameCtrl.applyMonopoleCard(monoPlayer, type);
    }
    //endregion

}
