package de.lmu.settleBattle.catanServer;

import com.google.gson.Gson;
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
  private static Building road2;
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
  private static Board board;

  //region initialize
  @BeforeClass
  public static void initialize() {
    player1 = new Player(0);
    player1.setColor(Color.WHITE);
    player1.setName("Lucy");
    player1.setStatus(Constants.START_GAME);

    player2 = new Player(42);
    player2.setStatus(Constants.START_GAME);

    city = new Building(BuildingType.CITY, 42);
    l1 = new Location(1, 0);
    l2 = new Location(1, 1);
    l3 = new Location(2, 0);
    city.build(new Location[]{l1, l2, l3});

    road = new Building(BuildingType.ROAD, 3);
    road.build(new Location[]{l1, l3});
    road2 = new Building(BuildingType.ROAD, 3);
    road2.build(new Location[]{l1, l2});

    field = new Field(RawMaterialType.WOOL, l1, 3);
    haven = new Haven(new Location[]{l1, l2}, RawMaterialType.CLAY);
    robber = new Robber();
    rmOverview = new RawMaterialOverview(0, 0, 3, 4, 2);
    dcOverview = new DevelopmentCardOverview(0, 1, 2, 3, 4);

    tradeRequest = new TradeRequest(rmOverview, rmOverview);
    tradeRequest.setPlayerId(13);

    tradeRequest_Accepted = new TradeRequest(rmOverview, rmOverview);
    tradeRequest_Accepted.setPlayerId(2);
    tradeRequest_Accepted.accept(true,3);

    board = new Board();
  }
  //endregion

  @Test
  public void welcomeNewPlayerShouldBeAnsweredRight() throws Exception {
    JSONObject welcomeJSON = new JSONObject(CatanMessage.welcomeNewPlayer(42).getPayload());

    assertTrue(welcomeJSON.has(Constants.GET_ID));

    JSONObject welcome = welcomeJSON.getJSONObject(Constants.GET_ID);

    assertEquals(42, welcome.get("id"));
  }

  @Test
  public void endGameJSONMessageShouldBeRight() throws Exception {
    assertEquals("{\"Spiel beendet\":{\"Nachricht\":\"Spieler Lucy hat das Spiel gewonnen.\",\"Sieger\":0}}",
        JSONUtils.createJSON(CatanMessage.endGame(player1)).toString());
  }

  @Test
  public void errorMessageShouldBeAnsweredRight() throws Exception {
    JSONObject errorJSON = JSONUtils.createJSON(CatanMessage.error(Constants.COLOR_ALREADY_ASSIGNED));
    JSONObject error = errorJSON.getJSONObject(Constants.ERROR);
    assertEquals(Constants.COLOR_ALREADY_ASSIGNED,
        error.get(Constants.MESSAGE));
  }

  @Test
  public void diceRequestShouldBeAnsweredRight() throws Exception {
    assertEquals("{\"Würfelwurf\":{\"Spieler\":42,\"Wurf\":[1,1]}}",
        CatanMessage.throwDice(42, new int[]{1, 1}).getPayload());
  }

  @Test
  public void statusUpdateShouldBeBuiltRight() throws Exception {
    JSONObject statusupdate = JSONUtils.createJSON((CatanMessage.statusUpdate(player1)));
    JSONObject player = (statusupdate.getJSONObject(Constants.STATUS_UPD)).getJSONObject(Constants.PLAYER);

    assertEquals(Constants.START_GAME, player.get(Constants.PLAYER_STATE));
    assertEquals("Weis", player.get(Constants.PLAYER_COLOR)); assertEquals("Lucy", player.get(Constants.PLAYER_NAME));
    assertEquals(0, player.get(Constants.PLAYER_ID));
  }

  //region buildingMessagesShouldBeRight
  @Test
  public void buildingMessagesShouldBeRight() throws Exception {
    assertEquals("{\"Eigentümer\":42,\"Typ\":\"Stadt\"," +
            "\"Ort\":[{\"x\":1,\"y\":0},{\"x\":1,\"y\":1},{\"x\":2,\"y\":0}]}",
        city.toJSONString());

    JSONObject newBuildingJSON = JSONUtils.createJSON(CatanMessage.newBuilding(city));
    JSONObject newBuilding = newBuildingJSON.getJSONObject(Constants.NEW_BUILDING);

    assertTrue(newBuilding.has(Constants.TYPE));

    assertEquals(42, newBuilding.get(Constants.OWNER));
    assertEquals(Constants.CITY, newBuilding.get(Constants.TYPE));

    JSONArray buildingLoc = newBuilding.getJSONArray(Constants.PLACE);
    assertEquals(3, buildingLoc.length());

    assertEquals(1, buildingLoc.getJSONObject(0).get("x"));
    assertEquals(1, buildingLoc.getJSONObject(1).get("y"));
    assertEquals(2, buildingLoc.getJSONObject(2).get("x"));
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
    JSONObject robber = robberJSON.getJSONObject(Constants.PLACE);

    assertEquals(robber.get("x"), 0);
    assertEquals(robber.get("y"), 0);

    robberJSON = JSONUtils.createJSON(CatanMessage.robberMoved(player1, JSONUtilsTest.robber, player2));

    assertTrue(robberJSON.has(Constants.ROBBER_AT));

    robber = robberJSON.getJSONObject(Constants.ROBBER_AT);

    assertEquals(0, robber.get(Constants.PLAYER));
    assertEquals(42, robber.get(Constants.DESTINATION));
    assertTrue(robber.has(Constants.PLACE));
  }
  //endregion

  //region rawMaterialOverviewJSONObjectShouldBeRight
  @Test
  public void rawMaterialOverviewJSONObjectShouldBeRight() throws Exception {
    JSONObject rmJSON = rmOverview.toJSON();

    assertEquals(3, rmJSON.get(Constants.WOOD));
    assertEquals(0, rmJSON.get(Constants.CLAY));
    assertEquals(4, rmJSON.get(Constants.WOOL));
    assertEquals(2, rmJSON.get(Constants.WHEAT));
    assertEquals(0, rmJSON.get(Constants.ORE));

    JSONObject rmJSON_Unknown = rmOverview.toJSON_Unknown();
    assertEquals(9, rmJSON_Unknown.get(Constants.UNKNOWN));

    //test costs
    JSONObject costsHiddenJSON = JSONUtils.createJSON(
        CatanMessage.costs(42, rmOverview, true));
    JSONObject costsHidden = (costsHiddenJSON.getJSONObject(Constants.COSTS));

    assertEquals(9, (costsHidden.getJSONObject(Constants.RAW_MATERIALS))
        .get(Constants.UNKNOWN));
    assertEquals(42, costsHidden.get(Constants.PLAYER));

    JSONObject costsJSON = JSONUtils.createJSON(CatanMessage.costs(42, rmOverview, false));
    JSONObject costs = costsJSON.getJSONObject(Constants.COSTS);

    assertEquals(42, costs.get(Constants.PLAYER));
    assertTrue(costs.has(Constants.RAW_MATERIALS));

    //test harvest
    JSONObject harvestJSON = JSONUtils.createJSON(CatanMessage.harvest(13, rmOverview, false));
    JSONObject harvest = harvestJSON.getJSONObject(Constants.HARVEST);

    assertEquals(13, harvest.get(Constants.PLAYER));
    assertTrue(harvest.has(Constants.RAW_MATERIALS));

    JSONObject harvestHiddenJSON = JSONUtils.createJSON(CatanMessage.harvest(2, rmOverview, true));
    JSONObject harvestHidden = harvestHiddenJSON.getJSONObject(Constants.HARVEST);
    assertEquals(9, (harvestHidden.getJSONObject(Constants.RAW_MATERIALS))
        .get(Constants.UNKNOWN));
    assertEquals(2, harvestHidden.get(Constants.PLAYER));
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
        JSONUtils.getMessageType(CatanMessage.throwDice(0, new int[]{1, 1})));

    assertEquals(Constants.START_CON,
        JSONUtils.getMessageType(CatanMessage.startGame(new Board())));
  }
  //endregion

  @Test
  public void developmentCardBoughtShouldBeCorrect() throws Exception {
    JSONObject payload = JSONUtils.createJSON(CatanMessage.developmentCardBought(4, DevCardType.KNIGHT));

    assertEquals(4, payload.get(Constants.PLAYER));
    assertEquals(Constants.KNIGHT, payload.get(Constants.CARD_BUY));
  }

  //region longestRoadMessageShouldBeCorrect
  @Test
  public void longestRoadMessageShouldBeCorrect() throws Exception {
    JSONObject emptyRoadJSON = JSONUtils.createJSON(CatanMessage.longestRoad());
    JSONObject playerRoadJSON = JSONUtils.createJSON(CatanMessage.longestRoad(13));

    assertTrue(!emptyRoadJSON.getJSONObject(Constants.LONGEST_RD).has(Constants.PLAYER));

    JSONObject playerRoad = playerRoadJSON.getJSONObject(Constants.LONGEST_RD);
    assertTrue(playerRoad.has(Constants.PLAYER));
    assertEquals(13, playerRoad.get(Constants.PLAYER));
  }
  //endregion

  //region tradeMessageShouldBeCorrect
  @Test
  public void tradeMessagesShouldBeCorrect() throws Exception {

    //test new trade request
    JSONObject tradeRequestJSON = JSONUtils.createJSON(CatanMessage.trade(tradeRequest, Constants.TRD_OFFER));

    JSONObject request = tradeRequestJSON.getJSONObject(Constants.TRD_OFFER);
    assertEquals(13, request.get(Constants.PLAYER));
    assertEquals(tradeRequest.getId(), request.get(Constants.TRADE_ID));

    assertTrue(request.has(Constants.OFFER));
    assertTrue(request.has(Constants.REQUEST));

    //test accepted trade request
    JSONObject tradeAcceptedJSON = JSONUtils.createJSON(CatanMessage.trade(tradeRequest_Accepted, Constants.TRD_ACC));
    JSONObject trAccepted = tradeAcceptedJSON.getJSONObject(Constants.TRD_ACC);

    assertEquals(tradeRequest_Accepted.getId(), trAccepted.get(Constants.TRADE_ID));
    assertTrue(tradeRequest_Accepted.getAcceptedBy(3));
    assertTrue(tradeRequest_Accepted.isAccepted());

    //test performed trade request
    tradeRequest_Accepted.execute(3);

    JSONObject tr = tradeRequest_Accepted.toJSON();

    assertEquals((Integer)tr.get(Constants.FELLOW_PLAYER), tradeRequest_Accepted.getExecutedWith());
    assertTrue(tradeRequest_Accepted.isExecuted());

    //test cancelled trade request
    tradeRequest.cancel();

    JSONObject tradeCancelledJSON = JSONUtils.createJSON(CatanMessage.trade(tradeRequest, Constants.TRD_ABORTED));
    JSONObject trCancelled = tradeCancelledJSON.getJSONObject(Constants.TRD_ABORTED);

    assertEquals(tradeRequest.getId(), trCancelled.get(Constants.TRADE_ID));
    assertEquals(tradeRequest.getPlayerId(), trCancelled.get(Constants.PLAYER));
  }
  //endregion

  //region knightCardMessageShouldBeCorrect
  @Test
  public void knightCardMessageShouldBeCorrect() throws Exception {
    JSONObject knightJSON = JSONUtils.createJSON(CatanMessage.knightCard(1, 4, l1));
    JSONObject knight = knightJSON.getJSONObject(Constants.CARD_KNIGHT);

    assertEquals(1, knight.get(Constants.PLAYER));
    assertEquals(4, knight.get(Constants.DESTINATION));
    assertTrue(knight.has(Constants.PLACE));
  }
  //endregion

  //region roadConstructionCardShouldBeCorrects
  @Test
  public void roadConstructionCardShouldBeCorrect() throws Exception {
    JSONObject roadJSON = JSONUtils.createJSON(CatanMessage.roadConstructionCard(road,road2));
    JSONObject road = roadJSON.getJSONObject(Constants.CARD_RD_CON);

    assertTrue(road.has(Constants.PLAYER));
    assertTrue(road.has(Constants.ROAD));
    assertTrue(road.getJSONObject(Constants.ROAD)
        .getJSONArray(Constants.PLACE).length() == 2);
  }
  //endregion

  @Test
  public void monopoleCardShouldBeCorrect() throws Exception {
    JSONObject monopoleJSON = JSONUtils.createJSON(CatanMessage.monopoleCard(0, RawMaterialType.WHEAT));
    JSONObject monopole = monopoleJSON.getJSONObject(Constants.MONOPOLE);

    assertTrue(monopole.has(Constants.PLAYER));
    assertEquals(Constants.WHEAT, monopole.get(Constants.RAW_MATERIAL));
  }

  @Test
  public void inventionCardShouldBeCorrect() throws Exception {
    JSONObject inventionJSON = JSONUtils.createJSON(CatanMessage.inventionCard(5, rmOverview));
    JSONObject invention = inventionJSON.getJSONObject(Constants.INVENTION);

    assertTrue(invention.has(Constants.PLAYER));
    assertTrue(invention.has(Constants.RAW_MATERIALS));
    assertEquals(2, invention.getJSONObject(Constants.RAW_MATERIALS).get(Constants.WHEAT));
  }

  @Test
  public void startGameShouldBeBuiltRight() throws Exception {
    JSONObject startGameJSON = JSONUtils.createJSON(CatanMessage.startGame(board));
    JSONObject board = startGameJSON.getJSONObject(Constants.START_CON).getJSONObject(Constants.CARD);

    JSONArray fieldsJSON = board.getJSONArray(Constants.FIELDS);
    JSONArray havensJSON = board.getJSONArray(Constants.HAVEN);
    JSONObject robberJSON = board.getJSONObject(Constants.ROBBER);
    JSONArray roadsJSON = board.getJSONArray(Constants.ROAD);
    JSONArray settlementsJSON = board.getJSONArray(Constants.SETTLEMENT);
    JSONArray citiesJSON = board.getJSONArray(Constants.CITY);

    assertTrue(robberJSON.has(Constants.PLACE));

    Gson gson = new Gson();
    Field[] fields = gson.fromJson(fieldsJSON.toString(), Field[].class);
    Haven[] havens = gson.fromJson(havensJSON.toString(), Haven[].class);
    Building[] roads = gson.fromJson(roadsJSON.toString(), Building[].class);
    Building[] settlements = gson.fromJson(settlementsJSON.toString(), Building[].class);
    Building[] cities = gson.fromJson(citiesJSON.toString(), Building[].class);

    assertEquals(19, fields.length);
    assertEquals(9, havens.length);
    assertEquals(0, roads.length);
    assertEquals(0, settlements.length);
    assertEquals(0, cities.length);


    assertTrue(fields[18].getLocation().x == 0 &&
        fields[18].getLocation().y == 0 &&
        fields[18].getHarvest() == RawMaterialType.DESERT);
  }
}
