package de.lmu.settleBattle.catanServer;

import static de.lmu.settleBattle.catanServer.Constants.TRADE_OR_BUILD;
import static de.lmu.settleBattle.catanServer.Constants.WAIT;
import static de.lmu.settleBattle.catanServer.Constants.WAIT_FOR_GAME_START;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

import static de.lmu.settleBattle.catanServer.Constants.*;

public class GameController implements PropertyChangeListener {
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
    private Player greatestArmyPlayer;

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


    //region starting and initializing the game
    public boolean startGame() {
        if (readyToStartGame()) {
            defineTurnOrder();
            initializeBuildingPhase();
            gameStarted = true;
            setPlayerActive(getCurrent().getId(), BUILD_SETTLEMENT);
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
    //endregion

    //region turn order handling
    public Player getCurrent() {
        return players.get(this.getCurrentIndex());
    }

    public int getCurrentIndex() {
        return currentTurn % players.size();
    }

    public Player nextMove() {
        return this.buildingPhaseActive ? next_initialPhase() : next_gameStarted();
    }

    private Player next_gameStarted() {
        currentTurn++;
        if (!buildingPhaseActive) setPlayerActive(getCurrent().getId(), DICE);
        return players.get(this.getCurrentIndex());
    }

    private Player next_initialPhase() {
        Player player;
        if (round == 1) {
            playerStack.push(getCurrent());
            if (playerStack.size() == players.size()) {
                round = 2;
                player = playerStack.pop();
            } else player = this.next_gameStarted();

        } else if (round == 2) {
            player = playerStack.pop();
            currentTurn--;

            if (playerStack.size() == 0) {
                //set round 3 before initial building phase ends so that first player can start game
                round = 3;
                currentTurn = 0;
            }
        } else {  //all players have placed their buildings and the first player has turn
            player = getCurrent();
            setBuildingPhaseActive(false);
        }

        return player;
    }


    public void setPlayerActive(int id, String status) {
        setPlayerActive(getPlayer(id), status);
    }

    public void setPlayerActive(Player player, String status) {
        System.out.printf("Set player %s to status %s", player.getId(), status);

        //due to property change listeners first all stati need to be set to WAIT
        //and afterwards active player status must be set
        for (Player p : players) {
            p.setStatus(WAIT);
        }

        player.setStatus(status);

    }

    //endregion

    //region place buildings handling

    /**
     * checks if the owner has the permission to build a building
     *
     * @param building
     * @return
     */
    private boolean hasBuildPermission(Building building) {
        boolean buildPermission = false;

        //during the building phase no cities can be built and no raw materials are reduced
        if (this.buildingPhaseActive && !building.isCity()) {
            //in initial building phase a road can only be placed next to last settlement that was built
            if (building.isRoad()) {
                Building settlement = getPlayer(building.getOwner()).getLastSettlement();
                buildPermission = settlement != null &&
                        settlement.isBuiltAroundHere(building.getLocations(), false);
            } else buildPermission = true;
        }

        //the player can only built building if he has enough raw materials
        else if (!this.buildingPhaseActive) {
            Player player = getPlayer(building.getOwner());
            buildPermission = player.canAfford(building.getType()) && player.getStock().getCount(building.getType()) > 0;
        }

        return buildPermission;
    }

    /**
     * places building if possible and does follow-up work
     * @param building
     * @return
     */
    public boolean placeBuilding(Building building) throws IllegalAccessException {
        boolean built;

        if (!hasBuildPermission(building)) return false;

        built = board.placeBuilding(building, this.buildingPhaseActive);

        //if the building was successfully built do follow-up work
        if (built) {
            Player owner = getPlayer(building.getOwner());
            owner.addBuilding(building);

            //add building to haven array in player so he can trade with it
            //havens are only added for settlements (if a city is placed then the haven was already
            //added when the sLocs was placed)
            if (board.isConnectedToHaven(building) && building.isSettlement()) {
                Haven haven = board.getConnectedHaven(building);
                owner.addHaven(haven);
            }

            //increase victory points
            if (!building.isRoad()) {
                    // always add 1 victory point because:
                    // a new settlement brings 1 victory point and
                    // if a city was built then the owner has already received 1 victory point for the settlement he built before
                    owner.increaseVictoryPoints(1);
            }

            //raw material distribution for second settlement
            if (buildingPhaseActive && (round == 2 || round == 3) && building.isSettlement()){
                distributeRawMaterial(building);
            }


            //decrease raw materials
            if (!buildingPhaseActive) owner.decreaseRawMaterials(Building.getCosts(building.getType()));

            updateStatus(owner.getId());
        }
        return built;
    }
    //endregion

    /**
     * updates status of player if the building phase is active or just ended
     *
     * @param id
     */
    private void updateStatus(int id) {
        if (getCurrent().getId() != id) {
            System.out.printf("Die IDs %s und %s stimmen nicht überein", getCurrent().getId(), id);
            return;
        }

        if (buildingPhaseActive) {
            String status = Constants.BUILD_STREET;
            Player player = getCurrent();

            if (player.getStatus().equals(BUILD_STREET)) {
                player = nextMove();
                status = buildingPhaseActive ? BUILD_SETTLEMENT : DICE;
            }

            setPlayerActive(player.getId(), status);
        }
    }

    //region development card handling

    /**
     * <method name: addDevelopmentCard>
     * <description: this method performs the actions required to buy development card>
     * <preconditions: player has the required cards to buy and his turn is up>
     * <postconditions: player gets a development card in exchange for his material cards>
     */

    public boolean buyDevelopmentCard(Player player) {
        boolean ret = false;

        if (player.canAffordDevCard()) {
            RawMaterialOverview price = new RawMaterialOverview(0,1,0,1,1);
            try {
                player.decreaseRawMaterials(price);
                player.addDevelopmentCard(developmentCardDeck.withdrawRandomCard(), 1);
                this.rawMaterialDeck.increase(price);
                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
                ret = false;
            }
        }

        return ret;
    }


    public boolean applyMonopoleCard(Player monoPlayer, RawMaterialType targetType) {
        if (!monoPlayer.hasMonopoleCard()) return false;

        try {
            monoPlayer.removeDevelopmentCard(DevCardType.MONOPOLE, 1);

            for (Player player : players) {
                if (player != monoPlayer && player.hasRawMaterial(targetType)) {
                    RawMaterialOverview overview =
                            new RawMaterialOverview(targetType, player.getRawMaterialCount(targetType));
                    player.decreaseRawMaterials(overview);
                    monoPlayer.increaseRawMaterials(overview);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean applyInventionCard(Player player, RawMaterialOverview overview) {
        if (!player.hasInventionCard() || overview.getTotalCount() != 2)
            return false;

        boolean ret;
        try {

            this.rawMaterialDeck.decrease(overview);
            player.increaseRawMaterials(overview);
            player.removeDevelopmentCard(DevCardType.INVENTION, 1);
            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
        }
        return ret;
    }
    //endregion

    //region field distribution handling
    public Map<Integer, RawMaterialOverview> mapOwnerWithHarvest(int number) {
        Map<Integer, RawMaterialOverview> distribution = new HashMap<>();

        Map<Building, RawMaterialType> buildings =
                board.getBuildingsOnDistributingFields(number);

        for (Building bld : buildings.keySet()) {
            int amount = bld.isCity() ? 2 : 1;

            if (distribution.containsKey(bld.getOwner())) {
                RawMaterialOverview overview = distribution.get(bld.getOwner());
                overview.increase(buildings.get(bld), amount);
                distribution.replace(bld.getOwner(), overview);

            } else distribution.put(bld.getOwner(),
                    new RawMaterialOverview(buildings.get(bld), amount));
        }

        return distribution;
    }

    /**
     * distributes raw materials for one building
     *
     * @param building
     */
    public void distributeRawMaterial(Building building) {
        if (!building.isRoad()) {
            RawMaterialOverview overview = board.getHarvest(building);
            getPlayer(building.getOwner()).increaseRawMaterials(overview);
        }
    }

    public void distributeRawMaterial(Map<Integer, RawMaterialOverview> distribution) {

        for (Player p : players) {
            if (distribution.containsKey(p.getId())) {
                RawMaterialOverview harvest = distribution.get(p.getId());
                p.increaseRawMaterials(harvest);
            }
        }
    }
    //endregion

    //region trade handling
    public boolean domesticTrade(TradeRequest tradeRequest, int offerentId, int fellowPlayerId) {
        Player offerent = getPlayer(offerentId);
        Player fellowPlayer = getPlayer(fellowPlayerId);

        if (offerent.canAfford(tradeRequest.getOffer()) &&
                fellowPlayer.canAfford(tradeRequest.getRequest()) &&
                tradeRequest.canBeExecutedBy(fellowPlayerId)) {

            offerent.trade(tradeRequest.getOffer(), tradeRequest.getRequest());
            fellowPlayer.trade(tradeRequest.getRequest(), tradeRequest.getOffer());

            tradeRequest.execute(fellowPlayerId);
            return true;
        }

        return false;
    }

    /**
     * performs sea trade
     * client does not haven any logic and thus sends a RawMaterialOverview offer
     * and one re
     *
     * @param player
     * @param tradeRequest
     * @return
     */
    public boolean seaTrade(Player player, TradeRequest tradeRequest) {

        //client sends raw material overview containing 1 clay/ore/.. ore for request/offer
        if (tradeRequest.getRequest().getTotalCount() != 1 &&
                tradeRequest.getOffer().getTotalCount() != 1)
            return false;

        RawMaterialType requestType = tradeRequest.getRequest().getType();
        RawMaterialType offerType = tradeRequest.getOffer().getType();

        if (!requestType.isValidTradingType() || !offerType.isValidTradingType()
                || requestType.equals(offerType)) return false;

        //check if player has 2:1 haven for trading
        Haven haven = player.get2To1Haven(offerType);
        RawMaterialOverview offer, request;
        int offerAmount = 2;

        if (haven == null) {
            offerAmount = player.hasXTo1Haven(3) ? 3 : 4;
        }

        offer = new RawMaterialOverview(offerType, offerAmount);
        request = new RawMaterialOverview(requestType, 1);

        if (player.canAfford(offer)) {
            try {
                player.trade(offer, request);
                return true;
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }
    //endregion

    //region player object handling

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

    public void addPlayer(Player p) throws IllegalAccessException {
        if (gameStarted) throw new IllegalAccessException("The game is already started!");

        if (players.size() < 4) {

            players.add(p);
            if (p.isKI()) p.addPropertyChangeListener(this);

        } else throw new IllegalAccessException("The player cannot be added. There" +
                "are already 4 players for this game!");
    }

    /**
     * adds new KI to game
     *
     * @throws IllegalAccessException
     */
    public Player addKI() throws IllegalAccessException {
        if (gameStarted)
            throw new IllegalAccessException("The game is already started!");

        Player player = new Player(getValidId(), getValidColor());
        player.setName("KI(" + player.getColor() + ")");
        setKI(player);
        addPlayer(player);
        return player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setKI(int id) {
        setKI(getPlayer(id));
    }

    public void setKI(Player player) {
        player.addPropertyChangeListener(this);
        player.setKI(true);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public Player getPlayer(String sessionId) {
        return getPlayer(SocketUtils.toInt(sessionId));
    }

    public Player getPlayer(int id) {
        for (Player p : players) {
            if (p.getId() == id)
                return p;
        }

        return null;
    }

    public boolean removePlayer(Player p) {
        return players.remove(p);
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

    /**
     * @return valid ID for new player
     */
    private int getValidId() {
        int id = 1000;
        for (Player player : players)
            id += player.getId();
        return id;
    }

    /**
     * @return valid color for new player
     */
    private Color getValidColor() throws IllegalAccessException {
        for (Color color : Color.values()) {
            if (isValidColor(color))
                return color;
        }

        throw new IllegalAccessException("All colors are occupied.");
    }

    //endregion

    //region robber handling

    /**
     * moves robber and exchanges raw materials
     *
     * @param currentId id of player moving the robber
     * @param targetId  id of player being robbed
     * @param newLoc    new location of robber
     * @return
     */
    public boolean activateRobber(int currentId, int targetId, Location newLoc) {
        boolean robSuccessful = false;

        //check if player has turn
        if (getCurrent().getId() != currentId) return false;

        //move robber to target location
        if (board.getRobber().move(newLoc)) {
            Player targetPlayer = getPlayer(targetId);
            Player currentPlayer = getCurrent();
            HashSet<Integer> owners = board.getOwnersToRobFrom(currentId, newLoc);

            //target was given
            if (targetPlayer != null && owners.contains(targetId)) {

                //it might be that the target player does not possess any raw materials
                //in this case the rob is stated as successful though because the target
                //was specifically determined by the client
                board.getRobber().robPlayer(currentPlayer, targetPlayer);
                robSuccessful = true;

            } else {
                int matchCount = 0;

                //get first player who can be robbed and rob random raw material
                for (int owner : owners) {
                    targetPlayer = getPlayer(owner);

                    if (targetPlayer != null && targetPlayer.getRawMaterialCount() > 0) {
                        board.getRobber().robPlayer(currentPlayer, targetPlayer);
                        robSuccessful = true;
                        break;
                    } else matchCount++;
                }

                //nobody can be robbed because nobody has a sLocs/city there or nobody has any raw materials
                if (!robSuccessful && matchCount == owners.size()) robSuccessful = true;
            }
        }
        return robSuccessful;
    }

    /**
     * player tosses cards after a roll of 7 if he has at least 7 raw materials
     *
     * @param id
     * @param overview
     * @return
     */
    public boolean tossRawMaterialCards(int id, RawMaterialOverview overview) {
        Player player = getPlayer(id);
        boolean ret = board.getRobber().robPlayer(player, overview);

        //if player still has 7 cards or more he must still toss cards and his status won't be changed
        if (!player.hasToExtractCards() && ret) {

            //set new status for player
            String newStatus = player.getId() == getCurrent().getId() ? TRADE_OR_BUILD : WAIT;
            player.setStatus(newStatus);
            ret = true;
        }

        return ret;
    }
    //endregion

    //region dice
    public boolean dice(int id) {
        if (getCurrent().getId() != id) return false;

        int[] dice = getCurrent().throwDice();
        int sum = dice[0] + dice[1];

        //the robber is activated
        if (sum == 7) {
            //others have to extract cards
            extractCardsDueToRobber();

            //update status of current player
            if (!getCurrent().getStatus().equals(EXTRACT_CARDS_DUE_TO_ROBBER))
                getCurrent().setStatus(TRADE_OR_BUILD);
        }

        //distribute raw materials
        else {
            Map<Integer, RawMaterialOverview> distribution =
                    mapOwnerWithHarvest(sum);

            distributeRawMaterial(distribution);

            //after raw material distribution player can continue his turn
            setPlayerActive(id, TRADE_OR_BUILD);
        }

        return true;

    }

    public void extractCardsDueToRobber() {
        for (Player player : getPlayers()) {
            if (player.hasToExtractCards()) {
                player.setStatus(EXTRACT_CARDS_DUE_TO_ROBBER);
            }
        }
    }

    //endregion

    //region properties
    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isBuildingPhaseActive() {
        return buildingPhaseActive;
    }

    public void setBuildingPhaseActive(boolean active) {
        this.buildingPhaseActive = active;
    }

    public Board getBoard() {
        return board;
    }

    public int getRound() { return this.round; }

    public boolean endGame(Player winner) {
        if(!winner.hasWon() ||  winner.getId() != getPlayerWithHighestPoints().getId()){
            return false;
        }
        this.isGameOver = true;
        return true;
    }

    public Player getPlayerWithHighestPoints(){
        Player topPlayer = players.get(0);
        for (Player player : players){
            if(player.getVictoryPointsCount() > topPlayer.getVictoryPointsCount()){
                topPlayer = player;
            }
        }
        return topPlayer;

    }

    public RawMaterialOverview getRawMaterialDeck() {
        return this.rawMaterialDeck;
    }

    public boolean assignGreatestArmy(Player player) throws Exception {
        boolean done = false;

        if (player.getArmyCount() < 3) return true;

        if (this.greatestArmyPlayer == null) {
            player.increaseVictoryPoints(2);
            player.setGreatestArmy(true);
            this.greatestArmyPlayer = player;
            done = true;
        } else {

            if (this.getPlayerWithHighestArmyCount() == player && this.greatestArmyPlayer != player) {

                try {
                    greatestArmyPlayer.decreaseVictoryPoints(2);
                    greatestArmyPlayer.setGreatestArmy(false);
                    player.increaseVictoryPoints(2);
                    player.setGreatestArmy(true);
                    this.greatestArmyPlayer = player;
                    done = true;
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                    done = false;
                }
            }
        }

        return done;

    }

    private Player getPlayerWithHighestArmyCount() {
        Player topPlayer = players.get(0);

        for (Player player : players) {
            if (player.getArmyCount() > topPlayer.getArmyCount()) {
                topPlayer = player;
            }
        }

        return topPlayer;
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        Player ki = (Player) evt.getNewValue();
        if (!ki.isKI()) return;

        if (!evt.getPropertyName().equals(PROP_STATUS))
            return;

        switch (ki.getStatus()) {
            case START_GAME:
                ki.setStatus(WAIT_FOR_GAME_START);
                break;

            case EXTRACT_CARDS_DUE_TO_ROBBER:
                //KI has to extract cards
                board.getRobber().robPlayer(ki);

                String status = getCurrent().getId() == ki.getId() ? TRADE_OR_BUILD : WAIT;
                ki.setStatus(status);
                break;

            case DICE:
                dice(ki.getId());
                break;

            case BUILD_SETTLEMENT:
                Building settlement = new Building(ki.getId(), BuildingType.SETTLEMENT, board.getRandomFreeSettlementLoc());
                try {
                    placeBuilding(settlement);
                }catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
                break;

            case BUILD_STREET:
                try {
                    Building street = new Building(ki.getId(), BuildingType.ROAD, board.getFreeRoadLoc(ki, buildingPhaseActive));
                    placeBuilding(street);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
                break;

            case TRADE_OR_BUILD:
                if (ki.canAfford(BuildingType.SETTLEMENT)) {
                    try {
                        Building s = new Building(ki.getId(), BuildingType.SETTLEMENT, board.getRandomFreeSettlementLoc());
                        placeBuilding(s);
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                } else if (ki.canAfford(BuildingType.ROAD)) {
                    try {
                        Building r = new Building(ki.getId(), BuildingType.ROAD, board.getFreeRoadLoc(ki, buildingPhaseActive));
                        placeBuilding(r);
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    buyDevelopmentCard(ki);
                }

                nextMove();

                break;

            case ROBBER_TO:
                boolean moved = activateRobber(ki.getId(), -1, new Location());
                if (!moved) moved = activateRobber(ki.getId(), -1, new Location(1, 1));

                if (!moved) throw new IllegalArgumentException("Der Räuber kann nicht versetzt werden.");
                break;
        }
    }

    //endregion
}

