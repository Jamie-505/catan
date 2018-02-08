package de.lmu.settleBattle.catanServer;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import static de.lmu.settleBattle.catanServer.Constants.*;

public class CatanMessage {

    //region serverProtocol

    /**
     * Creates message containing version and protocol
     *
     * @return JSON structured String Object
     */
    public static TextMessage serverProtocol() {
        JSONObject payload = new JSONObject();
        payload.put(VERSION, VERSION_SERVER);
        payload.put(PROTOCOL, "1.0");
        return new TextMessage(JSONUtils.setJSONType(HANDSHAKE, payload).toString());
    }
    //endregion

    //region welcomeNewPlayer

    /**
     * Json when client needs it's own session details
     *
     * @param id
     * @return JSON structured String
     */
    public static TextMessage welcomeNewPlayer(int id) {
        JSONObject payload = new JSONObject();
        payload.put("id", id);
        return new TextMessage(JSONUtils.setJSONType(GET_ID, payload).toString());
    }
    //endregion

    //region statusUpdateToPlayer
    public static Player statusUpdateToPlayer(TextMessage message) {
        JSONObject json = JSONUtils.createJSON(message);

        return SocketUtils.gson.fromJson(
                json.getJSONObject(STATUS_UPD).getJSONObject(PLAYER).toString(),
                Player.class);
    }
    //endregion

    public static TradeRequest seatradeToTradeRequest(String tradeType, TextMessage message) {
        JSONObject json = JSONUtils.createJSON(message);

        return SocketUtils.gson.fromJson(
                json.getJSONObject(tradeType).toString(),
                TradeRequest.class);
    }

    //region newBuildingToBuilding
    public static Building newBuildingToBuilding(TextMessage message) {
        JSONObject json = JSONUtils.createJSON(message);

        return SocketUtils.gson.fromJson(
                json.getJSONObject(NEW_BUILDING).toString(), Building.class);
    }
    //endregion

    //region messageToObject
    public static Object messageToObject(TextMessage message) {
        String type = JSONUtils.getMessageType(message);
        Object object = null;

        switch (type) {
            case STATUS_UPD:
                object = statusUpdateToPlayer(message);
                break;
            case NEW_BUILDING:
                object = newBuildingToBuilding(message);
                break;

        }

        return object;
    }
    //endregion

    //region extractBoard

    /**
     * extracts board object from startGame message
     *
     * @param startGameMsg
     * @return
     */
    public static Board extractBoard(TextMessage startGameMsg) {
        JSONObject json = JSONUtils.createJSON(startGameMsg);

        return SocketUtils.gson.fromJson(
                json.getJSONObject(START_CON).getJSONObject(CARD).toString(), Board.class);
    }
    //endregion

    //region throwDices

    /**
     * creates message to send to all players when a player throws the dices
     *
     * @param id
     * @param diceResult
     * @return JSON structured String containing the dice result
     */
    public static TextMessage throwDice(int id, int[] diceResult) throws CatanException {

        if (diceResult.length != 2) {
            StringBuilder arrayToString = new StringBuilder();
            for (int i = 0; i < diceResult.length; i++)
                arrayToString.append(i).append(": ").append(diceResult[i]).append(", ");

            throw new CatanException("No valid array " + arrayToString, false);
        }

        JSONObject payload = new JSONObject();
        payload.put(PLAYER, id);
        payload.put(DICE_THROW, diceResult);

        return new TextMessage(JSONUtils.setJSONType(DICE_RESULT, payload).toString());
    }
    //endregion

    //region statusUpdate

    /**
     * creates message containing status update
     *
     * @param player
     * @return string message structured like JSON
     */
    public static TextMessage statusUpdate(Player player) {
        JSONObject payload = player.toJSON();

        return new TextMessage(JSONUtils.setJSONType(Constants.STATUS_UPD,
                JSONUtils.setJSONType(Constants.PLAYER, payload)).toString());
    }

    /**
     * creates message containing status update
     *
     * @param player
     * @return string message structured like JSON
     */
    public static TextMessage statusUpdatePublic(Player player) {
        JSONObject payload = player.toJSON_Unknown();

        return new TextMessage(JSONUtils.setJSONType(Constants.STATUS_UPD,
                JSONUtils.setJSONType(Constants.PLAYER, payload)).toString());
    }


    //endregion

    //region board

