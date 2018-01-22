package de.lmu.settleBattle.catanServer;

import static de.lmu.settleBattle.catanServer.Constants.*;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.beans.PropertyChangeListener;

public class GameController {

    //region property change listener
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
    //endregion

    private Board board;
    private List<Player> players;
    private int currentTurn;
    private List<String> chatHistory;
    private RawMaterialOverview rawMaterialDeck;
    private DevelopmentCardOverview developmentCardDeck;
    private Stack<Player> playerStack;
    private boolean initialPhaseActive;
    private int round;
    private boolean isGameOver = false;
    private boolean gameStarted = false;
    private Player greatestArmyPlayer;

    public GameController() {
        players = new ArrayList<>();
        board = new Board();
        currentTurn = 0;
        chatHistory = new ArrayList<>();
        rawMaterialDeck = new RawMaterialOverview(19);
        developmentCardDeck = new DevelopmentCardOverview();
        initialPhaseActive = false;
        round = -1;
    }


    /**
     * checks if everybody is ready to start
     * defines turn order and starts game run
     *
     * @return true if game was started, otherwise false
     */
    public boolean startGame() {
        if (readyToStartGame()) {
            //defineTurnOrder();
            initializeBuildingPhase();
            gameStarted = true;
            activatePlayer(getCurrent().getId(), BUILD_SETTLEMENT);
        }
        return gameStarted;
    }

    /**
     * defines random turn order by shuffeling players
     */
    public void defineTurnOrder() {
        Collections.shuffle(players);
        currentTurn = 0;
    }

    /**
     * initializes objects used for initial phase
     */
    public void initializeBuildingPhase() {
        playerStack = new Stack<>();
        initialPhaseActive = true;
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

    /**
     * determines player who can move next
     * @return player to turn next
     */
    public Player nextMove() {
        return this.initialPhaseActive ? next_initialPhase() : next_gameStarted();
    }

    /**
     * will be called if game is started aleady and building phase is over
     * @return player to turn next
     */
    private Player next_gameStarted() {
        currentTurn++;
        if (!initialPhaseActive) activatePlayer(getCurrent().getId(), DICE);
        return players.get(this.getCurrentIndex());
    }

    /**
     * will be called if the initial phase is active
     * @return player to turn next
     */
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
            setInitialPhaseActive(false);
        }

