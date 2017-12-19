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
}