    /**
     * creates message containing start game and card for player
     *
     * @param board
     * @return string message structured like JSON
     */

    public static TextMessage startGame(Board board) {
        JSONObject payload = JSONUtils.setJSONType(Constants.CARD, board.toJSON());
        return new TextMessage(JSONUtils.setJSONType(START_CON, payload).toString());
    }

    public static TextMessage startGame(Board board, int[] ids) {
        JSONObject payload = JSONUtils.setJSONType(Constants.CARD, board.toJSON());
        payload.put(TURN_ORDER, ids);
        return new TextMessage(JSONUtils.setJSONType(START_CON, payload).toString());
    }

    public static TextMessage getTurnOrderMessage(int[] ids) {
        JSONObject payload = new JSONObject();
        payload.put(TURN_ORDER, ids);
        return new TextMessage(payload.toString());
    }
    //endregion

    //region endGame

    /**
     * creates message containing end game message and winner id
     *
     * @param winner
     * @return string message structured like JSON
     */
    public static TextMessage endGame(Player winner) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.NOTIFICATION, String.format(Constants.PLAYER_ID_WON_GAME, winner.getName()));
        payload.put(Constants.WINNER, winner.getId());
        return new TextMessage(JSONUtils.setJSONType(Constants.GAME_OVER, payload).toString());
    }
    //endregion

    //region error
    public static TextMessage error(String message) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.MESSAGE, message);
        return new TextMessage(JSONUtils.setJSONType(Constants.ERROR, payload).toString());
    }
    //endregion

    //region OK
    public static TextMessage OK() {
        JSONObject payload = new JSONObject();
        payload.put(Constants.SERVER_RES, Constants.OK);
        return new TextMessage(payload.toString());
    }
    //endregion

    //region costs

    /**
     * creates message containing costs
     *
     * @param playerId
     * @param overview
     * @param hideCosts
     * @return JSON structured String
     */
    public static TextMessage costs(int playerId, RawMaterialOverview overview, boolean hideCosts) {
        JSONObject costsPayload = hideCosts ? overview.toJSON_Unknown() : overview.toJSON();

        JSONObject payload = JSONUtils.setJSONType(Constants.RAW_MATERIALS, costsPayload);
        payload.put(Constants.PLAYER, playerId);

        return new TextMessage(JSONUtils.setJSONType(Constants.COSTS, payload).toString());
    }
    //endregion

    //region harvest
    public static TextMessage harvest(int playerId, RawMaterialOverview overview, boolean hideHarvest) {
        JSONObject harvestPayload = hideHarvest ? overview.toJSON_Unknown() : overview.toJSON();

        JSONObject payload = JSONUtils.setJSONType(Constants.RAW_MATERIALS, harvestPayload);
        payload.put(Constants.PLAYER, playerId);

        return new TextMessage(JSONUtils.setJSONType(Constants.HARVEST, payload).toString());
    }
    //endregion

    //region robberMoved
    public static TextMessage robberMoved(Player player, Robber robber, Player victim) {
        JSONObject payload = robber.toJSON();
        payload.put(Constants.PLAYER, player.getId());

        int id = -1;
        if (victim != null) id = victim.getId();

        payload.put(Constants.DESTINATION, id);

        return new TextMessage(JSONUtils.setJSONType(Constants.ROBBER_AT, payload).toString());
    }
    //endregion

    //region newBuilding
    public static TextMessage newBuilding(Building building) {
        return new TextMessage(
                JSONUtils.setJSONType(Constants.NEW_BUILDING, building.toJSON()).toString());
    }
    //endregion

    //region developmentCardBought
    public static TextMessage developmentCardBought(int playerId, DevCardType type) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.PLAYER, playerId);
        payload.put(Constants.DEV_CARD, type.toString());
        return new TextMessage(JSONUtils.setJSONType(CARD_SOLD,payload).toString());
    }

    public static TextMessage developmentCardBought_Public(int playerId) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.PLAYER, playerId);
        payload.put(Constants.DEV_CARD, UNKNOWN);
        return new TextMessage(JSONUtils.setJSONType(CARD_SOLD,payload).toString());
    }
    //endregion

    //region longestRoad
    public static TextMessage longestRoad(int playerId) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.PLAYER, playerId);

        return new TextMessage(JSONUtils.setJSONType(Constants.LONGEST_RD, payload).toString());
    }

    public static TextMessage longestRoad() {
        JSONObject payload = new JSONObject();
        return new TextMessage(JSONUtils.setJSONType(Constants.LONGEST_RD, payload).toString());
    }
    //endregion

    //region trade
    public static TextMessage trade(TradeRequest tr, String tradeAction) {
        JSONObject payload = tr.toJSON();
        return new TextMessage(JSONUtils.setJSONType(tradeAction, payload).toString());
    }

    public static TextMessage tradeAccept(TradeRequest tr, int acceptedBy) {
        JSONObject payload = tr.toJSON();

        payload.put(ACCEPT, tr.getAcceptedBy(acceptedBy));
        payload.put(FELLOW_PLAYER, acceptedBy);

        return new TextMessage(JSONUtils.setJSONType(TRD_ACC, payload).toString());
    }
    //endregion

    //region action cards

    public static TextMessage knightCard(int playerId, int opponentId, Location location) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.PLACE, location.toJSON());
        payload.put(Constants.DESTINATION, opponentId);
        payload.put(Constants.PLAYER, playerId);
        payload.put(Constants.TYPE, KNIGHT);

        return new TextMessage(JSONUtils.setJSONType(Constants.DEV_CARD_PLAYED, payload).toString());
    }

    public static TextMessage knightCard(int playerId, TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(Constants.CARD_KNIGHT);
        payload.put(Constants.PLAYER, playerId);
        return new TextMessage(JSONUtils.setJSONType(Constants.CARD_KNIGHT, payload).toString());
    }

    public static TextMessage roadConstructionCard(Building road1, Building road2) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.PLAYER, road1.getOwner());
        payload.put(Constants.ROAD, road1.toJSON());
        payload.put(Constants.ROAD, road2.toJSON());
        payload.put(Constants.TYPE, ROAD_CONSTRUCTION);

        return new TextMessage(JSONUtils.setJSONType(Constants.DEV_CARD_PLAYED, payload).toString());
    }

    public static TextMessage roadConstructionCard(int id) {
        JSONObject payload = new JSONObject();
        payload.put(PLAYER, id);
        payload.put(TYPE, ROAD_CONSTRUCTION);

        return new TextMessage(JSONUtils.setJSONType(Constants.DEV_CARD_PLAYED, payload).toString());
    }

    public static TextMessage monopoleCard(int playerId, RawMaterialType type) {
        JSONObject payload = new JSONObject();
        payload.put(Constants.PLAYER, playerId);
        payload.put(Constants.RAW_MATERIAL, type.toString());
        payload.put(Constants.TYPE, MONOPOLE);

        return new TextMessage(JSONUtils.setJSONType(Constants.DEV_CARD_PLAYED, payload).toString());
    }

    public static TextMessage monopoleCard(int playerId, TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(MONOPOLE);
        payload.put(Constants.PLAYER, playerId);
        return new TextMessage(JSONUtils.setJSONType(Constants.MONOPOLE, payload).toString());
    }

    public static TextMessage inventionCard(int playerId, RawMaterialOverview overview) {

        JSONObject payload = new JSONObject();
        payload.put(Constants.PLAYER, playerId);
        payload.put(Constants.RAW_MATERIALS, overview.toJSON());
        payload.put(Constants.TYPE, INVENTION);

        return new TextMessage(JSONUtils.setJSONType(Constants.DEV_CARD_PLAYED, payload).toString());
    }

    public static TextMessage inventionCard(int playerId, TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(INVENTION);
        payload.put(PLAYER, playerId);
        return new TextMessage(JSONUtils.setJSONType(INVENTION, payload).toString());
    }

    //endregion

    public static TextMessage playerLeft(int id) {
        JSONObject payload = new JSONObject();
        payload.put("Spieler ist weg", id);

        return new TextMessage(JSONUtils.setJSONType(MESSAGE, payload).toString());
    }

    public static TextMessage getBuildMessage(Building building) {
        JSONObject payload = new JSONObject();
        return new TextMessage(payload.put(BUILD, building.toJSON()).toString());
    }

    public static TextMessage chatMessage(int playerId, TextMessage message) {
        JSONObject payload = JSONUtils.createJSON(message).getJSONObject(CHAT_OUT);
        payload.put(Constants.SENDER, playerId);
        return new TextMessage(JSONUtils.setJSONType(Constants.CHAT_IN, payload).toString());

    }
}
