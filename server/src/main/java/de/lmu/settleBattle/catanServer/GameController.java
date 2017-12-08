package de.lmu.settleBattle.catanServer;
import java.util.*;

public class GameController {
  private Board board;
  private List<Player> players;
  private int currentTurnOrderIndex;
  private HashMap<Integer, Integer> turnOrder;
  private List<String> chatHistory;
  private RawMaterialOverview rawMaterialDeck;
  private DevelopmentCardOverview developmentCardDeck;

  public GameController() {
    board = new Board();
    players = new ArrayList<>();
    currentTurnOrderIndex = -1;
    turnOrder = new HashMap<>();
    chatHistory = new ArrayList<>();
    rawMaterialDeck =  new RawMaterialOverview(19);
    developmentCardDeck = new DevelopmentCardOverview();
  }


  /**
   * <method name: buyDevelopmentCard>
   * <description: this method performs the actions required to buy development card>
   * <preconditions: player has the required cards to buy and his turn is up>
   * <postconditions: player gets a development card in exchange for his material cards>
   */

  public void tradeDevelopmentCard(Player player) {
    if (player.canAffordDevCard()) {
      try {
        player.buyDevelopmentCard(developmentCardDeck.withdrawRandomCard());
        sellDevelopmentCard();
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

  }

  public void sellDevelopmentCard() {
    this.rawMaterialDeck.increase(RawMaterialType.ORE, 1);
    this.rawMaterialDeck.increase(RawMaterialType.WOOD, 1);
    this.rawMaterialDeck.increase(RawMaterialType.WEAT, 1);
  }

  //region defineTurnOrder
  public void defineTurnOrder() {
    turnOrder = new HashMap<>();

    ArrayList<Integer> randomTurnOrder = new ArrayList();

    for (int i = 0; i < players.size(); i++) {
      randomTurnOrder.add(i);
    }

    Collections.shuffle(randomTurnOrder);

    for (int i = 0; i < players.size(); i++) {
      turnOrder.put(i, randomTurnOrder.get(i));
    }

    currentTurnOrderIndex = 0;
  }
  //endregion

  public void initializeBuildingPhase() {
    //players[turnOrder.get(0)].setStatus(StatusConstants.BUILD_SETTLEMENT);
    //players[turnOrder.get(1)].setStatus(StatusConstants.WAIT);
    //players[turnOrder.get(2)].setStatus(StatusConstants.WAIT);
    //players[turnOrder.get(3)].setStatus(StatusConstants.WAIT);
  }

  ;

  public Player getNext() {
    currentTurnOrderIndex++;
    return getPlayer(turnOrder.get(currentTurnOrderIndex%players.size()));
  }

  public Player getCurrent() {
    return getPlayer(turnOrder.get(currentTurnOrderIndex));
  }

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

  /**
   * sets new status for active player and
   * sets status of all others to "Warten"
   * @param id
   * @param status
   */
  public void setPlayerActive(int id, String status) {
    for (Player player : players) {
      if (player.getId() == id)
        player.setStatus(status);
      else player.setStatus(Constants.WAIT);
    }
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
    for (Player player : players) {

      //do not compare player with itself
      if (player.getId() == id)
        continue;

      // if a name or a color is already chosen
      //return false
      if (player.getName().equals(name) ||
          player.getColor().equals(color))
        return false;
    }
    return true;
  }

  public boolean isValidName(String name) {
    if (name == null || name.equals("")) return false;

    for (Player player : players) {
      if (player.getName() != null && player.getName().equals(name))
        return false;
    }

    return true;
  }

  public boolean isValidColor(Color color) {
    if (color == null) return false;

    for (Player player : players) {
      if (player.getColor() != null && player.getColor() == color)
        return false;
    }

    return true;
  }

  //endregion

  public void addPlayer(Player p) throws IllegalAccessException {
    if (players.size() < 4)
      players.add(p);

    else throw new IllegalAccessException("The player cannot be added. There" +
        "are already 4 players for this game!");
  }

  public List<Player> getPlayers() {
    return players;
  }

  public int getPlayerCount() {
    return players.size();
  }

  public Player getPlayer(String sessionId) {
    return getPlayer(Integer.parseInt(sessionId, 16));
  }

  public Player getPlayer(int id) {
    for (Player p : players) {
      if (p.getId() == id)
        return p;
    }

    return null;
  }


  /**
   * checks if everybody is ready to play
   * @return true if every player has status "Spiel gestartet", otherwise false
   */
  public boolean readyToStartGame() {

    if (3 <= players.size() && players.size() <= 4) {

      for (Player player : players) {
        if (!player.getStatus().equals(Constants.START_CON))
          return false;
      }
      return true;
    }

    return false;
  }

  public boolean removePlayer(Player p) {
    return players.remove(p);
  }

  public ArrayList<Integer> getPlayersWithAtLeast7RawMaterials() {
    ArrayList<Integer> ids = new ArrayList<>();

    for (Player player : players) {
      if (player.hasToExtractCards())
        ids.add(player.getId());
    }

    return ids;
  }
}
