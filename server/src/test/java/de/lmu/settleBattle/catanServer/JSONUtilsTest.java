package de.lmu.settleBattle.catanServer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSONUtilsTest {
    private static Player player1;
    private static Player player2;
    private static Building city;
    private static Building road;
    private static Field field;
    private static Haven haven;
    private static Robber robber;
    private static RawMaterialOverview rmOverview;
    private static DevelopmentCardOverview dcOverview;
    private static TradeRequest tradeRequest;
    private static TradeRequest tradeRequest_Accepted;
    private static Location l1;
    private static Location l2;
    private static Location l3;

    //region initialize
    @BeforeClass
    public static void initialize()
    {
        player1 = new Player(0);
        player1.setColor(Color.WHITE);
        player1.setName("Lucy");
        player1.setStatus(Constants.START_GAME);

        player2 = new Player(42);
        player2.setStatus(Constants.START_GAME);

        city = new Building(BuildingType.CITY, 42);
        l1 = new Location(1,0);
        l2 = new Location(1,1);
        l3 = new Location(2,0);
        city.build(new Location[] {l1, l2, l3});

        road = new Building(BuildingType.ROAD, 3);
        road.build(new Location[] {l1, l3});

        field = new Field(l1, RawMaterialType.WOOL, 3);
        haven = new Haven(new Location[] {l1,l2}, RawMaterialType.CLAY);
        robber = new Robber();
        rmOverview = new RawMaterialOverview(0,0,3,4,2);
        dcOverview = new DevelopmentCardOverview(0,1,2,3,4);

        tradeRequest = new TradeRequest(rmOverview,rmOverview);
        tradeRequest.setPlayerId(13);

        tradeRequest_Accepted = new TradeRequest(rmOverview, rmOverview);
        tradeRequest_Accepted.setPlayerId(2);
        tradeRequest_Accepted.accept(3);
    }
    //endregion

    @Test
    public void welcomeNewPlayerShouldBeAnsweredRight() throws Exception {
        JSONObject json = new JSONObject(CatanMessage.welcomeNewPlayer(42).getPayload());

        assertTrue(json.has(Constants.GET_ID));

        JSONObject innerJSON = json.getJSONObject(Constants.GET_ID);

        assertEquals(42, innerJSON.get("id"));
    }

    @Test
    public void endGameJSONMessageShouldBeRight() throws Exception {
        assertEquals("{\"Spiel beendet\":{\"Nachricht\":\"Spieler Lucy hat das Spiel gewonnen.\",\"Sieger\":0}}",
                JSONUtils.createJSON(CatanMessage.endGame(player1)).toString());
    }

    @Test
    public void errorMessageShouldBeAnsweredRight() throws Exception {
        JSONObject errorJSON = JSONUtils.createJSON(CatanMessage.error(Constants.COLOR_ALREADY_ASSIGNED));
        JSONObject innerError = errorJSON.getJSONObject(Constants.ERROR);
        assertEquals(Constants.COLOR_ALREADY_ASSIGNED,
                innerError.get(Constants.MESSAGE));
    }

    @Test
    public void diceRequestShouldBeAnsweredRight() throws Exception {
        assertEquals("{\"Würfelwurf\":{\"Spieler\":42,\"Wurf\":[1,1]}}",
                CatanMessage.throwDice(42, new int[]{1,1}).getPayload());
    }

    @Test
    public void statusUpdateShouldBeBuiltRight() throws Exception {
        JSONObject json = JSONUtils.createJSON((CatanMessage.statusUpdate(player1)));
        JSONObject playerJSON = (json.getJSONObject(Constants.STATUS_UPD)).getJSONObject(Constants.PLAYER);

        assertEquals(Constants.START_GAME, playerJSON.get(Constants.PLAYER_STATE));
        assertEquals("Weiß", playerJSON.get(Constants.PLAYER_COLOR));
        assertEquals("Lucy", playerJSON.get(Constants.PLAYER_NAME));
        assertEquals(0, playerJSON.get(Constants.PLAYER_ID));
    }

    //region buildingMessagesShouldBeRight
    @Test
    public void buildingMessagesShouldBeRight() throws Exception {
        assertEquals("{\"Eigentümer\":42,\"Typ\":\"Stadt\"," +
                        "\"Ort\":[{\"x\":1,\"y\":0},{\"x\":1,\"y\":1},{\"x\":2,\"y\":0}]}",
                city.toJSONString());

        JSONObject json = JSONUtils.createJSON(CatanMessage.newBuilding(city));
        JSONObject innerObject = json.getJSONObject(Constants.NEW_BUILDING);

        assertTrue(innerObject.has(Constants.TYPE));

        assertEquals(42, innerObject.get(Constants.OWNER));
        assertEquals(Constants.CITY, innerObject.get(Constants.TYPE));

        JSONArray buildingLocation = innerObject.getJSONArray(Constants.PLACE);
        assertEquals(3, buildingLocation.length());

        assertEquals(1, buildingLocation.getJSONObject(0).get("x"));
        assertEquals(1, buildingLocation.getJSONObject(1).get("y"));
        assertEquals(2, buildingLocation.getJSONObject(2).get("x"));
    }
    //endregion

    @Test
    public void fieldJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Ort\":{\"x\":1,\"y\":0},\"Typ\":\"Wolle\",\"Zahl\":3}",
                field.toJSONString());
    }

    @Test
    public void havenJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Ort\":[{\"x\":1,\"y\":0},{\"x\":1,\"y\":1}],\"Typ\":\"Lehm Hafen\"}",
                haven.toJSONString());
    }

    //region robberMessagesShouldBeCorrect
    @Test
    public void robberMessagesShouldBeCorrect() throws Exception {
        JSONObject robberJSON = robber.toJSON();

        assertTrue(robberJSON.has(Constants.PLACE));
        JSONObject innerJSON = robberJSON.getJSONObject(Constants.PLACE);

        assertEquals(innerJSON.get("x"),0);
        assertEquals(innerJSON.get("y"),0);

        robberJSON = JSONUtils.createJSON(CatanMessage.robberMoved(player1, robber, player2));

        assertTrue(robberJSON.has(Constants.ROBBER_AT));

        innerJSON = robberJSON.getJSONObject(Constants.ROBBER_AT);

        assertEquals(0, innerJSON.get(Constants.PLAYER));
        assertEquals(42, innerJSON.get(Constants.DESTINATION));
        assertTrue(innerJSON.has(Constants.PLACE));
    }
    //endregion

    //region rawMaterialOverviewJSONObjectShouldBeRight
    @Test
    public void rawMaterialOverviewJSONObjectShouldBeRight() throws Exception {
        JSONObject rmJSON = rmOverview.toJSON();

        assertEquals(3, rmJSON.get(Constants.WOOD));
        assertEquals(0, rmJSON.get(Constants.CLAY));
        assertEquals(4, rmJSON.get(Constants.WOOL));
        assertEquals(2, rmJSON.get(Constants.WEAT));
        assertEquals(0, rmJSON.get(Constants.ORE));

        JSONObject rmJSON_Unknown = rmOverview.toJSON_Unknown();
        assertEquals(9, rmJSON_Unknown.get(Constants.UNKNOWN));

        //test costs
        JSONObject costsHidden = JSONUtils.createJSON(
                CatanMessage.costs(42, rmOverview, true));
        JSONObject innerCostsHidden = (costsHidden.getJSONObject(Constants.COSTS));

        assertEquals(9, (innerCostsHidden.getJSONObject(Constants.RAW_MATERIALS))
                .get(Constants.UNKNOWN));
        assertEquals(42, innerCostsHidden.get(Constants.PLAYER));

        JSONObject costs = JSONUtils.createJSON(CatanMessage.costs(42, rmOverview, false));
        JSONObject innerCost = costs.getJSONObject(Constants.COSTS);

        assertEquals(42, innerCost.get(Constants.PLAYER));
        assertTrue(innerCost.has(Constants.RAW_MATERIALS));

        //test harvest
        JSONObject harvest = JSONUtils.createJSON(CatanMessage.harvest(13, rmOverview, false));
        JSONObject innerHarvest = harvest.getJSONObject(Constants.HARVEST);

        assertEquals(13, innerHarvest.get(Constants.PLAYER));
        assertTrue(innerHarvest.has(Constants.RAW_MATERIALS));

        JSONObject harvestHidden = JSONUtils.createJSON(CatanMessage.harvest(2, rmOverview, true));
        JSONObject innerHarvestHidden = harvestHidden.getJSONObject(Constants.HARVEST);
        assertEquals(9, (innerHarvestHidden.getJSONObject(Constants.RAW_MATERIALS))
                .get(Constants.UNKNOWN));
        assertEquals(2, innerHarvestHidden.get(Constants.PLAYER));
    }

    //endregion

    //region developmentCardOverviewJSONObjectShouldBeRight
    @Test
    public void developmentCardOverviewJSONObjectShouldBeRight() throws Exception {
        JSONObject dcJSON = dcOverview.toJSON();

        assertEquals(0, dcJSON.get(Constants.KNIGHT));
        assertEquals(1, dcJSON.get(Constants.ROAD_CONSTRUCTION));
        assertEquals(2, dcJSON.get(Constants.MONOPOLE));
        assertEquals(3, dcJSON.get(Constants.INVENTION));
        assertEquals(4, dcJSON.get(Constants.VICTORY_PT));

        assertEquals("{\"Unbekannt\":10}",
                dcOverview.toJSONString_Unknown());
    }
    //endregion

    //region getMessageTypeShouldReturnCorrectType
    @Test
    public void getMessageTypeShouldReturnCorrectType() throws Exception {
        assertEquals(Constants.ERROR,
                JSONUtils.getMessageType(CatanMessage.error("")));

        assertEquals(Constants.HANDSHAKE,
                JSONUtils.getMessageType(CatanMessage.serverProtocol()));

        assertEquals(Constants.DICE_RESULT,
                JSONUtils.getMessageType(CatanMessage.throwDice(0, new int[] {1,1})));
    }
    //endregion

    @Test
    public void developmentCardBoughtShouldBeCorrect() throws Exception {
        JSONObject json = JSONUtils.createJSON(CatanMessage.developmentCardBought(4, DevCardType.KNIGHT));

        assertEquals(4, json.get(Constants.PLAYER));
        assertEquals(Constants.KNIGHT, json.get(Constants.CARD_BUY));
    }

    //region longestRoadMessageShouldBeCorrect
    public void longestRoadMessageShouldBeCorrect() throws Exception {
        JSONObject emptyRoadJSON = JSONUtils.createJSON(CatanMessage.longestRoad());
        JSONObject playerRoadJSON = JSONUtils.createJSON(CatanMessage.longestRoad(13));

        assertTrue(!emptyRoadJSON.getJSONObject(Constants.LONGEST_RD).has(Constants.PLAYER));

        JSONObject innerJSON = playerRoadJSON.getJSONObject(Constants.LONGEST_RD);
        assertTrue(innerJSON.has(Constants.PLAYER));
        assertEquals(13, innerJSON.get(Constants.PLAYER));
    }
    //endregion

    //region tradeMessageShouldBeCorrect
    @Test
    public void tradeMessagesShouldBeCorrect() throws Exception {

        //test new trade request
        JSONObject tradeRequestJSON = JSONUtils.createJSON(CatanMessage.newTradeRequest(tradeRequest));

        JSONObject innerRequest = tradeRequestJSON.getJSONObject(Constants.TRD_OFFER);
        assertEquals(13, innerRequest.get(Constants.PLAYER));
        assertEquals(tradeRequest.getId(), innerRequest.get(Constants.TRADE_ID));

        assertTrue(innerRequest.has(Constants.OFFER));
        assertTrue(innerRequest.has(Constants.REQUEST));

        //test accepted trade request
        JSONObject tradeAcceptedJSON = JSONUtils.createJSON(CatanMessage.tradeRequestAccepted(tradeRequest_Accepted));
        JSONObject innerAccepted = tradeAcceptedJSON.getJSONObject(Constants.TRD_ACC);

        assertEquals(tradeRequest_Accepted.getId(), innerAccepted.get(Constants.TRADE_ID));
        assertEquals(tradeRequest_Accepted.getAcceptedBy(), innerAccepted.get(Constants.FELLOW_PLAYER));
        assertEquals(tradeRequest_Accepted.isAccepted(), innerAccepted.get(Constants.ACCEPT));

        //test performed trade request
        JSONObject tradePerformedJSON = JSONUtils.createJSON(CatanMessage.tradePerformed(tradeRequest_Accepted));
        JSONObject innerPerformed = tradePerformedJSON.getJSONObject(Constants.TRD_FIN);

        assertEquals(tradeRequest_Accepted.getAcceptedBy(), innerPerformed.get(Constants.FELLOW_PLAYER));
        assertEquals(tradeRequest_Accepted.getPlayerId(), innerPerformed.get(Constants.PLAYER));

        //test cancelled trade request
        tradeRequest.cancel();

        JSONObject tradeCancelledJSON = JSONUtils.createJSON(CatanMessage.tradeCancelled(tradeRequest));

        JSONObject innerCancelled = tradeCancelledJSON.getJSONObject(Constants.TRD_ABORTED);

        assertEquals(tradeRequest.getId(), innerCancelled.get(Constants.TRADE_ID));
        assertEquals(tradeRequest.getPlayerId(), innerCancelled.get(Constants.PLAYER));
    }
    //endregion

    //region knightCardMessageShouldBeCorrect
    public void knightCardMessageShouldBeCorrect() throws Exception {
        //{ "Ritter ausspielen" : { "Ort" : { "x" : 0, "y" : 1 }, "Ziel" : 13, "Spieler" : 42 } }

        JSONObject knightJSON = JSONUtils.createJSON(CatanMessage.knightCard(1,4, l1));
        JSONObject innerKnight = knightJSON.getJSONObject(Constants.CARD_KNIGHT);

        assertEquals(1, innerKnight.get(Constants.PLAYER));
        assertEquals(4, innerKnight.get(Constants.DESTINATION));
        assertTrue(innerKnight.has(Constants.PLACE));
    }
    //endregion

    //region roadConstructionCardShouldBeCorrects
    public void roadConstructionCardShouldBeCorrect() throws Exception {
        // { "Straßenbaukarte ausspielen" : { "Spieler" : 42, "Straße1" : [ {"x" : -2, "y" : 0 },{ "x" : -1, "y" : 0 } ] } }

        JSONObject roadJSON = JSONUtils.createJSON(CatanMessage.roadContructionCard(road));
        JSONObject innerRoad = roadJSON.getJSONObject(Constants.CARD_RD_CON);

        assertTrue(innerRoad.has(Constants.PLAYER));
        assertTrue(innerRoad.has(Constants.ROAD));
        assertTrue(innerRoad.getJSONArray(Constants.ROAD).length() == 2);
    }
    //endregion

    @Test
    public void monopoleCardShouldBeCorrect() throws Exception {
        JSONObject monopoleJSON = JSONUtils.createJSON(CatanMessage.monopoleCard(0, RawMaterialType.WEAT));
        JSONObject innerMonopole = monopoleJSON.getJSONObject(Constants.MONOPOLE);

        assertTrue(innerMonopole.has(Constants.PLAYER));
        assertEquals(Constants.WEAT, innerMonopole.get(Constants.RAW_MATERIAL));
    }

    @Test
    public void inventionCardShouldBeCorrect() throws Exception {
        JSONObject inventionJSON = JSONUtils.createJSON(CatanMessage.inventionCard(5, rmOverview));
        JSONObject innerInvention = inventionJSON.getJSONObject(Constants.INVENTION);

        assertTrue(innerInvention.has(Constants.PLAYER));
        assertTrue(innerInvention.has(Constants.RAW_MATERIALS));
        assertEquals(2,innerInvention.getJSONObject(Constants.RAW_MATERIALS).get(Constants.WEAT));
    }

   /* @Test
    public void startGameShouldBeBuiltRight() throws Exception {
        assertEquals("{\"Spielgestartet\":{\"Karte\":{\n" +
                        "\"Felder\" : [ ], // Array von Feldern " +
                        "\"Gebäude\" : [ ], // Array von Gebäuden " +
                        "\"Häfen\" : [ ], // Array von Häfen " +
                        "\"Räuber\" : { // Feldposition des Räubers \"x\" : 0, \"y\" : 0 }\n" +
                        "}}}",
                jsonUtils.startGame(new RawMaterialOverview(1,1,0,0,0)).replace(" ",""));
    }
*/

}