        return player;
    }

    /**
     * if a real player is active, KIs need to be deactivated sometimes
     * @param ki ki to be deactivated
     * @return if KI was deactivated (real player is active), otherwise false
     */
    public boolean deactivateKI(Player ki) {

        if (!ki.isKI()) return false;

        boolean deactivateKI = false;

        for (Player player : getRealPlayers()) {
            if (player.isActive()) deactivateKI = true;
        }

        if (deactivateKI) ki.setDeactivated(true);
        else {
            ki.activateNextStatus();
            return false;
        }

        return true;
    }

    public void activatePlayer(int id, String status) {
        activatePlayer(getPlayer(id), status);
    }

    public void activateKIs() {
        for (Player player : players) {
            if (player.isDeactivated())
                player.activateNextStatus();
        }
    }

    public boolean realPlayersAreReady() {
        for (Player player : getRealPlayers()) {
            if (player.isActive())
                return false;
        }

        return true;
    }

    /**
     * activates player by setting status of all opponents to WAIT and his to [status]
     *
     * @param player player to be activated
     * @param status new status for player
     */
    public void activatePlayer(Player player, String status) {
        System.out.printf("Set player %s to status %s%n", player.getId(), status);

        //due to property change listeners first all stati need to be set to WAIT
        //and afterwards active player status must be set
        for (Player p : players) {
            //compare id because if active player is set to WAIT and afterwards to correct status,
            //client will receive 2 status update messages
            if (p.getId() != player.getId())
                p.setStatus(WAIT);
        }

        player.setStatus(status);
    }

    //endregion

    //region place buildings handling

    /**
     * checks if the player has the permission to build a building
     *
     * @param building check permission to build building
     * @return true if the player has permission to place the building on board, otherwise false
     */
    private boolean isValidBuildRequest(Building building, boolean throwException) throws CatanException {
        boolean hasBuiltPermission = false;
        String exText = "";

        //during the building phase no cities can be built and no raw materials are reduced
        if (this.initialPhaseActive && !building.isCity()) {
            //in initial building phase a road can only be placed next to last settlement that was built
            if (building.isRoad()) {
                Building settlement = getPlayer(building.getOwner()).getLastSettlement();
                if (settlement != null &&
                        settlement.isBuiltAroundHere(building.getLocations(), false))
                    hasBuiltPermission = true;

                else exText = "Die Straße muss neben die zuletzt platzierte Siedlung gebaut werden.";
            } else hasBuiltPermission = true;
        }

        //the player can only built building if he has enough raw materials
        else if (!this.initialPhaseActive) {
            Player player = getPlayer(building.getOwner());
            if (!player.canAfford(building.getType()))
                exText = String.format("Du kannst dir keine %s leisten.", building.getType().toString());

            else if (player.getStock().getCount(building.getType()) < 1)
                exText = String.format("Du kannst keine %s mehr bauen, da du schon alle gebaut hast", building.getType().toString());

            else hasBuiltPermission = true;

        } else exText = "In der initialen Phase kann keine Stadt gebaut werden";

        if (throwException && !hasBuiltPermission) throw new CatanException(exText, true);
        return hasBuiltPermission;
    }

    /**
     * places building if possible and does follow-up work
     *
     * @param building building to be placed
     * @return true if building was placed, otherwise false
     */
    public boolean placeBuilding(Building building, boolean throwException) throws CatanException {
        boolean built;

        if (!isValidBuildRequest(building, throwException)) return false;

        built = board.placeBuilding(building, this.initialPhaseActive);

        //if the building was successfully built do follow-up work
        if (built) {
            Player owner = getPlayer(building.getOwner());
            owner.addBuilding(building);

            //add building to haven array in player so he can trade with it
            //havens are only added for settlements (if a city is placed then the haven was already
            //added when the sLocs was placed)
            if (building.isSettlement()) {
                Haven haven = board.getConnectedHaven(building);
                if (haven != null) owner.addHaven(haven);
            }

            //increase victory points
            if (!building.isRoad()) {
                // always add 1 victory point because a new settlement brings 1 victory point and
                // if a city was built then the owner has already received 1 victory point for the settlement he built before
                owner.increaseVictoryPoints(1, true);
            }

            //raw material distribution for second settlement
            if (initialPhaseActive && (round == 2 || round == 3) && building.isSettlement()) {
                distributeRawMaterial(building);
            }

            //decrease raw materials
            if (!initialPhaseActive) owner.decreaseRawMaterials(Building.getCosts(building.getType()));

            updateStatus(owner.getId());
        }
        return built;

    }

    public boolean placeBuilding(Building building) throws CatanException {
        return placeBuilding(building, false);
    }
    //endregion

    /**
     * updates status of player if the building phase is active or just ended
     *
     * @param id id of the player
     */
    private void updateStatus(int id) {
        if (getCurrent().getId() != id) {
            System.out.printf("Die IDs %s und %s stimmen nicht überein\n", getCurrent().getId(), id);
            return;
        }

        if (initialPhaseActive) {
            String status = Constants.BUILD_STREET;
            Player player = getCurrent();

            if (player.getStatus().equals(BUILD_STREET)) {
                player = nextMove();
                status = initialPhaseActive ? BUILD_SETTLEMENT : DICE;
            }

            activatePlayer(player.getId(), status);
        }
    }

    //region development card handling

    /**
     * <method name: buyDevelopmentCard>
     * <description: this method performs the actions required to buy development card>
     * <preconditions: player has the required cards to buy and his turn is up>
     * <postconditions: player gets a development card in exchange for his material cards>
     */
    public void buyDevelopmentCard(Player player) throws CatanException {
        if (!player.canAffordDevCard())
            throw new CatanException("Du kannst dir keine Entwicklungskarte leisten.", true);

        RawMaterialOverview price = new RawMaterialOverview(0, 1, 0, 1, 1);

        player.decreaseRawMaterials(price);
        DevCardType type = developmentCardDeck.withdrawRandomCard();
        player.addDevelopmentCard(type, 1);
        this.rawMaterialDeck.increase(price);
    }


    public void applyMonopoleCard(Player monoPlayer, RawMaterialType targetType) throws CatanException {
        if (!monoPlayer.hasMonopoleCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, monoPlayer.getId(), DevCardType.MONOPOLE.toString()), true);

        monoPlayer.removeDevelopmentCard(DevCardType.MONOPOLE, 1);

        for (Player player : players) {
            if (player != monoPlayer && player.hasRawMaterial(targetType)) {
                RawMaterialOverview overview =
                        new RawMaterialOverview(targetType, player.getRawMaterialCount(targetType));
                player.decreaseRawMaterials(overview);
                monoPlayer.increaseRawMaterials(overview);
            }
        }
    }

    public void applyRoadConstructionCard(Player player, Building road1, Building road2) throws CatanException {
        if (!player.hasRoadConstructionCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, player.getId(), DevCardType.ROAD_CONSTRUCTION.toString()), true);

        //add roads to board directly
        boolean built = this.board.placeBuilding(road1, false);

        if (built) {
            built = this.board.placeBuilding(road2, false);
        }

        //remove construction card
        if (built) {
            player.removeDevelopmentCard(DevCardType.ROAD_CONSTRUCTION);
        }
    }

    public void applyInventionCard(Player player, RawMaterialOverview overview) throws CatanException {
        if (!player.hasInventionCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, player.getId(), DevCardType.INVENTION.toString()), true);

        else if (overview.getTotalCount() != 2)
            throw new CatanException(String.format("Es können genau 2 Rohstoffe gezogen werden, nicht %s", overview.getTotalCount()), true);

        this.rawMaterialDeck.decrease(overview);
        player.increaseRawMaterials(overview);
        player.removeDevelopmentCard(DevCardType.INVENTION, 1);
    }
    //endregion

    //region field distribution handling
    public Map<Integer, RawMaterialOverview> mapOwnerWithHarvest(int number)
            throws CatanException {

        Map<Integer, RawMaterialOverview> distrMap = new HashMap<>();

        Map<Building, RawMaterialType> bldMap =
                board.getBuildingsOnDistributingFields(number);

        for (Building bld : bldMap.keySet()) {
            int amount = bld.isCity() ? 2 : 1;

            if (distrMap.containsKey(bld.getOwner())) {
                RawMaterialOverview overview = distrMap.get(bld.getOwner());
                overview.increase(bldMap.get(bld), amount);
                distrMap.replace(bld.getOwner(), overview);

            } else distrMap.put(bld.getOwner(),
                    new RawMaterialOverview(bldMap.get(bld), amount));
        }

        return distrMap;
    }

    /**
     * distributes raw materials for one building
     *
     * @param building
     */
    public void distributeRawMaterial(Building building) throws CatanException {
        if (!building.isRoad()) {
            RawMaterialOverview overview = board.getHarvest(building);
            getPlayer(building.getOwner()).increaseRawMaterials(overview);
        }
    }

    public void distributeRawMaterial(Map<Integer, RawMaterialOverview> distribution) throws CatanException {

        for (Player p : players) {
            if (distribution.containsKey(p.getId())) {
                RawMaterialOverview harvest = distribution.get(p.getId());
                p.increaseRawMaterials(harvest);
            }
        }
    }
    //endregion

    //region trade handling
    public void domesticTrade(TradeRequest tradeRequest, int offerentId, int fellowPlayerId)
            throws CatanException {
        Player offerent = getPlayer(offerentId);
        Player fellowPlayer = getPlayer(fellowPlayerId);

        if (!offerent.canAfford(tradeRequest.getOffer()))
            throw new CatanException("Der Anbieter besitzt die angebotenen Rohstoffe nicht.", true);

        if (!fellowPlayer.canAfford(tradeRequest.getRequest()))
            throw new CatanException("Du besitzt die angeforderten Rohstoffe nicht.", true);

        if (!tradeRequest.canBeExecutedBy(fellowPlayerId))
            throw new CatanException("Der Handel kann nicht mit dir ausgeführt werden. Hast du ihn angenommen?", true);

        offerent.trade(tradeRequest.getOffer(), tradeRequest.getRequest());
        fellowPlayer.trade(tradeRequest.getRequest(), tradeRequest.getOffer());

        tradeRequest.execute(fellowPlayerId);
    }

    /**
     * performs sea trade
     * client sends one RawMaterialOverview for offer and one for request
     *
     * @param player       player who offered sea trade
     * @param tradeRequest trade request
     */
    public void seaTrade(Player player, TradeRequest tradeRequest) throws CatanException {

        //client sends raw material overview containing 1 clay/ore/.. ore for request/offer
        if (tradeRequest.getRequest().getTotalCount() != 1 &&
                tradeRequest.getOffer().getTotalCount() != 1)
            throw new CatanException(
                    "Die Anfrage ist nicht korrekt. Sie muss für Angebot und Nachfrage je einen Rohstoff enthalten.",
                    true);

        RawMaterialType requestType = tradeRequest.getRequest().getType();
        RawMaterialType offerType = tradeRequest.getOffer().getType();

        if (!requestType.isValidTradingType() || !offerType.isValidTradingType())
            throw new CatanException("Mit diesem Rohstoff kann nicht gehandelt werden.", true);

        if (requestType.equals(offerType))
            throw new CatanException("Angebot und Nachfrage müssen unterschiedliche Rohstoffe beinhalten",
                    true);

        //check if player has 2:1 haven for trading
        Haven haven = player.get2To1Haven(offerType);
        RawMaterialOverview offer, request;
        int offerAmount = 2;

        if (haven == null) {
            offerAmount = player.hasXTo1Haven(3) ? 3 : 4;
        }

        offer = new RawMaterialOverview(offerType, offerAmount);
        request = new RawMaterialOverview(requestType, 1);

        if (!player.canAfford(offer)) throw new CatanException("Du hast nicht genug Rohstoffe für diesen Handel.", true);

        player.trade(offer, request);
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

    public void addPlayer(Player p) throws CatanException {
        if (gameStarted) throw new CatanException("Das Spiel wurde bereits gestartet.");

        if (players.size() < 4) {

            players.add(p);
            //if (p.isKI()) p.addPropertyChangeListener(this);

        } else throw new CatanException("Es sind bereits genug Spieler verbunden.");
    }

    /**
     * adds new KI to game
     *
     * @throws IllegalAccessException
     */
    public Player addKI() throws CatanException {
        if (gameStarted)
            throw new CatanException("Das Spiel ist bereits gestartet.");

        Player player = new Player(getValidId(), getValidColor());
        player.setName("KI(" + player.getColor() + ")");
        setKI(player);
        addPlayer(player);
        return player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getRealPlayers() {
        List<Player> realPlayers = new ArrayList<>();

        for (Player player : this.players) {
            if (!player.isKI()) realPlayers.add(player);
        }

        return realPlayers;
    }

    public void setKI(int id) {
        setKI(getPlayer(id));
    }

    public void setKI(Player player) {
        //player.addPropertyChangeListener(this);
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
    private Color getValidColor() throws CatanException {
        for (Color color : Color.values()) {
            if (isValidColor(color))
                return color;
        }

        throw new CatanException("Alle Farben sind bereits vergeben", false);
    }

    //endregion

    //region robber handling

    /**
     * moves robber and exchanges raw materials
     *
     * @param playerId     id of player moving the robber
     * @param opponentId   id of player being robbed
     * @param newRobberLoc new location of robber
     * @return id of player that was robbed
     */
    public void activateRobber(int playerId, int opponentId, Location newRobberLoc) throws CatanException {
        //check if player has turn
        if (getCurrent().getId() != playerId) CatanException.throwNotYourTurnException(getCurrent().getId());

        //move robber to target location
        board.getRobber().move(newRobberLoc);
        Player opponent = getPlayer(opponentId);
        Player currentPlayer = getCurrent();
        HashSet<Integer> owners = board.getOwnersToRobFrom(playerId, newRobberLoc);

        //target was given
        if (opponent != null && owners.contains(opponentId)) {

            //it might be that the target player does not possess any raw materials
            //in this case the rob is stated as successful though because the target
            //was specifically determined by the client
            board.getRobber().robPlayer(currentPlayer, opponent);

        } else {

            //get first player who can be robbed and rob random raw material
            for (int owner : owners) {
                opponent = getPlayer(owner);

                if (opponent != null && opponent.getRawMaterialCount() > 0) {
                    board.getRobber().robPlayer(currentPlayer, opponent);
                    break;
                }
                else { opponent = null; }
            }
        }

        changes.firePropertyChange(ROBBER_AT, false, CatanMessage.robberMoved(getCurrent(), board.getRobber(), opponent));

        currentPlayer.setStatus(TRADE_OR_BUILD);
    }

    /**
     * player tosses cards after a roll of 7 if he has at least 7 raw materials
     *
     * @param id id of the player who has to toss cards
     * @param overview overview containing all cards he extracts
     */
    public void tossRawMaterialCards(int id, RawMaterialOverview overview) throws CatanException {
        Player player = getPlayer(id);

        int toWithdraw = player.getRawMaterialCount()/2;
        if (overview.getTotalCount() != toWithdraw)
            throw new CatanException(
                    String.format("Du besitzt %s Rohstoffe und musst deswegen %s und nicht %s abgeben.",
                            player.getRawMaterialCount(), toWithdraw, overview.getTotalCount()), true);
        board.getRobber().robPlayer(player, overview);

        //set new status for player
        player.activateNextStatus();
    }
    //endregion

    //region dice
    public void dice(int id) throws CatanException {
        if (getCurrent().getId() != id) CatanException.throwNotYourTurnException(getCurrent().getId());
        Player player = getCurrent();

        System.out.printf("%s dices" , player.toString());
        int[] dice = player.throwDice();
        int sum = dice[0] + dice[1];

        //the robber is activated
        if (sum == 7) {
            //players have to extract cards
            extractCardsDueToRobber();

            //if the active player must not extract cards he can move the robber
            if (!player.getStatus().equals(EXTRACT_CARDS_DUE_TO_ROBBER)) {
                if (player.isKI()) {
                    player.setNextStatus(ROBBER_TO);
                    deactivateKI(player);
                }
                else { player.setStatus(ROBBER_TO); }
            }
        }

        //distribute raw materials
        else {
            Map<Integer, RawMaterialOverview> distribution =
                    mapOwnerWithHarvest(sum);

            distributeRawMaterial(distribution);

            //after raw material distribution player can continue his turn
            activatePlayer(id, TRADE_OR_BUILD);
        }
    }

    /**
     * updates status of players who have to toss cards
     */
    public void extractCardsDueToRobber() {
        List<Player> kiList = new ArrayList<>();
        int id = getCurrent().getId();

        //first update non KI players because otherwise there will be problems
        //with status updates
        for (Player player : getPlayers()) {
            if (player.hasToExtractCards()) {
                if (player.isKI()){ kiList.add(player); }

                else {
                    String nextStatus = (player.getId() == id) ? ROBBER_TO : WAIT;
                    player.setNextStatus(nextStatus);
                    player.setStatus(EXTRACT_CARDS_DUE_TO_ROBBER);
                }
            }
        }

        //update KIs
        for (Player player : kiList) {
            String nextStatus = (player.getId() == id) ? ROBBER_TO : WAIT;
            player.setNextStatus(nextStatus);
            player.setStatus(EXTRACT_CARDS_DUE_TO_ROBBER);
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

    public boolean isInitialPhaseActive() {
        return initialPhaseActive;
    }

    public void setInitialPhaseActive(boolean active) {
        this.initialPhaseActive = active;
    }

    public Board getBoard() {
        return board;
    }

    public int getRound() {
        return this.round;
    }

    public boolean endGame(Player winner) {
        if (!winner.hasWon() || winner.getId() != getPlayerWithHighestPoints().getId()) {
            return false;
        }
        this.isGameOver = true;
        return true;
    }

    public Player getPlayerWithHighestPoints() {
        Player topPlayer = players.get(0);
        for (Player player : players) {
            if (player.getVictoryPointsCount() > topPlayer.getVictoryPointsCount()) {
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
            player.increaseVictoryPoints(2, true);
            player.setGreatestArmy(true);
            this.greatestArmyPlayer = player;
            done = true;
        } else {

            if (this.getPlayerWithHighestArmyCount() == player && this.greatestArmyPlayer != player) {

                try {
                    greatestArmyPlayer.decreaseVictoryPoints(2);
                    greatestArmyPlayer.setGreatestArmy(false);
                    player.increaseVictoryPoints(2, true);
                    player.setGreatestArmy(true);
                    this.greatestArmyPlayer = player;
                    done = true;
                } catch (Exception ex) {
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
}

