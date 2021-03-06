package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.lmu.settleBattle.catanServer.Constants.*;

public class Player extends JSONStringBuilder implements Comparable, Cloneable {

    //region property change listener
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }
    //endregion

    // region Members
    @Expose
    private int id;

    @Expose
    @SerializedName(Constants.PLAYER_NAME)
    private String name;

    @Expose
    @SerializedName(Constants.PLAYER_STATE)
    private String status;

    @Expose
    @SerializedName(Constants.PLAYER_COLOR)
    private Color color;

    @Expose
    @SerializedName(Constants.VICTORY_PTS)
    private int victoryPtsTotal;
    private int victoryPtsHidden;

    @Expose
    @SerializedName(Constants.ARMY)
    private int armyCount;

    @Expose
    @SerializedName(Constants.LARGEST_ARMY)
    private boolean greatestArmy;

    @Expose
    @SerializedName(Constants.LONGEST_RD)
    private boolean hasLongestRoad;

    private int longestRoadLength;

    @Expose
    @SerializedName(Constants.RAW_MATERIALS)
    protected RawMaterialOverview rawMaterialDeck;

    @Expose
    @SerializedName(Constants.DEV_CARDS)
    private DevelopmentCardOverview developmentDeck;

    protected List<Building> settlements;
    protected List<Building> roads;
    protected List<Building> cities;
    protected BuildingStock stock;

    protected ArrayList<Haven> havens;

    private boolean isKI;
    private String nextStatus;
    //endregion

    //region Constructors

    /**
     * <method name: Player>
     * <description: deffault constructor without parameters>
     * <preconditions: none>
     * <postconditions: new player object is created>
     */
    public Player() {
        this.id = -1;
        this.color = null;
        this.name = null;
        this.status = "";
        this.victoryPtsTotal = 0;
        this.victoryPtsHidden = 0;
        this.armyCount = 0;
        this.greatestArmy = false;
        this.hasLongestRoad = false;
        this.rawMaterialDeck = new RawMaterialOverview(0);
        this.developmentDeck = new DevelopmentCardOverview(0);

        //initialize building stock
        this.settlements = new ArrayList<>();
        this.roads = new ArrayList<>();
        this.cities = new ArrayList<>();

        stock = new BuildingStock();

        this.havens = new ArrayList<>();
        this.isKI = false;
        this.nextStatus = WAIT;
    }

    /**
     * <method name: Player>
     * <description: constructor>
     * <preconditions: none>
     * <postconditions: new Player object is created with the given id>
     *
     * @param id this is id given from the server
     */
    public Player(int id) {
        this();
        this.id = id;
    }

    public Player(int id, boolean isKI) {
        this(id);
        this.isKI = isKI;
    }

    public Player(int id, Color color) {
        this(id);
        this.color = color;
    }
    //endregion

    //region Actions

    /**
     * <method name: throwDice>
     * <description: this method rolls two dice>
     * <preconditions: none>
     * <postconditions: dice are rolled>
     *
     * @return gives back a random int
     */
    public int[] throwDice() {
        Dice dice = new Dice();

        int[] result = dice.roll();

        System.out.printf("%s diced %s. fire property change", this.id, result[0] + result[1]);
        changes.firePropertyChange(ROLL_DICE, result, this);

        return result;
    }

    public void endMove() {
        changes.firePropertyChange(END_TURN, "", this);
    }

    /**
     * <method name: addDevelopmentCard>
     * <description: this method performs the actions required to buy development card>
     * <preconditions: player has the required cards to buy and his turn is up>
     * <postconditions: player gets a development card in exchange for his material cards>
     */
    public void addDevelopmentCard(DevCardType card, int amount) throws CatanException {
        this.developmentDeck.increase(card, amount);
        if (card == DevCardType.VICTORY_POINT) {
            this.victoryPtsHidden += amount;
            increaseVictoryPoints(amount, false);
        }

        changes.firePropertyChange(DEV_CARD_BUY, card, this);
    }

    //region trade

    /**
     * <method name: trade>
     * <description: none>
     * <preconditions: none>
     * <postconditions: none>
     */
    public void trade(RawMaterialOverview offer, RawMaterialOverview request)
            throws CatanException {

        this.rawMaterialDeck.decrease(offer);
        changes.firePropertyChange(TRD_DECR_NO_STAT_UPD, offer, this);

        this.rawMaterialDeck.increase(request);
        changes.firePropertyChange(TRD_INCR, request, this);
    }
    //endregion

    //region hasXTo1Haven

    /**
     * returns if the player can trade 3:1 or 2:1
     *
     * @param count
     * @return
     */
    public boolean hasXTo1Haven(int count) throws CatanException {
        if (count != 3 && count != 2)
            throw new CatanException("Es gibt keinen " + count + ":1 Hafen", false);

        if (count == 3) {
            for (Haven haven : havens) {
                if (haven.getHarvest().equals(RawMaterialType.WATER))
                    return true;
            }
        } else {
            for (Haven haven : havens)
                //if he has any haven which has not the type WATER he can trade 2:1
                if (!haven.getHarvest().equals(RawMaterialType.WATER))
                    return true;
        }

        return false;
    }
    //endregion

    public Haven get2To1Haven(RawMaterialType type) {

        if (type.equals(RawMaterialType.WATER))
            return null;

        for (Haven haven : havens) {
            if (haven.getHarvest().equals(type))
                return haven;
        }

        return null;
    }

    public void addHaven(Haven haven) {
        haven.setOccupied(true);
        this.havens.add(haven);
    }

    public List<RawMaterialType> getRawMaterialTypes() {
        return this.rawMaterialDeck.getTypes();
    }

    public int getRawMaterialCount() {
        return this.rawMaterialDeck.getTotalCount();
    }

    public boolean hasInventionCard() {
        return this.developmentDeck.hasInventionCard();
    }

    /**
     * <method name: hasWon>
     * <description: this method checks if the player has at least 10 points>
     * <preconditions: none>
     * <postconditions: none>
     */
    public boolean hasWon() {
        return this.victoryPtsTotal >= 10;
    }

    public void decreaseRawMaterials(RawMaterialOverview overview, boolean sendStatusUpdate)
            throws CatanException {
        if (overview.getTotalCount() == 0) return;

        this.rawMaterialDeck.decrease(overview);

        String property = sendStatusUpdate ? RMO_DECR : RMO_DECR_NO_STAT_UPD;
        changes.firePropertyChange(property, overview, this);
    }

    public void decreaseRawMaterials(RawMaterialOverview overview) throws CatanException {
        decreaseRawMaterials(overview, true);
    }

    public void increaseRawMaterials(RawMaterialOverview overview, boolean sendStatusUpdate) throws CatanException {
        if (overview.getTotalCount() == 0) return;

        this.rawMaterialDeck.increase(overview);
        String property = sendStatusUpdate ? RMO_INCR : RMO_INCR_NO_STAT_UPD;
        changes.firePropertyChange(property, overview, this);
    }

    public void increaseRawMaterials(RawMaterialOverview overview) throws CatanException {
        increaseRawMaterials(overview, true);
    }

    public void removeDevelopmentCard(DevCardType type) throws CatanException {
        this.developmentDeck.decrease(type, 1);
    }

    public boolean hasRoadConstructionCard() {
        return this.developmentDeck.hasRoadConstructionCard();
    }

    public boolean isActive() {
        return this.status.equals(EXTRACT_CARDS_DUE_TO_ROBBER) || this.status.equals(ROBBER_TO) ||
                this.status.equals(TRADE_OR_BUILD) || this.status.equals(BUILD_SETTLEMENT) ||
                this.status.equals(BUILD_STREET) || this.status.equals(DICE) || this.status.equals(START_GAME);
    }

    private int increaseArmyCount() {
        return armyCount++;
    }

    /**
     * applys knight card
     */
    public void applyKnightCard() throws CatanException {
        removeKnightCard();
        increaseArmyCount();
    }

    public void decreaseVictoryPoints(int amount, boolean sendStatusUpdate) throws CatanException {
        if (victoryPtsTotal <= 0)
            throw new CatanException(String.format("Siegpunkte können nicht um %s verringert werden. (Haben %s)",
                    amount, victoryPtsTotal), true);
        if (amount < 0)
            throw new CatanException(String.format("amount muss positiv sein (Wert: %s)", amount), false);

        int oldVpAmount = victoryPtsTotal;
        this.victoryPtsTotal -= amount;

        if (sendStatusUpdate) changes.firePropertyChange(VICTORY_PTS, oldVpAmount, this);
    }

    public void decreaseVictoryPoints(int amount) throws CatanException {
        decreaseVictoryPoints(amount, false);
    }

    public void increaseVictoryPoints(int amount, boolean sendStatusUpdate)
            throws CatanException {
        if (amount < 0)
            throw new CatanException(String.format("amount ist negativ und somit ungültig (Wert: %s)", amount), false);

        int oldVpAmount = this.victoryPtsTotal;
        this.victoryPtsTotal += amount;

        if (sendStatusUpdate) changes.firePropertyChange(VICTORY_PTS, oldVpAmount, this);
    }

    /**
     * Player has to extract half of their cards if
     * he has at least 7 raw materials
     *
     * @return
     */
    public boolean hasToExtractCards() {
        return rawMaterialDeck.getTotalCount() > 7;
    }

    public boolean hasRawMaterial(RawMaterialType type) {
        return this.rawMaterialDeck.getTypeCount(type) >= 1;
    }

    public int getRawMaterialCount(RawMaterialType type) {
        return this.rawMaterialDeck.getTypeCount(type);
    }

    public boolean hasMonopoleCard() {
        return this.developmentDeck.hasMonopoleCard();
    }

    public boolean hasKnightCard() {
        return this.developmentDeck.hasKnightCard();
    }

    public void removeDevelopmentCard(DevCardType type, int amount) throws CatanException {
        this.developmentDeck.decrease(type, amount);
    }

    public boolean canAffordDevCard() {
        return this.rawMaterialDeck.canAffordDevelopmentCard();
    }

    public boolean canAfford(Building building) {
        return this.rawMaterialDeck.canAfford(building);
    }

    public boolean canAfford(BuildingType type) {
        return this.rawMaterialDeck.canAfford().contains(type);
    }

    public boolean canAfford(RawMaterialOverview overview) {
        return this.rawMaterialDeck.canAfford(overview);
    }

    public boolean shouldAcceptTradeRequest(TradeRequest tr) {
        boolean accept = false;
        RawMaterialOverview request = tr.getRequest();
        RawMaterialOverview offer = tr.getOffer();

        for (BuildingType type : BuildingType.values()) {
            accept = couldAffordBuilding(type, request, offer) && !canAfford(type) && stock.getCount(type) > 0;
            if (accept) break;
        }

        if (!accept) {
            accept = canAfford(BuildingType.SETTLEMENT) && request.hasOnly(RawMaterialType.ORE) && stock.getCount(BuildingType.SETTLEMENT) > 0 ||
                    canAfford(BuildingType.CITY) && request.getOreCount() == 0 && request.getWheatCount() == 0 && stock.getCount(BuildingType.CITY) > 0||
                    canAfford(BuildingType.ROAD) && !canAfford(BuildingType.SETTLEMENT) && request.getClayCount() == 0 && request.getWoodCount() == 0 && stock.getCount(BuildingType.ROAD) > 0;
        }

        if (!accept) {
            accept = couldAffordDevCard(request, offer) && !canAffordDevCard();
        }

        return accept;
    }

    private boolean couldAffordDevCard(RawMaterialOverview request, RawMaterialOverview offer) {
        boolean ret;

        try {
            RawMaterialOverview newOverview = (RawMaterialOverview) this.rawMaterialDeck.clone();
            newOverview.decrease(request);
            newOverview.increase(offer);

            ret = newOverview.canAffordDevelopmentCard();
        } catch (Exception ex) {
            return false;
        }

        return ret;
    }

    private boolean couldAffordBuilding(BuildingType type, RawMaterialOverview request, RawMaterialOverview offer) {
        boolean ret;

        try {
            RawMaterialOverview newOverview = (RawMaterialOverview) this.rawMaterialDeck.clone();
            newOverview.decrease(request);
            newOverview.increase(offer);

            ret = newOverview.canAfford().contains(type);
        } catch (Exception ex) {
            return false;
        }

        return ret;
    }

    public boolean isRCActive() {
        return this.status.equals(FIRST_STREET) || this.status.equals(SECOND_STREET);
    }

    public void sendBestTradeRequest() throws CatanException {
        TradeRequest tr = this.rawMaterialDeck.getBestTradeRequest();

        if (tr != null) {
            tr.setPlayerId(this.getId());
            changes.firePropertyChange(SEND_TRD, tr, this);
        }
    }

    /**
     * random decision if a trade request should be sent to all
     *
     * @return true if KI wants to send trade request
     */
    public boolean decidesToSendTr() {
        Random random = new Random();

        int randomInt = random.nextInt(10);
        return randomInt % 3 == 0;
    }
    //region Properties


    public int getId() {
        return id;
    }

    public int getArmyCount() {
        return armyCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String newStatus, boolean sendStatusUpdate) {
        if (newStatus.equals("")) return;

        boolean fire = !newStatus.equals(this.status);
        String oldStatus = this.status;
        this.status = newStatus;

        if (fire && sendStatusUpdate) {
            changes.firePropertyChange(STATUS_UPD, oldStatus, this);
        }
    }

    public void setStatus(String newStatus) {
        setStatus(newStatus, true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color newColor) {
        boolean fire = !newColor.equals(this.color);
        this.color = newColor;
        if (fire)
            changes.firePropertyChange(STATUS_UPD, "null", this);
    }

    public boolean isKI() {
        return isKI;
    }

    public void setKI(boolean isKI) {
        this.isKI = isKI;
    }

    public boolean hasLongestRoad() {
        return hasLongestRoad;
    }

    public void setHasLongestRoad(boolean boo) {
        this.hasLongestRoad = boo;
    }

    public boolean isGreatestArmy() {
        return greatestArmy;
    }

    public void setGreatestArmy(boolean boo) {
        this.greatestArmy = boo;
    }

    public void assignGreatestArmy() throws CatanException {
        this.greatestArmy = true;
        this.increaseVictoryPoints(2, true);
    }

    public void removeGreatestArmy() throws CatanException {
        this.greatestArmy = false;
        this.decreaseVictoryPoints(2, true);
    }

    public void setLongestRoadLength(int longestRoadLength) {
        this.longestRoadLength = longestRoadLength;
    }

    public int getLongestRoadLength() {
        return longestRoadLength;
    }

    public int getVictoryPoints() {
        return this.victoryPtsTotal;
    }
    //endregion


    public String getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(String nextStatus) {
        this.nextStatus = nextStatus;
    }

    public void activateNextStatus() {
        String nextStat = this.nextStatus;
        this.nextStatus = WAIT;
        this.setStatus(nextStat);
    }

    @Override
    public String toJSONString_Unknown() {
        JSONObject hiddenJSON = this.toJSON();

        hiddenJSON.remove(DEV_CARDS);
        hiddenJSON.put(DEV_CARDS, this.developmentDeck.toJSON_Unknown());

        hiddenJSON.remove(RAW_MATERIALS);
        hiddenJSON.put(RAW_MATERIALS, this.rawMaterialDeck.toJSON_Unknown());

        hiddenJSON.remove(VICTORY_PTS);
        hiddenJSON.put(VICTORY_PTS, this.victoryPtsTotal - this.victoryPtsHidden);

        return hiddenJSON.toString();
    }

    @Override
    public int compareTo(Object o) {
        Player player = (Player) o;

        if (player.getId() < this.getId())
            return 1;
        else if (player.getId() == this.getId())
            return 0;
        return -1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "ID: " + id + "_Name:" + this.getName() + "_Farbe:" + this.getColor() + "_Status:" + this.getStatus();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Player) {
            Player player = (Player) object;
            return this.getId() == player.getId();
        }
        return false;
    }

    public List<Building> getBuildings(BuildingType type) {
        List<Building> buildings = null;
        switch (type) {
            case ROAD:
                buildings = this.roads;
                break;
            case SETTLEMENT:
                buildings = this.settlements;
                break;
            case CITY:
                buildings = this.cities;
                break;
        }
        return buildings;
    }

    public BuildingStock getStock() {
        return stock;
    }

    public void addBuilding(Building bld) throws CatanException {
        switch (bld.getType()) {
            case ROAD:
                roads.add(bld);
                stock.decrease(BuildingType.ROAD);
                break;

            case CITY:
                Building foundS = null;
                for (Building s : settlements) {
                    if (s.isBuiltAroundHere(bld.getLocations(), true)) {
                        foundS = s;
                        break;
                    }
                }

                if (foundS != null) {
                    settlements.remove(foundS);
                    cities.add(bld);
                    stock.decrease(BuildingType.CITY);
                    stock.increaseSettlement();
                }
                break;

            case SETTLEMENT:
                settlements.add(bld);
                stock.decrease(BuildingType.SETTLEMENT);
                break;
        }
    }

    public Building getLastSettlement() {
        if (this.settlements.size() == 0) return null;
        return this.settlements.get(settlements.size() - 1);
    }

    public List<Building> getSettlements() {
        return settlements;
    }

    public Building getRandomSettlement() {
        Random random = new Random();
        int randomIndex = random.nextInt(this.settlements.size());

        return this.settlements.get(randomIndex);
    }

    public List<Building> getSettlementsAndCities() {
        List<Building> buildings = new ArrayList<>();
        buildings.addAll(settlements);
        buildings.addAll(cities);
        return buildings;
    }

    /**
     *
     * @param allAttachable specifies if all settlements and citites which are returned are attachable,
     *                      which means if a road can be placed next to them
     * @return list of attachable settlements and cities
     */
    public List<Building> getSettlementsAndCities(boolean allAttachable) {
        List<Building> attachableList = new ArrayList<>();
        List<Building> buildings = getSettlementsAndCities();

        if (!allAttachable) return buildings;

        for (Building bld : buildings) {
            if (bld.isAttachable()) attachableList.add(bld);
        }

        return attachableList;
    }

    public RawMaterialType removeRandomResource() throws Exception {
        Random r = new Random();
        int random = r.nextInt(6);
        RawMaterialType type = RawMaterialType.values()[random];
        this.rawMaterialDeck.decrease(type, 1);
        return type;
    }

    public DevelopmentCardOverview getDevelopmentDeck() {
        return developmentDeck;
    }

    public void removeKnightCard() throws CatanException {
        this.developmentDeck.decrease(DevCardType.KNIGHT, 1);
    }

    public List<Building> getRoads() {
        return roads;
    }

    public List<Building> getAttachableRoads() {
        List<Building> attachableRoads = new ArrayList<>();

        for (Building r : roads) {
            if (r.isAttachable()) attachableRoads.add(r);
        }

        return attachableRoads;
    }

    //____________FOR TESTING___________________________
    public void setRawMaterialDeck(RawMaterialOverview rmo) {
        this.rawMaterialDeck = rmo;
    }
}
