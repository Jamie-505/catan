package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Player extends JSONStringBuilder implements Comparable, Cloneable {

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

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
    private int victoryPoints;

    private int victoryPointsTotal;

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
    private RawMaterialOverview rawMaterialDeck;

    @Expose
    @SerializedName(Constants.DEV_CARDS)
    private DevelopmentCardOverview developmentDeck;

    private Building[] buildingStock;
    private Haven haven;
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
        this.victoryPointsTotal = 0;
        this.armyCount = 0;
        this.greatestArmy = false;
        this.longestRoad = false;
        this.rawMaterialDeck = new RawMaterialOverview(0);
        this.developmentDeck = new DevelopmentCardOverview(0);
        this.buildingStock = null;
        this.haven = null;
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
    public static int[] throwDice() {
        Dice dice = new Dice();
        return dice.roll();
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
            this.rawMaterialDeck.decrease(RawMaterialType.WEAT, 1);
            this.developmentDeck.increase(card, 1);
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * <method name: sendTradeRequest>
     * <description: player places an offer for other players>
     * <preconditions: player turn is up and>
     * <postconditions: trade request is sent>
     */
    public TradeRequest sendTradeRequest(TradeRequest tr) { //@return Trade @param TradeRequest
        //TODO
        return null;
    }

    /**
     * <method name: acceptTradeRequest>
     * <description: this method is called to accept a trade request>
     * <preconditions: none>
     * <postconditions: trade request is accepted>
     */
    public void acceptTradeRequest(Object tr) { //@param TradeRequest
        //TODO
    }

    /**
     * <method name: declineTradeRequest>
     * <description: this method is called to decline a trade request>
     * <preconditions: none>
     * <postconditions: none>
     */
    public void declineTradeRequest(Object tr) { //@param TradeRequest
        //TODO
    }

    /**
     * <method name: trade>
     * <description: none>
     * <preconditions: none>
     * <postconditions: none>
     */
    public void trade(Object tr) { //@param TradeRequest
        //TODO
    }


    /**
     * <method name: moveRobber>
     * <description: player chooses where to move the robber and move it>
     * <preconditions: number 7 is rolled>
     * <postconditions: the robber is moved>
     */
    public void moveRobber(Location[] lod) {
        //TODO
    }


    /**
     * <method name: robPlayer>
     * <description: the player choose a card from one of the other players who have settlement where the robber moved >
     * <preconditions: number 7 is rolled>
     * <postconditions: player gets a card from another players>
     */
    public void robPlayer(Player plr) {
        //TODO
    }

    /**
     * <method name: has10VectoryPoints>
     * <description: this method checks if the player has at least 10 points>
     * <preconditions: none>
     * <postconditions: none>
     */
    public boolean has10VectoryPoints() {
        //TODO
        return false;
    }

    public void decreaseRawMaterials(RawMaterialOverview overview) throws IllegalArgumentException {
        this.rawMaterialDeck.decrease(overview);
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

    /**
     * <method name: sendChatMessage>
     * <description: this method sends a chat message from the [???] to the [???]>
     * <preconditions: none>
     * <postconditions: message is sent to the [???]>
     */
    public void sendChatMessage(String msg) {
        //TODO
    }

    //endregion

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
        boolean fire = status.equals(oldStatus) ? false:true;
        this.status = status;
        if (fire) changes.firePropertyChange( "status", oldStatus, this );
    }

    public void addPropertyChangeListener( PropertyChangeListener l )
    {
        changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l )
    {
        changes.removePropertyChangeListener( l );
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
        boolean fire = color.equals(this.color) ? false:true;
        this.color = color;
        if (fire)
            changes.firePropertyChange( "color", "null", this );
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

    public boolean canAffordDevCard() {
        return this.rawMaterialDeck.canAffordDevelopmentCard();
    }

    public boolean canAfford(Building building) {
        return this.rawMaterialDeck.canAfford(building);
    }

    public boolean canAfford(BuildingType type) {
        return this.rawMaterialDeck.canAfford().contains(type);
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
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
