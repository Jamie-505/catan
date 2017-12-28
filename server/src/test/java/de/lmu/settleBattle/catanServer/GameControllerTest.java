package de.lmu.settleBattle.catanServer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GameControllerTest {
    private GameController gameController;
    private Player player1;
    private Player player2;
    private Player player3;

    @Before
    public void setUp() throws Exception {
        gameController = new GameController();

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
        gameController.setPlayerActive(player1.getId(), Constants.BUILD_SETTLEMENT);

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
        Player player = gameController.getCurrent();
        gameController.setPlayerActive(player.getId(), Constants.BUILD_SETTLEMENT);
    }

    @Test
    public void startGame() throws Exception {
        initializeGame();

        List<Player> list = gameController.getPlayers();

        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());
        assertTrue(list.get(0).getStatus().equals(Constants.BUILD_SETTLEMENT));
        assertTrue(list.get(1).getStatus().equals(Constants.WAIT));
        assertTrue(list.get(2).getStatus().equals(Constants.WAIT));

        getNext();
    }

    //region getNext
    public void getNext() throws Exception {
        List<Player> list = gameController.getPlayers();

        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(1).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(2).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(2).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(1).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(1).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(2).getId() == gameController.getCurrent().getId());

        gameController.getNext();
        assertTrue(list.get(0).getId() == gameController.getCurrent().getId());
    }
    //endregion

    @Test
    public void moveRobber() {
        int currentId = gameController.getCurrent().getId();

        //board contains no settlements so there is no one to rob
        assertTrue(gameController.moveRobber(currentId, 7, new Location(-1,0)));

        //only one player can be robbed, target is specified
        int robbedId = currentId == 2 ? 3 : 2;
        gameController.getPlayer(robbedId).setRawMaterialDeck(new RawMaterialOverview(0,0,2,0,0));

        Building s1 = new Building(robbedId, BuildingType.SETTLEMENT, new Location[] {new Location(2,0), new Location(1,1), new Location(2,1)});
        gameController.getBoard().addSettlement(s1);

        assertTrue(gameController.moveRobber(currentId, robbedId, new Location(2,0)));
        assertTrue(gameController.getPlayer(robbedId).getRawMaterialCount() == 1);

        //only one player can be robbed, target is not specified
        Building s2 = new Building(robbedId, BuildingType.SETTLEMENT, new Location[] {new Location(-3,0), new Location(-2,0), new Location(-2,-1)});
        gameController.getBoard().addSettlement(s2);

        assertTrue(gameController.moveRobber(currentId, 7, new Location(-2,0)));
        assertTrue(gameController.getPlayer(robbedId).getRawMaterialCount() == 0);

        int thirdId = currentId == 1 ? 3 : 1;

        //more players can be robbed, target is not specified
        Building s3 = new Building(robbedId, BuildingType.SETTLEMENT, new Location[] {new Location(-1,1), new Location(0,0), new Location(-1,0)});
        Building s4 = new Building(thirdId, BuildingType.SETTLEMENT, new Location[] {new Location(1,-1), new Location(0,0), new Location(1,0)});
        gameController.getBoard().addSettlement(s3);
        gameController.getBoard().addSettlement(s4);
        Player p1 = gameController.getPlayer(robbedId);
        Player p2 = gameController.getPlayer(thirdId);
        p1.setRawMaterialDeck(new RawMaterialOverview(1,1,1,0,1));
        p2.setRawMaterialDeck(new RawMaterialOverview(2,0,2,0,0));

        int robbedRmCount = p1.getRawMaterialCount();
        int thirdRmCount = p2.getRawMaterialCount();

        assertTrue(gameController.moveRobber(currentId, 7, new Location(0,0)));

        //one of them got robbed
        assertTrue(p1.getRawMaterialCount() == robbedRmCount-1 || p2.getRawMaterialCount() == thirdRmCount-1);
        assertFalse(p1.getRawMaterialCount() == robbedRmCount-1 && p2.getRawMaterialCount() == thirdRmCount-1);

        //do not rob own player, robber will be moved but no raw materials will be de/increased
        Building s5 = new Building(currentId, BuildingType.SETTLEMENT, new Location[] {new Location(-1,-1), new Location(0,-1), new Location(0,-2)});
        Building s6 = new Building(currentId, BuildingType.SETTLEMENT, new Location[] {new Location(0,-2), new Location(0,-3), new Location(1,-3)});
        gameController.getBoard().addSettlement(s5);
        gameController.getBoard().addSettlement(s6);

        int currentRmCount = gameController.getCurrent().getRawMaterialCount();
        assertTrue(gameController.moveRobber(currentId, -1, new Location(0,-2)));
        assertTrue(currentRmCount == gameController.getCurrent().getRawMaterialCount());
    }
}