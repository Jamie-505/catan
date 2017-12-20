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

    public int toInt(String string) {
        return Integer.parseInt(string, 16);
    }

    //region build
    public boolean build(WebSocketSession session, TextMessage message) throws IOException {
        int id = toInt(session.getId());
        Building building = gson.fromJson(JSONUtils.createJSON(message)
                .getJSONObject(Constants.BUILD).toString(), Building.class);
        boolean built = this.gameCtrl.placeBuilding(id, building.getLocations(), building.getType());

        if (built) {
            //in the building phase the current player can build a road after a settlement
            if (gameCtrl.isBuildingPhaseActive()) {
                Player next = gameCtrl.getCurrent();
                String status = Constants.BUILD_STREET;

                if (building.getType().equals(BuildingType.ROAD)) {
                    next = gameCtrl.getNext();
                    status = Constants.BUILD_SETTLEMENT;
                }

                nextMove(next.getId(), status);
            }
        }

        return built;
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

    //region nextMove
    public void nextMove(int idOfNextPlayer, String statusForNextPlayer) throws IOException {
        gameCtrl.setPlayerActive(idOfNextPlayer, statusForNextPlayer);
    }
    //endregion

    //region startGame
    public void startGame() throws IOException {
        gameCtrl.startGame();

        Player player = gameCtrl.getCurrent();
        nextMove(player.getId(), Constants.BUILD_SETTLEMENT);
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

    //region extractCardsDueToRobber
    public void extractCardsDueToRobber() throws IOException {
        for (Player player : gameCtrl.getPlayers()) {
            if (player.hasToExtractCards()) {
                player.setStatus(Constants.EXTRACT_CARDS_DUE_TO_ROBBER);
            }
        }
    }
    //endregion*/

    //region tossRawMaterials

    /**
     * a player has to toss raw materials
     *
     * @param session
     * @param message
     * @return
     */
    public boolean tossRawMaterials(WebSocketSession session, TextMessage message) {
        JSONObject json = JSONUtils.createJSON(message);
        JSONObject rawMaterialJSON = json.getJSONObject(Constants.RAW_MATERIALS);
        RawMaterialOverview overview = gson.fromJson(rawMaterialJSON.toString(), RawMaterialOverview.class);
        Player player = gameCtrl.getPlayer(toInt(session.getId()));

        try {
            player.decreaseRawMaterials(overview);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }
    //endregion

    //region setStatus
    public void setStatus(String id, String status) {
        Player player = gameCtrl.getPlayer(id);
        player.setStatus(status);
    }
    //endregion

    public boolean seatrade(WebSocketSession session, TextMessage message) {
        TradeRequest tradeRequest = CatanMessage.seatradeToTradeRequest(SEA_TRADE, message);
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
        TradeRequest tradeRequest = CatanMessage.seatradeToTradeRequest(TRD_RES, message);
        return tradeRequest.accept(toInt(session.getId()));
    }

    public boolean tradeCancelled(TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(TRD_REJ);
        int tradeId = (Integer)payload.get(TRADE_ID);
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

        JSONObject payload = JSONUtils.createJSON(message);
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

}
