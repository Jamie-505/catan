package de.lmu.settleBattle.catanServer;

import static de.lmu.settleBattle.catanServer.Constants.*;

import java.beans.PropertyChangeSupport;
import java.util.*;
import java.beans.PropertyChangeListener;

public class GameController {

    //region property change listener
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }
    //endregion

    private Board board;
    private List<Player> players;
    private int currentTurn;
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
    private void initializeBuildingPhase() {
        playerStack = new Stack<>();
        initialPhaseActive = true;
        round = 1;

    }
    //endregion

    //region turn handling
    public Player getCurrent() {
        return players.get(this.getCurrentIndex());
    }

    private int getCurrentIndex() {
        return currentTurn % players.size();
    }

    /**
     * determines player who can move next
     *
     * @return player to turn next
     */
    public Player nextMove() {
        Player current = getCurrent();
        if (current.hasWon()) {
            endGame(current);
            changes.firePropertyChange(GAME_OVER, "null", current);
            return current;
        }
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

        while (getCurrent().isActive() && getCurrent().isKI() && !gameOver) {
            moveKI(getCurrent());
        }
    }

    /**
     * performs move for KI according to its status
     *
     * @param ki ki to be moved
     */
    public void moveKI(Player ki) {
        boolean nextMove = false;

        try {
            switch (ki.getStatus()) {
                case START_GAME:
                    ki.setStatus(WAIT_FOR_GAME_START);
                    break;

                case DICE:
                    dice(ki.getId());
                    break;

                case BUILD_SETTLEMENT:
                    Building settlement = new Building(ki.getId(),
                            BuildingType.SETTLEMENT, board.getFreeSettlementLoc());
                    placeBuilding(settlement, true);
                    break;

                case BUILD_STREET:
                    Building street = new Building(ki.getId(), BuildingType.ROAD,
                            board.getFreeRoadLoc(ki, initialPhaseActive));
                    placeBuilding(street, true);
                    break;

                case TRADE_OR_BUILD:
                    nextMove = true;

                    //play development card if ki possesses one
                    if (ki.getDevelopmentDeck().getTotalCount() > 0)
                        applyDevCard(ki);

                    if (ki.canAfford(BuildingType.CITY) && ki.getSettlements().size() > 0 && ki.getStock().getCount(BuildingType.CITY) > 0) {
                        Building city = new Building(ki.getId(), BuildingType.CITY, ki.getRandomSettlement().getLocations());
                        placeBuilding(city, true);
                    } else if (ki.canAfford(BuildingType.SETTLEMENT) && ki.getStock().getCount(BuildingType.SETTLEMENT) > 0) {
                        boolean built = placeValidSettlement(ki);

                        // if settlement could not be build then there is no valid location for it
                        // => player must build road to be able to place settlement
                        if (!built && ki.canAfford(BuildingType.ROAD) && ki.getStock().getCount(BuildingType.ROAD) > 0) {
                            Building r = new Building(ki.getId(), BuildingType.ROAD, board.getFreeRoadLoc(ki, initialPhaseActive));
                            placeBuilding(r, true);
                        }
                    } else if (ki.canAfford(BuildingType.ROAD) && ki.getStock().getCount(BuildingType.ROAD) > 0) {
                        Building r = new Building(ki.getId(), BuildingType.ROAD, board.getFreeRoadLoc(ki, initialPhaseActive));
                        placeBuilding(r, true);
                    } else if (ki.canAffordDevCard()) {
                        buyDevelopmentCard(ki);
                    }
                    break;

                case ROBBER_TO:
                    if (!waitForPlayerToExtractCards(ki.getId()))
                        activateRobber(ki.getId(), -1, board.getNewLocForRobber(ki.getId()));
                    break;

                case EXTRACT_CARDS_DUE_TO_ROBBER:
                    //KI has to extract cards
                    tossRawMaterialCards(ki);
                    updateStatusAfterCardExtraction(ki);
                    break;

                case WAIT_FOR_ALL_TO_EXTRACT_CARDS:
                    if (!waitForPlayerToExtractCards(ki.getId()))
                        updateStatusAfterCardExtraction(ki);

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

    public void makeKIsRespondToTr(TradeRequest tr) {
        List<Player> kis = getKIs();

        if (kis.size() == 0) {
            return;
        }

        for (Player ki : kis) {
            boolean accept = ki.canAfford(tr.getRequest()) && ki.shouldAcceptTradeRequest(tr);
            tr.accept(accept, ki.getId());
        }
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
            if (!initialPhaseActive && !owner.isRCActive())
                owner.decreaseRawMaterials(Building.getCosts(building.getType()));

            if (owner.getStatus().equals(Constants.FIRST_STREET))
                owner.removeDevelopmentCard(DevCardType.ROAD_CONSTRUCTION);

            updateStatus(owner.getId());
        }
        return built;
    }

    public boolean placeBuilding(Building building) throws CatanException {
        return placeBuilding(building, false);
    }


    /**
     * places settlement at random valid location for player
     *
     * @param owner owner of new settlement
     */
    private boolean placeValidSettlement(Player owner) {
        //iterate through rows and check for each if settlement can be placed next to it
        for (Building road : owner.getRoads()) {
            List<Location[]> locs = board.getAdjacentSettlementLocs(road.getLocations());
            if (locs == null) continue;

            boolean built;

            for (Location[] sLoc : locs) {
                try {
                    Building settlement = new Building(owner.getId(), BuildingType.SETTLEMENT, sLoc);
                    built = placeBuilding(settlement, false);
                    if (built) return true;
                } catch (CatanException ex) {
                    //Location was not valid, try ahead
                }
            }
        }

        return false;
    }


    /**
     * assigns longest road to owner if he has longest road
     *
     * @param road  new road which has been placed on board
     * @param owner owner of new road
     * @throws CatanException
     */
    void assignLongestRoad(Building road, Player owner) throws CatanException {
        //if the player has less than 5 roads or less than the current longest road he cant possibly get the longest road
        if (owner.getRoads().size() < Math.max(5, board.getLongestRoadLength())) return;

        int newRoadLength = board.getLongestRoad(road, owner, false, false).size();

        //update length of longest road of current player if it increased
        if (newRoadLength > owner.getLongestRoadLength())
            owner.setLongestRoadLength(newRoadLength);

        if (isLongestRoad(newRoadLength)) {
            board.setLongestRoadLength(newRoadLength);
            owner.setHasLongestRoad(true);

            if (playerWithLongestRoad == null) {
                playerWithLongestRoad = owner;
                playerWithLongestRoad.increaseVictoryPoints(2, true);
            }

            if (owner != playerWithLongestRoad) {
                playerWithLongestRoad.setHasLongestRoad(false);
                playerWithLongestRoad.decreaseVictoryPoints(2, true);
                playerWithLongestRoad = owner;
                playerWithLongestRoad.increaseVictoryPoints(2, true);
            }
        }
    }

    private boolean isLongestRoad(int newRoadLength) {
        return board.getLongestRoadLength() < newRoadLength && newRoadLength >= 5;
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
            if (player.getStatus().equals(SECOND_STREET))
                endRCCard(player.getId());

            else player.setStatus(SECOND_STREET);
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

    /**
     * applies random development card for KI
     *
     * @param ki KI which has turn
     */
    private void applyDevCard(Player ki) {
        List<DevCardType> types = ki.getDevelopmentDeck().getApplicableTypes();
        if (types.size()==0)return;

        Random random = new Random();
        int index = random.nextInt(types.size());

        try {
            switch (types.get(index)) {
                case KNIGHT:
                    applyKnightCard(ki.getId(), -1, board.getNewLocForRobber(ki.getId()));
                    break;
                case INVENTION:
                    applyInventionCard(ki);
                    break;
                case MONOPOLE:
                    applyMonopoleCard(ki, RawMaterialType.getRandomTradingType());
                    break;
                case ROAD_CONSTRUCTION:
                    applyRCCard(ki);
                    break;

            }
        } catch (CatanException ex) {
            System.out.println(ki.getName() + "löste eine Exception aus: " + ex.getMessage());
        }
    }

    /**
     * applies monopole card for player
     *
     * @param monoPlayer player who applies card
     * @param targetType raw material type he will receive from everybody
     * @throws CatanException if player has no monopole card
     */
    public void applyMonopoleCard(Player monoPlayer, RawMaterialType targetType) throws CatanException {
        if (!monoPlayer.hasMonopoleCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, monoPlayer.getId(), DevCardType.MONOPOLE.toString()), true);

        monoPlayer.removeDevelopmentCard(DevCardType.MONOPOLE, 1);

        for (Player player : players) {
            if (!player.equals(monoPlayer) && player.hasRawMaterial(targetType)) {
                RawMaterialOverview overview =
                        new RawMaterialOverview(targetType, player.getRawMaterialCount(targetType));
                player.decreaseRawMaterials(overview);
                monoPlayer.increaseRawMaterials(overview);
            }
        }

        monoPlayer.setStatus(TRADE_OR_BUILD);

        changes.firePropertyChange(MONOPOLE_PLAYED, targetType, monoPlayer);
    }

    /**
     * applies road construction for KI
     *
     * @param ki player who applies road construction card
     *           roads will be chosen randomly and card will be removed immediately
     * @throws CatanException if player has no rc card
     */
    public void applyRCCard(Player ki) throws CatanException {

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
        } catch (CatanException ex) {
            System.out.println("KI " + ki.getName() + " löste eine Exception aus: " + ex.getMessage());
            ki.setStatus(TRADE_OR_BUILD);
        }
        finally {
            ki.removeDevelopmentCard(DevCardType.ROAD_CONSTRUCTION);
            changes.firePropertyChange(RC_PLAYED, "null", ki);
        }
    }

    public void endRCCard(int id) {
        endRCCard(getPlayer(id));
    }

    public void endRCCard(Player player) {
        if (player.isRCActive()) {
            boolean fire = !player.getStatus().equals(FIRST_STREET);
            player.setStatus(TRADE_OR_BUILD);
            if (fire) changes.firePropertyChange(RC_PLAYED, "", player);
        }
    }

    public void applyInventionCard(Player player, RawMaterialOverview overview) throws CatanException {
        if (!player.hasInventionCard())
            throw new CatanException(String.format(PLAYER_HAS_NO_DEV_CARD, player.getId(), DevCardType.INVENTION.toString()), true);

        if (overview.getTotalCount() != 2)
            throw new CatanException(String.format("Es können genau 2 Rohstoffe gezogen werden, nicht %s", overview.getTotalCount()), true);

        this.rawMaterialDeck.decrease(overview);
        player.increaseRawMaterials(overview);
        player.removeDevelopmentCard(DevCardType.INVENTION, 1);

        player.setStatus(TRADE_OR_BUILD);

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
        List<RawMaterialType> types = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            RawMaterialType type = this.rawMaterialDeck.withdrawRandomCard();
            types.add(type);
        }

        RawMaterialOverview overview = new RawMaterialOverview();

        for (RawMaterialType type : types) {
            overview.increase(type, 1);
        }

        applyInventionCard(player, overview);
    }


    public void applyKnightCard(int knightPlayerId, int targetPlayerId, Location newRobberLoc) throws CatanException {
        Player knightPlayer = getPlayer(knightPlayerId);
        if (!knightPlayer.equals(getCurrent()))
            CatanException.throwNotYourTurnException(knightPlayerId);

        targetPlayerId = activateRobber(knightPlayerId, targetPlayerId, newRobberLoc);

        //remove knight card after robber has been activated because if wrong data has been sent (e.g. opponent cannot be robbed)
        //the card should not be removed from player
        knightPlayer.applyKnightCard();

        Object[] data = new Object[2];
        data[0] = targetPlayerId;
        data[1] = newRobberLoc;

        if (isGreatestArmyPlayer(knightPlayer))
            assignGreatestArmy(knightPlayer);

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

        if (!player.canAfford(offer))
            throw new CatanException("Du hast nicht genug Rohstoffe für diesen Handel.", true);

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

    public List<Player> getKIs() {
        List<Player> kiList = new ArrayList<>();

        for (Player player : this.players) {
            if (player.isKI()) kiList.add(player);
        }

        return kiList;
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
    public int activateRobber(int playerId, int opponentId, Location newRobberLoc) throws CatanException {
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
                } else {
                    opponent = null;
                }
            }
        }

        changes.firePropertyChange(ROBBER_AT, false, CatanMessage.robberMoved(getCurrent(), board.getRobber(), opponent));

        currentPlayer.setStatus(TRADE_OR_BUILD);
        return opponent == null ? -1 : opponent.getId();
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
        this.gameStarted = false;
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

    public boolean isGreatestArmyPlayer(Player player) throws CatanException {

        if (player.getArmyCount() < 3) return false;

        return this.greatestArmyPlayer == null || this.getPlayerWithHighestArmyCount().equals(player);
    }

    public void assignGreatestArmy(Player player) throws CatanException {

        if (this.greatestArmyPlayer == null) {
            player.assignGreatestArmy();
            this.greatestArmyPlayer = player;
        } else if (!this.greatestArmyPlayer.equals(player)) {
            this.greatestArmyPlayer.removeGreatestArmy();
            player.assignGreatestArmy();
            this.greatestArmyPlayer = player;
        }
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

    public DevelopmentCardOverview getDevelopmentCardDeck() {
        return this.developmentCardDeck;
    }

    public void setDevelopmentCardDeck(DevelopmentCardOverview devCardDeck) {
        this.developmentCardDeck = devCardDeck;
    }
    //endregion
}

