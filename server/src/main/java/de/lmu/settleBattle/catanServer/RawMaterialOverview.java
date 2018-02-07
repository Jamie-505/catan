package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RawMaterialOverview extends JSONStringBuilder implements Cloneable {

    @Expose
    @SerializedName(Constants.WOOD)
    private int woodCount;

    @Expose
    @SerializedName(Constants.CLAY)
    private int clayCount;

    @Expose
    @SerializedName(Constants.WOOL)
    private int woolCount;

    @Expose
    @SerializedName(Constants.WHEAT)
    private int wheatCount;

    @Expose
    @SerializedName(Constants.ORE)
    private int oreCount;

    //region Constructors
    public RawMaterialOverview() {
        this.clayCount = this.oreCount = this.woodCount =
                this.woolCount = this.wheatCount = 0;
    }

    public RawMaterialOverview(int initAmount) {
        this.clayCount = this.oreCount = this.woodCount =
                this.woolCount = this.wheatCount = initAmount;
    }

    public RawMaterialOverview(int clay, int ore, int wood, int wool, int wheat) {
        this.clayCount = clay;
        this.oreCount = ore;
        this.woodCount = wood;
        this.woolCount = wool;
        this.wheatCount = wheat;
    }

    public RawMaterialOverview(RawMaterialType type, int initAmount) {
        this();
        switch (type) {
            case WHEAT:
                wheatCount = initAmount;
                break;
            case CLAY:
                clayCount = initAmount;
                break;
            case ORE:
                oreCount = initAmount;
                break;
            case WOOL:
                woolCount = initAmount;
                break;
            case WOOD:
                woodCount = initAmount;
                break;
        }
    }
    //endregion

    public int getTotalCount() {
        return clayCount + oreCount + woodCount + wheatCount + woolCount;
    }

    //region increase

    /**
     * increases a raw material amount
     *
     * @param type type of raw material to increase
     * @param i    value to be added
     */
    public void increase(RawMaterialType type, int i) throws CatanException {
        //the method increase must not decrease an amount
        if (i < 0) throw new CatanException("i ist negativ und somit ungültig (Wert: " + i + ")", true);

        switch (type) {
            case ORE:
                oreCount += i;
                break;
            case CLAY:
                clayCount += i;
                break;
            case WHEAT:
                wheatCount += i;
                break;
            case WOOD:
                woodCount += i;
                break;
            case WOOL:
                woolCount += i;
                break;
        }
    }


    public void increase(RawMaterialOverview overview) {
        this.oreCount += overview.oreCount;
        this.clayCount += overview.clayCount;
        this.woolCount += overview.woolCount;
        this.woodCount += overview.woodCount;
        this.wheatCount += overview.wheatCount;
    }

    //endregion

    //region decrease

    /**
     * decreases a raw material amount
     *
     * @param type type of raw material to decrease
     * @param i    value to be subtracted
     */
    public void decrease(RawMaterialType type, int i) throws CatanException {
        if (i < 0) throw new CatanException(String.format("i ist negativ und somit ungültig (Wert: %s)", i), true);

        int typeCount = getTypeCount(type);
        if (i > typeCount)
            throw new CatanException(String.format("%s kann nicht um %s verringert werden. (Haben %s)", type.toString(), i, typeCount), true);

        switch (type) {
            case ORE:
                oreCount -= i;
                break;
            case CLAY:
                clayCount -= i;
                break;
            case WHEAT:
                wheatCount -= i;
                break;
            case WOOD:
                woodCount -= i;
                break;
            case WOOL:
                woolCount -= i;
                break;
        }
    }

    public void decrease(RawMaterialOverview overview)
            throws CatanException {
        if (this.oreCount < overview.oreCount)
            throw new CatanException(String.format("Erz kann nicht um %s verringert werden. (Haben %s)", oreCount, overview.oreCount), true);

        if (this.wheatCount < overview.wheatCount)
            throw new CatanException(String.format("Getreide kann nicht um %s verringert werden. (Haben %s)", wheatCount, overview.wheatCount), true);

        if (this.woodCount < overview.woodCount)
            throw new CatanException(String.format("Holz kann nicht um %s verringert werden. (Haben %s)", woodCount, overview.woodCount), true);

        if (this.woolCount < overview.woolCount)
            throw new CatanException(String.format("Wolle kann nicht um %s verringert werden. (Haben %s)", woolCount, overview.woolCount), true);

        if (this.clayCount < overview.clayCount)
            throw new CatanException(String.format("Lehm kann nicht um %s verringert werden. (Haben %s)", clayCount, overview.clayCount), true);

        this.oreCount -= overview.oreCount;
        this.clayCount -= overview.clayCount;
        this.woolCount -= overview.woolCount;
        this.woodCount -= overview.woodCount;
        this.wheatCount -= overview.wheatCount;
    }
    //endregion

    //region withdraw random card


    /**
     * used in the main deck to withdraw random card
     *
     * @return random card
     * @throws IllegalArgumentException
     */

    public RawMaterialType withdrawRandomCard() throws CatanException {

        List<RawMaterialType> cards = this.getTypes();
        Random random = new Random();
        RawMaterialType card = cards.get(random.nextInt(cards.size()));

        decrease(card, 1);

        return card;
    }

    //endregion
    //region canAfford

    /**
     * decides whether there is enough raw material to afford a building
     *
     * @param building building to afford
     * @return
     */
    public boolean canAfford(Building building) {
        return canAfford(building.getCost());
    }

    public boolean canAfford(RawMaterialOverview rmo) {
        if (this.wheatCount < rmo.wheatCount ||
                this.woolCount < rmo.woolCount ||
                this.woodCount < rmo.woodCount ||
                this.oreCount < rmo.oreCount ||
                this.clayCount < rmo.clayCount)
            return false;

        return true;
    }

    public boolean canAffordDevelopmentCard() {
        return canAfford(DevelopmentCardOverview.getCosts());
    }

    /**
     * creates a list of BuildingTypes that can be afforded with raw material stock
     *
     * @return list
     */
    public List<BuildingType> canAfford() {
        List<BuildingType> buildings = new ArrayList();

        if (canAfford(Building.getCosts(BuildingType.ROAD)))
            buildings.add(BuildingType.ROAD);

        if (canAfford(Building.getCosts(BuildingType.SETTLEMENT)))
            buildings.add(BuildingType.SETTLEMENT);

        if (canAfford(Building.getCosts(BuildingType.CITY)))
            buildings.add(BuildingType.CITY);

        return buildings;
    }
    //endregion

    //region Getters and Setters
    public int getOreCount() {
        return oreCount;
    }

    public int getClayCount() {
        return clayCount;
    }

    public int getWheatCount() {
        return wheatCount;
    }

    public int getWoodCount() {
        return woodCount;
    }

    public int getWoolCount() {
        return woolCount;
    }

    public void setClayCount(int clayCount) {
        this.clayCount = clayCount;
    }

    public void setOreCount(int oreCount) {
        this.oreCount = oreCount;
    }

    public void setWheatCount(int wheatCount) {
        this.wheatCount = wheatCount;
    }

    public void setWoodCount(int woodCount) {
        this.woodCount = woodCount;
    }

    public void setWoolCount(int woolCount) {
        this.woolCount = woolCount;
    }

    //endregion

    @Override
    public String toJSONString_Unknown() {
        JSONObject json = new JSONObject();
        json.put(Constants.UNKNOWN, getTotalCount());
        return json.toString();
    }

    //region hasOnly

    /**
     * returns true if raw material overview has only raw materials
     * with a special type and typeCount == count
     *
     * @param count
     * @param type
     * @return
     */
    public boolean hasOnly(int count, RawMaterialType type) {
        int typeCount = getTypeCount(type);
        return typeCount == count && getTotalCount() == count;
    }

    /**
     * returns true if raw material overview has only raw materials
     * of one raw material type with typeCount == count, otherwise false
     *
     * @param count
     * @return
     */
    public boolean hasOnly(int count) {
        return oreCount == count && getTotalCount() == count ||
                clayCount == count && getTotalCount() == count ||
                woodCount == count && getTotalCount() == count ||
                woolCount == count && getTotalCount() == count ||
                wheatCount == count && getTotalCount() == count;
    }

    /**
     * returns true if any of the offer material matches
     * the request that the method called upon
     * @param offer
     * @return
     */
    public boolean hasSameMaterial(RawMaterialOverview offer){

        if ((this.clayCount > 0 && offer.clayCount > 0) ||
                (this.oreCount > 0 && offer.oreCount > 0) ||
                (this.wheatCount > 0 && offer.wheatCount > 0) ||
                (this.woolCount > 0 && offer.woolCount > 0) ||
                (this.woodCount > 0 && offer.woodCount > 0)) return true;

        return false;
    }

    /**
     * returns true if raw material overview contains only raw
     * materials with a special type
     *
     * @param type
     * @return
     */
    public boolean hasOnly(RawMaterialType type) {
        int typeCount = getTypeCount(type);
        return typeCount == getTotalCount();
    }
    //endregion

    public RawMaterialType getType() {
        RawMaterialType type = RawMaterialType.WATER;

        if (this.hasOnly(RawMaterialType.CLAY))
            type = RawMaterialType.CLAY;

        else if (this.hasOnly(RawMaterialType.WOOD))
            type = RawMaterialType.WOOD;

        else if (this.hasOnly(RawMaterialType.WOOL))
            type = RawMaterialType.WOOL;

        else if (this.hasOnly(RawMaterialType.WHEAT))
            type = RawMaterialType.WHEAT;

        else if (this.hasOnly(RawMaterialType.ORE))
            type = RawMaterialType.ORE;

        return type;
    }

    /**
     * returns raw material amount of a special type
     *
     * @param type
     * @return
     */
    public int getTypeCount(RawMaterialType type) {
        int typeCount = 0;

        switch (type) {
            case ORE:
                typeCount = oreCount;
                break;
            case WHEAT:
                typeCount = wheatCount;
                break;
            case WOOD:
                typeCount = woodCount;
                break;
            case WOOL:
                typeCount = woolCount;
                break;
            case CLAY:
                typeCount = clayCount;
                break;
        }
        return typeCount;
    }

    public List<RawMaterialType> getTypes() {
        List<RawMaterialType> types = new ArrayList<>();

        if (oreCount > 0) types.add(RawMaterialType.ORE);
        if (wheatCount > 0) types.add(RawMaterialType.WHEAT);
        if (woolCount > 0) types.add(RawMaterialType.WOOL);
        if (woodCount > 0) types.add(RawMaterialType.WOOD);
        if (clayCount > 0) types.add(RawMaterialType.CLAY);

        return types;
    }

    public TradeRequest getBestTradeRequest() throws CatanException {
        if (this.getTotalCount() < 1) return null;

        RawMaterialOverview request = new RawMaterialOverview(0);
        RawMaterialOverview offer = new RawMaterialOverview(0);

        List<BuildingType> canAfford = canAfford();
        List<RawMaterialType> types = getTypes();

        if (!canAfford.contains(BuildingType.SETTLEMENT)) {
            request.increase(RawMaterialType.WOOD, woodCount == 0 ? 1 : 0);
            request.increase(RawMaterialType.CLAY, clayCount == 0 ? 1 : 0);

            //if player cannot even afford road, don't request more
            //otherwise ask for wheat and wool to build settlement
            if (canAfford.contains(BuildingType.ROAD)) {
                request.increase(RawMaterialType.WHEAT, wheatCount == 0 ? 1 : 0);
                request.increase(RawMaterialType.WOOL, woolCount == 0 ? 1 : 0);
            }

            if (types.contains(RawMaterialType.ORE))
                offer.increase(RawMaterialType.ORE, 1);
            else offer.increase(getMaxType(), 1);

        } else if (!canAfford.contains(BuildingType.CITY)) {
            request.increase(RawMaterialType.ORE, Math.min(3 - oreCount, 0));
            request.increase(RawMaterialType.WHEAT, Math.min(2 - wheatCount, 0));

            if (types.contains(RawMaterialType.WOOL))
                offer.increase(RawMaterialType.WOOL, 1);
            else if (types.contains(RawMaterialType.WOOD))
                offer.increase(RawMaterialType.WOOD, 1);
            else if (types.contains(RawMaterialType.CLAY))
                offer.increase(RawMaterialType.CLAY, 1);
            else offer.increase(getMaxType(), 1);
        }

        return new TradeRequest(offer, request);
    }

    public RawMaterialType getMaxType() {
        List<RawMaterialType> types = getTypes();

        if (types.size() == 0)
            return null;

        if (types.size() == 1)
            return types.get(0);

        RawMaterialType max = types.get(0);

        for (int i = 1; i < types.size(); i++) {
            if (getTypeCount(types.get(i)) > getTypeCount(max))
                max = types.get(i);
        }

        return max;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        String pattern = "Erz:%s, Lehm:%s, Getreide:%s, Wolle:%s, Holz:%s";
        return String.format(pattern, oreCount, clayCount, wheatCount, woolCount, woodCount);
    }
}

