package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class Player extends JSONStringBuilder implements Comparable, Cloneable {

    //region property change listener
    protected PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
    //endregion

    // region Members
    @Expose
    protected int id;

    @Expose
    @SerializedName(Constants.PLAYER_NAME)
    protected String name;

    @Expose
    @SerializedName(Constants.PLAYER_STATE)
    protected String status;

    @Expose
    @SerializedName(Constants.PLAYER_COLOR)
    protected Color color;

    @Expose
    @SerializedName(Constants.VICTORY_PTS)
    protected int victoryPoints;

    protected int victoryPointsDevCards;

    @Expose
    @SerializedName(Constants.ARMY)
    protected int armyCount;

    @Expose
    @SerializedName(Constants.LARGEST_ARMY)
    protected boolean greatestArmy;

    @Expose
    @SerializedName(Constants.LONGEST_RD)
    protected boolean longestRoad;

    @Expose
    @SerializedName(Constants.RAW_MATERIALS)
    protected RawMaterialOverview rawMaterialDeck;

    @Expose
    @SerializedName(Constants.DEV_CARDS)
    protected DevelopmentCardOverview developmentDeck;

    protected Building[] buildingStock;
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
        this.victoryPoints = 0;
        this.victoryPointsDevCards = 0;
        this.armyCount = 0;
        this.greatestArmy = false;
        this.longestRoad = false;
        this.rawMaterialDeck = new RawMaterialOverview(0);
        this.developmentDeck = new DevelopmentCardOverview(0);
        this.buildingStock = null;
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

        changes.firePropertyChange(Constants.DICE, result, this);

        return result;
    }

    public void endMove() {
        changes.firePropertyChange(Constants.END_TURN, "", this);
    }

    /**
     * <method name: buyDevelopmentCard>
     * <description: this method performs the actions required to buy development card>
     * <preconditions: player has the required cards to buy and his turn is up>
     * <postconditions: player gets a development card in exchange for his material cards>
     */
    public void buyDevelopmentCard(DevCardType card) throws Exception {
        try {
            this.rawMaterialDeck.decrease(RawMaterialType.ORE, 1);
            this.rawMaterialDeck.decrease(RawMaterialType.WOOL, 1);
            this.rawMaterialDeck.decrease(RawMaterialType.WHEAT, 1);
            this.developmentDeck.increase(card, 1);
        } catch (Exception e) {
            throw e;
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
        changes.firePropertyChange("Trade Decrease",
                offer, this);

        this.rawMaterialDeck.increase(request);
        changes.firePropertyChange("Trade Increase",
                request, this);
    }
    //endregion

    //region hasXTo1Haven

    /**
     * returns if the player can trade 3:1 or 2:1
     *
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

    /**
     * <method name: has10VictoryPoints>
     * <description: this method checks if the player has at least 10 points>
     * <preconditions: none>
     * <postconditions: none>
     */
    public boolean has10VictoryPoints() {
        return (this.victoryPoints + this.victoryPointsDevCards) >= 10;
    }

    public void addVictoryPoints(int vicPtsGain) {
        this.victoryPoints += vicPtsGain;
    }

    public void decreaseRawMaterials(RawMaterialOverview overview) throws IllegalArgumentException {
        changes.firePropertyChange("RawMaterialDecrease", overview, this);
        this.rawMaterialDeck.decrease(overview);
    }

    public void increaseRawMaterials(RawMaterialOverview overview) throws IllegalArgumentException {
        changes.firePropertyChange("RawMaterialIncrease", overview, this);
        this.rawMaterialDeck.increase(overview);
    }

    /**
     * Player has to extract half of their cards if
     * he has at least 7 raw materials
     *
     * @return
     */
    public boolean hasToExtractCards() {
        return rawMaterialDeck.getTotalCount() >= 7 ? true : false;
    }

    //endregion


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

    public void setStatus(String status) {
        String oldStatus = this.status;
        boolean fire = status.equals(oldStatus) ? false : true;
        this.status = status;
        if (fire) changes.firePropertyChange("status", oldStatus, this);
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
        boolean fire = color.equals(this.color) ? false : true;
        this.color = color;
        if (fire)
            changes.firePropertyChange("color", "null", this);
    }

    public boolean isKI() {
        return isKI;
    }

    public void setKI(boolean isKI) { this.isKI = isKI; }

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

    @Override
    public String toJSONString_Unknown() {
        JSONObject hiddenJSON = this.toJSON();

        hiddenJSON.remove(Constants.DEV_CARDS);
        hiddenJSON.put(Constants.DEV_CARDS, this.developmentDeck.toJSON_Unknown());

        hiddenJSON.remove(Constants.RAW_MATERIALS);
        hiddenJSON.put(Constants.RAW_MATERIALS, this.rawMaterialDeck.toJSON_Unknown());

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
        return "ID: "+ id + "_Name:" + this.getName() + "_Farbe:" + this.getColor() + "_Status:" +this.getStatus();
    }

    //____________FOR TESTING___________________________
    public void setRawMaterialDeck(RawMaterialOverview rmo) {
        this.rawMaterialDeck = rmo;
    }
}
