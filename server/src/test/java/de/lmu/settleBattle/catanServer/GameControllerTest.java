package de.lmu.settleBattle.catanServer;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static de.lmu.settleBattle.catanServer.Constants.*;
import static org.junit.Assert.*;

public class GameControllerTest {
    private CatanSocketHandler handler;
    private GameController gameController;
    private Player player1;
    private Player player2;
    private Player player3;

    @Before
    public void setUp() throws Exception {
        handler = new CatanSocketHandler();
        gameController = handler.getGameCtrl();

        player1 = new Player(1);
        player1.setName("name1");
        player1.setColor(Color.ORANGE);

        player2 = new Player(2);
        player2.setName("name2");
        player2.setColor(Color.BLUE);

        player3 = new Player(3);
        player3.setName("name3");
        player3.setColor(Color.WHITE);

        gameController.addPlayer(player1);
        gameController.addPlayer(player2);
        gameController.addPlayer(player3);

        for (Player player : gameController.getPlayers())
            player.setStatus(Constants.WAIT_FOR_GAME_START);

    }

    @Test
    public void isValidName() throws Exception {

        assertTrue(gameController.isValidName("new name"));
        assertTrue(gameController.isValidColor(Color.RED));

        assertTrue(!gameController.isValidName(player1.getName()));
        assertTrue(!gameController.isValidColor(player1.getColor()));
    }

    @Test
    public void setPlayerActive() throws Exception {
        gameController.activatePlayer(player1, Constants.BUILD_SETTLEMENT);

        assertTrue(gameController.getPlayer(1).getStatus() == Constants.BUILD_SETTLEMENT);
        assertTrue(gameController.getPlayer(2).getStatus() == Constants.WAIT);
        assertTrue(gameController.getPlayer(3).getStatus() == Constants.WAIT);
    }

    @Test
    public void readyToStartGame() throws Exception {

        assertTrue(gameController.readyToStartGame());

        player1.setStatus(Constants.WAIT);

        assertTrue(!gameController.readyToStartGame());
    }

    public void initializeGame() {
        gameController.startGame();
    }

    @Test
    public void startGame() throws Exception {
        initializeGame();

        List<Player> list = gameController.getPlayers();

        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());
        assertTrue(list.get(0).getStatus().equals(Constants.BUILD_SETTLEMENT));
        assertTrue(list.get(1).getStatus().equals(Constants.WAIT));
        assertTrue(list.get(2).getStatus().equals(Constants.WAIT));

