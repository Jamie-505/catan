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
    private boolean gameOver;
    private boolean gameStarted;
    private Player playerWithLongestRoad;
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
        gameOver = false;
        gameStarted = false;
        playerWithLongestRoad = null;
    }

    //region initialize

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
     *
     * @return player to turn next
     */
    public Player nextMove() {
        return this.initialPhaseActive ? next_initialPhase() : next_gameStarted();
    }

    /**
     * will be called if game is started aleady and building phase is over
     *
     * @return player to turn next
     */
    private Player next_gameStarted() {
        currentTurn++;
        if (!initialPhaseActive) activatePlayer(getCurrent().getId(), DICE);
        return players.get(this.getCurrentIndex());
    }

    /**
     * will be called if the initial phase is active
     *
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

    //endregion
    //region KI handling

    /**
     * moves KIs until real player has turn
     */
    public void moveKIs() {
        Player current = getCurrent();

        //status WAIT_FOR_ALL_TO_EXTRACT_CARDS is no active status --> update if everybody is
        //ready now, otherwise wait ahead
        if (current.isKI() && current.getStatus().equals(WAIT_FOR_ALL_TO_EXTRACT_CARDS))
            moveKI(current);

        if (!getCurrent().isKI() || !getCurrent().isActive())
            return;

        while (getCurrent().isActive() && getCurrent().isKI()) {
            moveKI(getCurrent());
        }
    }

    /**
     * performs move for KI according to its status
     *
     * @param ki ki to be moved
     * @throws CatanException
     */
    public void moveKI(Player ki) {
        boolean nextMove = false;

        try {
            switch (ki.getStatus()) {
                case START_GAME:
                    ki.setStatus(WAIT_FOR_GAME_START);
                    nextMove = false;
                    break;

                case DICE:
                    dice(ki.getId());
                    nextMove = false;
                    break;

                case BUILD_SETTLEMENT:
                    Building settlement = new Building(ki.getId(),
                            BuildingType.SETTLEMENT, board.getRandomFreeSettlementLoc());
                    placeBuilding(settlement, true);
                    nextMove = false;
                    break;

                case BUILD_STREET:
                    Building street = new Building(ki.getId(), BuildingType.ROAD,
                            board.getFreeRoadLoc(ki, initialPhaseActive));
                    placeBuilding(street, true);
                    nextMove = false;
                    break;

                case TRADE_OR_BUILD:
                    //play development card if ki possesses one
                    try {
                        if (ki.hasInventionCard()) applyInventionCard(ki);
                        else if (ki.hasRoadConstructionCard()) applyRCCardForKI(ki);
                        else if (ki.hasMonopoleCard()) applyMonopoleCard(ki, RawMaterialType.getRandomTradingType());
                        else if (ki.hasKnightCard()) applyKnightCard(ki.getId(), -1, board.getRandomFieldLoc());
                    } catch (CatanException ex) {
                        System.out.println("Dieser Zug war nicht gültig, versuche es beim nächsten Mal nochmal");
                    }

                    if (ki.canAfford(BuildingType.SETTLEMENT)) {
                        Building s = new Building(ki.getId(), BuildingType.SETTLEMENT, board.getRandomFreeSettlementLoc());
                        placeBuilding(s, true);
                    } else if (ki.canAfford(BuildingType.ROAD)) {
                        Building r = new Building(ki.getId(), BuildingType.ROAD, board.getFreeRoadLoc(ki, initialPhaseActive));
                        placeBuilding(r, true);
                    } else if (ki.canAffordDevCard()) {
                        buyDevelopmentCard(ki);
                    }
                    nextMove = true;
                    break;

                case ROBBER_TO:
                    if (waitForPlayerToExtractCards(ki.getId()))
                        nextMove = false;

                    else {
                        boolean activated;
                        try {
                            activateRobber(ki.getId(), -1, board.getRandomFieldLoc());
                            activated = true;
                        } catch (CatanException ex) {
                            System.out.println(ex.getMessage());
                            activated = false;
                        }

                        //try second time
                        if (!activated)
                            activateRobber(ki.getId(), -1, board.getRandomFieldLoc());

                        nextMove = false;
                    }
                    break;

                case EXTRACT_CARDS_DUE_TO_ROBBER:
                    //KI has to extract cards
                    tossRawMaterialCards(ki);
                    updateStatusAfterCardExtraction(ki);
                    nextMove = false;
                    break;

                case WAIT_FOR_ALL_TO_EXTRACT_CARDS:
                    if (!waitForPlayerToExtractCards(ki.getId()))
                        updateStatusAfterCardExtraction(ki);
                    nextMove = false;

            }
        } catch (CatanException ex) {
            System.out.println(ex.getMessage());
            ki.setStatus(WAIT, false);
        }
        if (nextMove) nextMove();
    }

    public boolean waitForPlayerToExtractCards(int id) {
        for (Player player : getRealPlayers()) {
            if (player.getStatus().equals(EXTRACT_CARDS_DUE_TO_ROBBER) && player.getId() != id)
                return true;
        }
        return false;
    }

    public void activatePlayer(int id, String status) {
        activatePlayer(getPlayer(id), status);
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
            if (!p.equals(player.getId())) p.setStatus(WAIT);
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
            if (!player.canAfford(building.getType()) && !player.isRCActive())
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
                // always add 1 victory point because:
                // a new settlement brings 1 victory point and
                // if a city was built then the owner has already received 1 victory point for the settlement he built before
                owner.increaseVictoryPoints(1, true);
            } else {
              //if owner has new longest road, update it
              assignLongestRoad(building, owner);
            }

            //raw material distribution for second settlement
            if (initialPhaseActive && (round == 2 || round == 3) && building.isSettlement()) {
                distributeRawMaterial(building);
            }

            //decrease raw materials
            if (!initialPhaseActive&& !owner.isRCActive()) owner.decreaseRawMaterials(Building.getCosts(building.getType()));
            if(owner.getStatus().equals(Constants.FIRST_STREET)) owner.removeDevelopmentCard(DevCardType.ROAD_CONSTRUCTION);
            updateStatus(owner.getId());
        }
        return built;

    }

    public boolean placeBuilding(Building building) throws CatanException {
        return placeBuilding(building, false);
    }

    /**
     * assigns longest road to owner if he has longest road
     *
     * @param road  new road which has been placed on board
     * @param owner owner of new road
     * @throws CatanException
     */
    void assignLongestRoad(Building road, Player owner) throws CatanException {
      //if the player has less than 5 roads or less then the current longest road he cant possibly get the longest road
      if (owner.getRoads().size() < Math.max(5, board.getLongestRoadLength()) ) return;

      int newRoadLength = getBoard().getLongestRoad(road, owner, false, false).size();

      if (newRoadLength > owner.getLongestRoadLength())
        owner.setLongestRoadLength(newRoadLength);

      if (isLongestRoad(newRoadLength) && newRoadLength >= 5) {
        getBoard().setLongestRoadLength(newRoadLength);
        if (playerWithLongestRoad == null) {
          owner.setHasLongestRoad(true);
          playerWithLongestRoad = owner;
          playerWithLongestRoad.increaseVictoryPoints(2, true);
        }

        if (owner != playerWithLongestRoad) {
          playerWithLongestRoad.setHasLongestRoad(false);
          owner.setHasLongestRoad(true);
          playerWithLongestRoad.decreaseVictoryPoints(2, true);
          playerWithLongestRoad = owner;
          playerWithLongestRoad.increaseVictoryPoints(2, true);
        }
      }
    }

    private boolean isLongestRoad(int newRoadLength) {
        return board.getLongestRoadLength() < newRoadLength;
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
        Player player = getCurrent();

        if (initialPhaseActive) {
            String status = Constants.BUILD_STREET;

            if (player.getStatus().equals(BUILD_STREET)) {
                player = nextMove();
                status = initialPhaseActive ? BUILD_SETTLEMENT : DICE;
            }

            activatePlayer(player.getId(), status);
        }

        else if (player.isRCActive())  {
            String newStatus = player.getStatus().equals(FIRST_STREET) ? SECOND_STREET : TRADE_OR_BUILD;
            player.setStatus(newStatus);
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

        DevCardType type = developmentCardDeck.withdrawRandomCard();
        player.addDevelopmentCard(type, 1);
        player.decreaseRawMaterials(price);
        this.rawMaterialDeck.increase(price);
    }


    public void applyMonopoleCard(Player monoPlayer, RawMaterialType targetType) throws CatanException {
        if (!monoPlayer.hasMonopoleCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, monoPlayer.getId(), DevCardType.MONOPOLE.toString()), true);

        changes.firePropertyChange(MONOPOLE_PLAYED, targetType, monoPlayer);

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

    public void applyRCCardForKI(Player ki) throws CatanException {

        if (!ki.isKI()) return;

        if (!ki.hasRoadConstructionCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, ki.getId(), DevCardType.ROAD_CONSTRUCTION.toString()), true);

        ki.setStatus(FIRST_STREET);

        try {
            Building road1 = new Building(ki.getId(), BuildingType.ROAD, board.getFreeRoadLoc(ki, false));

            boolean built = this.board.placeBuilding(road1, false);

            if (built) {
                Building road2 = new Building(ki.getId(), BuildingType.ROAD, board.getFreeRoadLoc(ki, false));
                this.board.placeBuilding(road2, false);
            }
        }
        catch (CatanException ex) {
            System.out.println("KI "+ ki.getName() + " löst eine Exception aus: " + ex.getMessage());
        }
        finally {
            ki.removeDevelopmentCard(DevCardType.ROAD_CONSTRUCTION);
        }
    }

    public void applyInventionCard(Player player, RawMaterialOverview overview) throws CatanException {
        if (!player.hasInventionCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, player.getId(), DevCardType.INVENTION.toString()), true);

        if (overview.getTotalCount() != 2)
            throw new CatanException(String.format("Es können genau 2 Rohstoffe gezogen werden, nicht %s", overview.getTotalCount()), true);

        changes.firePropertyChange(INVENTION_PLAYED, "null", player);

        this.rawMaterialDeck.decrease(overview);
        player.increaseRawMaterials(overview);
        player.removeDevelopmentCard(DevCardType.INVENTION, 1);


        changes.firePropertyChange(INVENTION_PLAYED, overview, player);
    }

    /**
     * applies invention card withdrawing random cards from
     * game controller deck
     *
     * @param player
     * @throws CatanException
     */
    public void applyInventionCard(Player player) throws CatanException {
        if (!player.hasInventionCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, player.getId(), DevCardType.INVENTION.toString()), true);

        List<RawMaterialType> types = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            RawMaterialType type = this.rawMaterialDeck.withdrawRandomCard();
            types.add(type);
        }

        RawMaterialOverview overview = new RawMaterialOverview();

        for (RawMaterialType type : types) {
            overview.increase(type, 1);
        }

        changes.firePropertyChange(INVENTION_PLAYED, types, player);

        player.increaseRawMaterials(overview);
    }


    public void applyKnightCard(int knightPlayerId, int targetPlayerId, Location newRobberLoc) throws CatanException {
        Player knightPlayer = getPlayer(knightPlayerId);
        if (!knightPlayer.equals(getCurrent()))
            CatanException.throwNotYourTurnException(knightPlayerId);

        if (!knightPlayer.hasInventionCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, knightPlayer.getId(), DevCardType.KNIGHT.toString()), true);

        knightPlayer.applyKnightCard();
        activateRobber(knightPlayerId, targetPlayerId, newRobberLoc);

        Object[] data = new Object[2];
        data[0] = targetPlayerId;
        data[1] = newRobberLoc;
        changes.firePropertyChange(KNIGHT_PLAYED, data, knightPlayer);
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
        if (gameStarted) throw new CatanException("Das Spiel wurde bereits gestartet.", true);

        if (players.size() < 4) {
            players.add(p);

        } else throw new CatanException("Es sind bereits genug Spieler verbunden.", true);
    }

    /**
     * adds new KI to game
     *
     * @throws CatanException if game is already started
     */
    public Player addKI() throws CatanException {
        if (gameStarted)
            throw new CatanException("Das Spiel ist bereits gestartet.", true);

        Player player = new Player(getValidId(), getValidColor());
        player.setName("KI(" + player.getColor() + ")");
        player.setKI(true);
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
                } else { opponent = null; }
            }
        }

        changes.firePropertyChange(ROBBER_AT, false, CatanMessage.robberMoved(getCurrent(), board.getRobber(), opponent));

        currentPlayer.setStatus(TRADE_OR_BUILD);
    }

    /**
     * toss raw material cards and update status of current player to ROBBER_TO
     *
     * @param id       id of player who tossed cards
     * @param overview overview of raw materials to extract
     * @throws CatanException if overview contains invalid raw materials
     */
    public void tossCardsAndUpdateStatus(int id, RawMaterialOverview overview) throws CatanException {
        tossRawMaterialCards(id, overview);

        Player player = getPlayer(id);
        updateStatusAfterCardExtraction(player);
    }

    /**
     * player tosses cards after a roll of 7 if he has at least 7 raw materials
     *
     * @param id id of the player who has to toss cards
     * @param overview overview containing all cards he extracts
     */
    public void tossRawMaterialCards(int id, RawMaterialOverview overview) throws CatanException {
        Player player = getPlayer(id);

        int toWithdraw = player.getRawMaterialCount() / 2;
        if (overview.getTotalCount() != toWithdraw)
            throw new CatanException(
                    String.format("Du besitzt %s Rohstoffe und musst deswegen %s und nicht %s abgeben.",
                            player.getRawMaterialCount(), toWithdraw, overview.getTotalCount()), true);
        board.getRobber().robPlayer(player, overview);
    }

    public void tossRawMaterialCards(Player player) throws CatanException {
        board.getRobber().robPlayer(player);
    }

    public void updateStatusAfterCardExtraction(Player player) {
        String nextStatus = WAIT;

        if (player.getNextStatus().equals(ROBBER_TO) || player.getStatus().equals(WAIT_FOR_ALL_TO_EXTRACT_CARDS)) {
            nextStatus = waitForPlayerToExtractCards(player.getId()) ? WAIT_FOR_ALL_TO_EXTRACT_CARDS : ROBBER_TO;
        }

        player.setNextStatus(WAIT);
        player.setStatus(nextStatus);
    }
    //endregion

    //region dice
    public void dice(int id) throws CatanException {
        if (getCurrent().getId() != id) CatanException.throwNotYourTurnException(getCurrent().getId());
        Player player = getCurrent();

        System.out.printf("%s dices ", player.toString());
        int[] dice = player.throwDice();
        int sum = dice[0] + dice[1];

        //the robber is activated
        if (sum == 7) {
            setNextStatusAfterDiceOf7();

            //players have to extract cards
            extractCardsDueToRobber();
        }

        //distribute raw materials
        else {
            Map<Integer, RawMaterialOverview> distribution =
                    mapOwnerWithHarvest(sum);

            distributeRawMaterial(distribution);

            //after raw material distribution player can continue his turn
            activatePlayer(player, TRADE_OR_BUILD);
        }
    }

    public void setNextStatusAfterDiceOf7() {
        for (Player player : players) {
            String nextStatus = player.getId() == getCurrent().getId() ? ROBBER_TO : WAIT;
            player.setNextStatus(nextStatus);
        }
    }

    /**
     * updates status of players who have to toss cards
     * moveKIs if they have turn
     */
    public void extractCardsDueToRobber() {
        Player current = getCurrent();

        //first update all inactive players because the active one may rob
        //another player afterwards and first, everybody has to extract cards
        for (Player player : getPlayers()) {
            if (player.equals(current))
                continue;

            if (player.hasToExtractCards()) {
                player.setStatus(EXTRACT_CARDS_DUE_TO_ROBBER);

                //extract cards for KI
                if (player.isKI()) moveKI(player);
            }

            //if player must not extract cards (except for current player
            // he can be set to his next status
            else player.activateNextStatus();
        }

        //extract cards fo KI
        if (current.hasToExtractCards()) {
            current.setStatus(EXTRACT_CARDS_DUE_TO_ROBBER);
            if (current.isKI()) moveKI(current);
        } else updateStatusAfterCardExtraction(current);

        //go ahead if everybody is ready
        if (current.isKI() && current.getStatus().equals(ROBBER_TO))
            moveKIs();
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

    public boolean endGame(Player winner) {
        if (!winner.hasWon() || winner.getId() != getPlayerWithHighestPoints().getId()) {
            return false;
        }
        this.gameOver = true;
        return true;
    }

    public Player getPlayerWithHighestPoints() {
        Player topPlayer = players.get(0);
        for (Player player : players) {
            if (player.getVictoryPoints() > topPlayer.getVictoryPoints()) {
                topPlayer = player;
            }
        }
        return topPlayer;

    }

    public Player getPlayerWithLongestRoad() {
        return playerWithLongestRoad;
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
        return this.gameOver;
    }

    public DevelopmentCardOverview getDevelopmentCardDeck() { return this.developmentCardDeck; }

    public void setDevelopmentCardDeck(DevelopmentCardOverview devCardDeck) { this.developmentCardDeck = devCardDeck; }
    //endregion
}

