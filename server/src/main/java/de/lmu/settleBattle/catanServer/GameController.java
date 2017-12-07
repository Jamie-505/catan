package de.lmu.settleBattle.catanServer;
import java.util.*;

public class GameController {
    private Board board;
    private List<Player> players;
    private int currentPlayerId;
    private HashMap<Integer, Integer> turnOrder;
    private List<String> chatHistory;
    private RawMaterialOverview resourcesDeck;
    private DevelopmentCardDeck developmentDeck;

    public GameController() {

        board = new Board();
        players = new ArrayList();
        currentPlayerId = -1;
        turnOrder = new HashMap<>();
        chatHistory = new ArrayList<>();
        resourcesDeck =  new RawMaterialOverview(19);
        developmentDeck = new DevelopmentCardDeck();
    }

    //region isValidPlayerData

    /**
     * checks if the chosen player data is valid, meaning
     * if the color or name are already taken
     *
     * @param id
     * @param name
     * @param color
     * @return true if the color and name are free, otherwise false
     */
    public boolean isValidPlayerData(int id, String name, Color color) {
        for (int i = 0; i < players.size(); i++) {

            //do not compare player with itself
            if (players.get(i).getId().equals(id))
                continue;

            // if a name or a color is already chosen
            //return false
            if (players.get(i).getName().equals(name) ||
                    players.get(i).getColor().equals(color))
                return false;
        }
        return true;
    }
    //endregion

    //region defineTurnOrder
    public void defineTurnOrder() {
        turnOrder = new HashMap<>();

        List<Integer> randomTurnOrder = new ArrayList();

        for (int i = 0; i < players.size(); i++) {
            randomTurnOrder.add(i);
        }

        Collections.shuffle(randomTurnOrder);

        for (int i = 0; i < players.size(); i++) {
            turnOrder.put(i, randomTurnOrder.get(i));
        }
    }
    //endregion

    public void initializeBuildingPhase() {
        //players[turnOrder.get(0)].setStatus(StatusConstants.BUILD_SETTLEMENT);
        //players[turnOrder.get(1)].setStatus(StatusConstants.WAIT);
        //players[turnOrder.get(2)].setStatus(StatusConstants.WAIT);
        //players[turnOrder.get(3)].setStatus(StatusConstants.WAIT);
    }

    ;

    public void initializeNextMove() {
    }

    ;

    public void distributeRawMaterial() {
    }

    ;

    public void endGame() {
    }

    ;

    public void conductTrade(TradeRequest tr) {
    }

    ;

    public void addPlayer(Player p) throws Exception {
        if (players.size() < 4)
            players.add(p);

        else throw new Exception("The player cannot be added. There" +
                "are already 4 players for this game!");
    }

    public int getPlayerCount() {
        return players.size();
    }

    public Player getPlayer(String sessionId) {
        for (Player p : players) {
            if (p.getSessionId().equals(sessionId))
                return p;
        }

        return null;
    }

    public boolean removePlayer(Player p) {
        return players.remove(p);
    }
}