        nextMove();
    }

    //region nextMove
    public void nextMove() throws Exception {
        List<Player> list = gameController.getPlayers();

        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(1).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(2).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(2).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(1).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(1).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(2).getId() == gameController.getCurrent().getId());

        gameController.nextMove();
        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());
    }
    //endregion

    public boolean robberActivated(int id, int opponentId, Location newRobberLoc) {
        boolean activated;

        try {
            gameController.activateRobber(id, opponentId, newRobberLoc);
            activated = true;
        } catch (CatanException ex) {
            activated = false;
        }

        return activated;
    }

    //region moveRobber
    @Test
    public void moveRobber() throws CatanException {
        int currentId = gameController.getCurrent().getId();

        //board contains no settlements so there is no one to rob
        assertTrue(robberActivated(currentId, 7, new Location(-1, 0)));

        //only one player can be robbed, target is specified
        int robbedId = currentId == 2 ? 3 : 2;
        gameController.getPlayer(robbedId).setRawMaterialDeck(new RawMaterialOverview(0, 0, 2, 0, 0));

        Building s1 = new Building(robbedId, BuildingType.SETTLEMENT, new Location[]{new Location(2, 0), new Location(1, 1), new Location(2, 1)});
        gameController.getBoard().addSettlement(s1);

        assertTrue(robberActivated(currentId, robbedId, new Location(2, 0)));
        assertTrue(gameController.getPlayer(robbedId).getRawMaterialCount() == 1);

        //only one player can be robbed, target is not specified
        Building s2 = new Building(robbedId, BuildingType.SETTLEMENT, new Location[]{new Location(-3, 0), new Location(-2, 0), new Location(-2, -1)});
        gameController.getBoard().addSettlement(s2);

        assertTrue(robberActivated(currentId, 7, new Location(-2, 0)));
        assertTrue(gameController.getPlayer(robbedId).getRawMaterialCount() == 0);

        int thirdId = currentId == 1 ? 3 : 1;

        //more players can be robbed, target is not specified
        Building s3 = new Building(robbedId, BuildingType.SETTLEMENT, new Location[]{new Location(-1, 1), new Location(0, 0), new Location(-1, 0)});
        Building s4 = new Building(thirdId, BuildingType.SETTLEMENT, new Location[]{new Location(1, -1), new Location(0, 0), new Location(1, 0)});
        gameController.getBoard().addSettlement(s3);
        gameController.getBoard().addSettlement(s4);
        Player p1 = gameController.getPlayer(robbedId);
        Player p2 = gameController.getPlayer(thirdId);
        p1.setRawMaterialDeck(new RawMaterialOverview(1, 1, 1, 0, 1));
        p2.setRawMaterialDeck(new RawMaterialOverview(2, 0, 2, 0, 0));

        int robbedRmCount = p1.getRawMaterialCount();
        int thirdRmCount = p2.getRawMaterialCount();

        assertTrue(robberActivated(currentId, 7, new Location(0, 0)));

        //one of them got robbed
        assertTrue(p1.getRawMaterialCount() == robbedRmCount - 1 || p2.getRawMaterialCount() == thirdRmCount - 1);
        assertFalse(p1.getRawMaterialCount() == robbedRmCount - 1 && p2.getRawMaterialCount() == thirdRmCount - 1);

        //do not rob own player, robber will be moved but no raw materials will be de/increased
        Building s5 = new Building(currentId, BuildingType.SETTLEMENT, new Location[]{new Location(-1, -1), new Location(0, -1), new Location(0, -2)});
        Building s6 = new Building(currentId, BuildingType.SETTLEMENT, new Location[]{new Location(0, -2), new Location(0, -3), new Location(1, -3)});
        gameController.getBoard().addSettlement(s5);
        gameController.getBoard().addSettlement(s6);

        int currentRmCount = gameController.getCurrent().getRawMaterialCount();
        assertTrue(robberActivated(currentId, -1, new Location(0, -2)));
        assertTrue(currentRmCount == gameController.getCurrent().getRawMaterialCount());
    }
    //endregion


    //region KI
    @Test
    public void KI_initialPhase() throws Exception {
        gameController.setKI(player1);
        player1.addPropertyChangeListener(handler);
        gameController.setKI(player2);
        player2.addPropertyChangeListener(handler);

        Player player4 = new Player(4, true);
        player4.setName("Hering");
        player4.setColor(Color.RED);

        gameController.addPlayer(player4);
        player4.addPropertyChangeListener(handler);
        player4.setStatus(START_GAME);

        assertEquals(WAIT_FOR_GAME_START, player4.getStatus());

        initializeGame();

        //after game initialization game is started an KIs place buildings until player3 has turn
        assertEquals(BUILD_SETTLEMENT, player3.getStatus());
        assertEquals(WAIT, player4.getStatus());
        assertEquals(WAIT, player2.getStatus());
        assertEquals(WAIT, player1.getStatus());

        //player 3 places his first settlement and afterwards should have status BUILD_STREET
        int sCnt = gameController.getBoard().getSettlements().size();
        int rCnt = gameController.getBoard().getRoads().size();
        Building s1 = new Building(player3.getId(), BuildingType.SETTLEMENT, gameController.getBoard().getRandomFreeSettlementLoc());
        gameController.placeBuilding(s1);

        assertEquals(sCnt + 1, gameController.getBoard().getSettlements().size());
        assertEquals(rCnt, gameController.getBoard().getRoads().size());
        assertEquals(0, gameController.getBoard().getCities().size());

        assertEquals(BUILD_STREET, player3.getStatus());
        assertEquals(WAIT, player4.getStatus());
        assertEquals(WAIT, player2.getStatus());
        assertEquals(WAIT, player1.getStatus());

        //player 3 places his second road
        Building r1 = new Building(player3.getId(), BuildingType.ROAD, gameController.getBoard()
                .getFreeRoadLoc(player3, gameController.isInitialPhaseActive()));
        gameController.placeBuilding(r1);

        assertEquals(BUILD_SETTLEMENT, player3.getStatus());
        assertEquals(WAIT, player4.getStatus());
        assertEquals(WAIT, player2.getStatus());
        assertEquals(WAIT, player1.getStatus());


        //player 3 places his second settlement
        sCnt = gameController.getBoard().getSettlements().size();
        rCnt = gameController.getBoard().getRoads().size();
        Building s2 = new Building(player3.getId(), BuildingType.SETTLEMENT, gameController.getBoard().getRandomFreeSettlementLoc());
        gameController.placeBuilding(s2);

        assertEquals(sCnt + 1, gameController.getBoard().getSettlements().size());
        assertEquals(rCnt, gameController.getBoard().getRoads().size());
        assertEquals(0, gameController.getBoard().getCities().size());

        assertEquals(BUILD_STREET, player3.getStatus());
        assertEquals(WAIT, player4.getStatus());
        assertEquals(WAIT, player2.getStatus());
        assertEquals(WAIT, player1.getStatus());

        //player 3 places his second road
        Building r2 = new Building(player3.getId(), BuildingType.ROAD, gameController.getBoard()
                .getFreeRoadLoc(player3, gameController.isInitialPhaseActive()));
        assertTrue(gameController.placeBuilding(r2));

        assertTrue(gameController.getBoard().getSettlements().size() >= 8);
        assertTrue(gameController.getBoard().getRoads().size() >= 8);
        assertFalse(gameController.isInitialPhaseActive());

        assertEquals(DICE, player3.getStatus());
        assertEquals(WAIT, player4.getStatus());
        assertEquals(WAIT, player2.getStatus());
        assertEquals(WAIT, player1.getStatus());

        //player 3 must dice and his status will be set to TRADE_OR_BUILD
        gameController.dice(player3.getId());

        assertTrue(player3.getStatus().equals(TRADE_OR_BUILD) || player3.getStatus().equals(ROBBER_TO));
    }

    @Test
    public void KI_statusupdates() {
        gameController.setKI(player3);
        player3.addPropertyChangeListener(handler);

        player3.setRawMaterialDeck(new RawMaterialOverview(1, 4, 5, 3, 0));
        player3.setStatus(EXTRACT_CARDS_DUE_TO_ROBBER);

        assertEquals(7, player3.getRawMaterialCount());

        Location loc = gameController.getBoard().getRobber().getLocation();

        Player player = gameController.getCurrent();
        gameController.setKI(player);
        player.addPropertyChangeListener(handler);
        gameController.activatePlayer(player, ROBBER_TO);

        assertFalse(loc.equals(gameController.getBoard().getRobber().getLocation()));
    }
    //endregion

    @Test
    public void rawMaterialDistribution_InitialPhase() throws CatanException {

        initializeGame();

        Player player = gameController.getCurrent();

        for (int i = 0; i < 6; i++) {
            Building s = new Building(player.getId(), BuildingType.SETTLEMENT, gameController.getBoard()
                    .getRandomFreeSettlementLoc());
            assertTrue(gameController.placeBuilding(s));
            Building r = new Building(player.getId(), BuildingType.ROAD, gameController.getBoard()
                    .getFreeRoadLoc(player, gameController.isInitialPhaseActive()));
            assertTrue(gameController.placeBuilding(r));

            if (i < 3) assertEquals(0, player.getRawMaterialCount());
            else assertTrue(player.getRawMaterialCount() >= 1);

            player = gameController.getCurrent();
        }
    }
    //endregion
}