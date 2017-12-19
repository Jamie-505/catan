package de.lmu.settleBattle.catanServer;

import java.util.*;

import static de.lmu.settleBattle.catanServer.Constants.*;

public class GameController {
    private Board board;
    private List<Player> players;
    private int currentTurn;
    private List<String> chatHistory;
    private RawMaterialOverview rawMaterialDeck;
    private DevelopmentCardOverview developmentCardDeck;
    private Stack<Player> playerStack;
    private boolean buildingPhaseActive;
    private int round;
    private boolean isGameOver = false;
    private boolean gameStarted = false;

    public GameController() {
        board = new Board();
        players = new ArrayList<>();
        currentTurn = 0;
        chatHistory = new ArrayList<>();
        rawMaterialDeck = new RawMaterialOverview(19);
        developmentCardDeck = new DevelopmentCardOverview();
        buildingPhaseActive = false;
        round = -1;
    }


    public boolean startGame() {
        if (readyToStartGame()) {
            defineTurnOrder();
            initializeBuildingPhase();
            gameStarted = true;
        }
        return gameStarted;
    }

    public void defineTurnOrder() {
        Collections.shuffle(players);
        currentTurn = 0;
    }

    public void initializeBuildingPhase() {
        playerStack = new Stack<>();
        buildingPhaseActive = true;
        round = 1;

    }

    public Player getNext() {
        return this.buildingPhaseActive ? getNext_BuildingPhase() : getNext_Move();
    }

    private Player getNext_Move() {
        currentTurn++;
        return players.get(this.getCurrentIndex());
    }

    private Player getNext_BuildingPhase() {
        Player player;
        if (round == 1) {
            playerStack.push(getCurrent());
            if (playerStack.size() == players.size()) {
                round = 2;
                player = playerStack.pop();
            }
            else player = this.getNext_Move();

        } else if (round == 2){
            player = playerStack.pop();
            currentTurn--;

            if (playerStack.size() == 0) {
                //set round 3 before initial building phase ends so that first player can start game
                round = 3;
                currentTurn=0;
            }
        }
        else {  //all players have placed their buildings and the first player has turn
            buildingPhaseActive = false;
            player = getCurrent();
        }

        return player;
    }


    public boolean placeBuilding(int owner, Location[] locs, BuildingType type) {
        boolean buildPermission = false;
        boolean built = false;

        //during the building phase no cities can be built and no raw materials are reduced
        if (this.buildingPhaseActive && !type.equals(BuildingType.CITY))
            buildPermission = true;

        //the player can only built building if he has enough raw materials
        else if (!this.buildingPhaseActive) {
            buildPermission = getPlayer(owner).canAfford(type);
        }

        if (buildPermission) {
            built = board.placeBuilding(owner, locs, type);

            //if the building was successfully built and the player needs to pay for it
            //--> reduce raw materials
            if (built && !buildingPhaseActive)
                getPlayer(owner).decreaseRawMaterials(Building.getCosts(type));
        }
        return built;
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


    public Player getCurrent() {
        return players.get(this.getCurrentIndex());
    }

    public int getCurrentIndex() {
        return currentTurn % players.size();
    }

    //region mapOwnerWithHarvest
    public Map<Integer, RawMaterialOverview> mapOwnerWithHarvest(int number) {
        Map<Integer, RawMaterialOverview> distribution = new HashMap<>();

        Map<Building, RawMaterialType> buildings =
                board.getBuildingsOnDistributingFields(number);

        for (Building bld : buildings.keySet()) {
            int amount = bld.getType().equals(BuildingType.CITY) ? 2 : 1;

            if (distribution.containsKey(bld.getOwner())) {
                RawMaterialOverview overview = distribution.get(bld.getOwner());
                overview.increase(buildings.get(bld), amount);
                distribution.replace(bld.getOwner(), overview);

            } else distribution.put(bld.getOwner(),
                    new RawMaterialOverview(buildings.get(bld), amount));
        }

        return distribution;
    }
    //endregion

    public void distributeRawMaterial(Map<Integer, RawMaterialOverview> distribution) {

        for (Player p : players) {
            if (distribution.containsKey(p.getId())) {
                RawMaterialOverview harvest = distribution.get(p.getId());
                p.increaseRawMaterials(harvest);
            }
        }
    }

    public void endGame() {
    }

    public void conductTrade(TradeRequest tr) {
    }

    /**
     * sets new status for active player and
     * sets status of all others to "Warten"
     *
     * @param id
     * @param status
     * @return list of players where status changed
     */
    public void setPlayerActive(int id, String status) {
        for (Player player : players) {
            String newStatus = player.getId() == id ? status : WAIT;
            player.setStatus(newStatus);
        }
    }


    //region isValidPlayerData

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
     *
     * @return true if every player has status "Spiel gestartet", otherwise false
     */
    public boolean readyToStartGame() {

        if (3 <= players.size() && players.size() <= 4) {

            for (Player player : players) {
                if (!player.getStatus().equals(WAIT_FOR_GAME_START))
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

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isBuildingPhaseActive() {
        return buildingPhaseActive;
    }

    public void setBuildingPhaseActive(boolean buildingPhaseActive) {
        this.buildingPhaseActive = buildingPhaseActive;
    }

    public Board getBoard() {
        return board;
    }
}

