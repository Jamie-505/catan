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

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
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
    private boolean longestRoad;

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
        this.longestRoad = false;
        this.rawMaterialDeck = new RawMaterialOverview(0);
        this.developmentDeck = new DevelopmentCardOverview(0);

        //initialize building stock
        this.settlements = new ArrayList<>();
        this.roads = new ArrayList<>();
        this.cities = new ArrayList<>();

        stock = new BuildingStock();

        this.havens = new ArrayList<>();
        this.isKI = false;
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

        changes.firePropertyChange(DICE, result, this);

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
    public void addDevelopmentCard(DevCardType card, int amount) throws Exception {
        this.developmentDeck.increase(card, amount);
        if(card == DevCardType.VICTORY_POINT){
            this.victoryPtsHidden += amount;
            increaseVictoryPoints(amount, false);
        }
    }

    //region trade
    /**
     * <method name: trade>
     * <description: none>
     * <preconditions: none>
     * <postconditions: none>
     */
    public void trade(RawMaterialOverview offer, RawMaterialOverview request)
            throws IllegalArgumentException {

        this.rawMaterialDeck.decrease(offer);
        changes.firePropertyChange(TRD_DECR, offer, this);

        this.rawMaterialDeck.increase(request);
        changes.firePropertyChange(TRD_INCR, request, this);
    }
    //endregion

    //region hasXTo1Haven
    /**
     * returns if the player can trade 3:1 or 2:1
     * @param count
     * @return
     */
    public boolean hasXTo1Haven(int count) {
        if (count != 3 && count != 2)
            throw new IllegalArgumentException("There is no " + count + " to 1 haven");

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

    public void decreaseRawMaterials(RawMaterialOverview overview) throws IllegalArgumentException {
        if (overview.getTotalCount() == 0) return;

        this.rawMaterialDeck.decrease(overview);
        changes.firePropertyChange(RMO_DECR, overview, this);
    }

    public void increaseRawMaterials(RawMaterialOverview overview) throws IllegalArgumentException {
        if (overview.getTotalCount() == 0) return;

        this.rawMaterialDeck.increase(overview);
        changes.firePropertyChange(RMO_INCR, overview, this);
    }

    public void removeDevelopmentCard(DevCardType type) throws Exception {
        this.developmentDeck.decrease(type, 1);
    }

    public boolean hasRoadConstructionCard() {
        return this.developmentDeck.hasRoadConstructionCard();
    }

    private int increaseArmyCount() {
        return armyCount++;
    }

    /**
     * plays knight card if player possesses one
     * @return
     */
    public boolean playKnight() {
        boolean done = false;
        try {
            if (developmentDeck.hasKnightCard()) {
                developmentDeck.decrease(DevCardType.KNIGHT, 1);
                increaseArmyCount();
                done = true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            done = false;
        }
        return done;
    }

    public void decreaseVictoryPoints(int i) throws Exception {
        if(this.victoryPtsTotal <= 0) throw new Exception();
        this.victoryPtsTotal -= - i;
    }

    public void increaseVictoryPoints(int amount, boolean showAll){
        int oldVpAmount = this.victoryPtsTotal;
        this.victoryPtsTotal = victoryPtsTotal + amount;

        if (showAll) changes.firePropertyChange(VP_INCR, oldVpAmount, this);
        else changes.firePropertyChange(SU_ONLY_TO_ME, oldVpAmount, this);
    }

    /**
     * Player has to extract half of their cards if
     * he has at least 7 raw materials
     *
     * @return
     */
    public boolean hasToExtractCards() {
        return rawMaterialDeck.getTotalCount() >= 7;
    }

    public boolean hasRawMaterial(RawMaterialType type){
        return this.rawMaterialDeck.getTypeCount(type) >= 1;
    }

    public int getRawMaterialCount(RawMaterialType type){
        return this.rawMaterialDeck.getTypeCount(type);
    }

    public boolean hasMonopoleCard() {
        return this.developmentDeck.hasMonopoleCard();
    }

    public void removeDevelopmentCard(DevCardType type , int amount) throws Exception {
        this.developmentDeck.decrease(type,amount);
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

    //region Properties

    /**
     * <method name: getId>
     * <description: this is a getter method for the id>
     * <preconditions: none>
     * <postconditions: none>
     */
    public int getId() {
        return id;
    }

    /**
     * <method name: getArmyCount>
     * <description: this is a getter method for the army count>
     * <preconditions: none>
     * <postconditions: none>
     */
    public int getArmyCount() {
        return armyCount;
    }


    /**
     * <method name: getStatus>
     * <description: this is a getter method for the status>
     * <preconditions: none>
     * <postconditions: none>
     */
    public String getStatus() {
        return status;
    }

    /**
     * <method name: setStatus>
     * <description: this method is a setter method for the status>
     * <preconditions: none>
     * <postconditions: none>
     */

    public void setStatus(String newStatus) {
        boolean fire = !newStatus.equals(this.status);
        if (fire) {
            String oldStatus = this.status;
            this.status = newStatus;
            changes.firePropertyChange(SU_TO_ALL, oldStatus, this);
        }
    }

    /**
     * <method name: getName>
     * <description: this is a getter method for the name>
     * <preconditions: none>
     * <postconditions: none>
     */

    public String getName() {
        return name;
    }

    /**
     * <method name: setName>
     * <description: this is a setter method for the name>
     * <preconditions: none>
     * <postconditions: none>
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * <method name: getColor>
     * <description: this is a getter method for the color>
     * <preconditions: none>
     * <postconditions: none>
     */

    public Color getColor() {
        return color;
    }

    /**
     * <method name: setColor>
     * <description: this is a setter method for the color>
     * <preconditions: none>
     * <postconditions: none>
     */

    public void setColor(Color color) {
        boolean fire = !color.equals(this.color);
        this.color = color;
        if (fire)
            changes.firePropertyChange(SU_TO_ALL, "null", this);
    }

    public boolean isKI() {
        return isKI;
    }

    public void setKI(boolean isKI) {
        this.isKI = isKI;
    }

    /**
     * <method name: isLongestRoad>
     * <description: this method checks if a road is the greatest>
     * <preconditions: none>
     * <postconditions: none>
     */
    public boolean isLongestRoad() {
        return longestRoad;
    }

    /**
     * <method name: setLongestRoad>
     * <description: this a setter method for the longest road >
     * <preconditions: none>
     * <postconditions: none>
     */
    public void setLongestRoad(boolean boo) {
        this.longestRoad = boo;
    }


    /**
     * <method name: isGreatestArmy>
     * <description: this method checks if an army is the greatest>
     * <preconditions: none>
     * <postconditions: none>
     */

    public boolean isGreatestArmy() {
        return greatestArmy;
    }

    /**
     * <method name: setGreatestArmy>
     * <description: this is a setter method for the greatest army>
     * <preconditions: none>
     * <postconditions: none>
     */

    public void setGreatestArmy(boolean boo) {
        this.greatestArmy = boo;
    }
    //endregion

    public int getVictoryPointsCount() {
        return victoryPtsTotal;
    }

    @Override
    public String toJSONString_Unknown() {
        JSONObject hiddenJSON = this.toJSON();

        hiddenJSON.remove(DEV_CARDS);
        hiddenJSON.put(DEV_CARDS, this.developmentDeck.toJSON_Unknown());

        hiddenJSON.remove(RAW_MATERIALS);
        hiddenJSON.put(RAW_MATERIALS, this.rawMaterialDeck.toJSON_Unknown());

        hiddenJSON.remove(VICTORY_PTS);
        hiddenJSON.put(VICTORY_PTS, this.victoryPtsTotal-this.victoryPtsHidden);

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

    public void addBuilding(Building bld) {
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

        return this.settlements.get(settlements.size()-1);
    }

    public List<Building> getSettlements() {
        return settlements;
    }

    public List<Building> getCities() {
        return cities;
    }

    public List<Building> getSettlementsAndCities() {
        List<Building> buildings = new ArrayList<>();
        buildings.addAll(settlements);
        buildings.addAll(cities);
        return buildings;
    }

    public RawMaterialType removeRandomResource() throws Exception {
        Random r = new Random();
        int random = r.nextInt(6);
        RawMaterialType type = RawMaterialType.values()[random];
        this.rawMaterialDeck.decrease(type, 1);
        return type;
    }

    public DevelopmentCardOverview getDevelopmentDeck(){
        return developmentDeck;
    }

    public void removeKnightCard() throws Exception {
        this.developmentDeck.decrease(DevCardType.KNIGHT,1);
    }

    //____________FOR TESTING___________________________
    public void setRawMaterialDeck(RawMaterialOverview rmo) {
        this.rawMaterialDeck = rmo;
    }

    public RawMaterialOverview getResources() {
        return this.rawMaterialDeck;
    }
}
