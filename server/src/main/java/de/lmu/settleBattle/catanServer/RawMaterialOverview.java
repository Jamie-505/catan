package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RawMaterialOverview extends JSONStringBuilder {

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
    @SerializedName(Constants.WEAT)
    private int weatCount;

    @Expose
    @SerializedName(Constants.ORE)
    private int oreCount;

    //region Constructors
    public RawMaterialOverview() {
        this.clayCount = this.oreCount = this.woodCount =
                this.woolCount = this.weatCount = 0;
    }

    public RawMaterialOverview(int initAmount) {
        this.clayCount = this.oreCount = this.woodCount =
                this.woolCount = this.weatCount = initAmount;
    }

    public RawMaterialOverview(int clay, int ore, int wood, int wool, int weat) {
        this.clayCount = clay;
        this.oreCount = ore;
        this.woodCount = wood;
        this.woolCount = wool;
        this.weatCount = weat;
    }

    public RawMaterialOverview(RawMaterialType type, int initAmount) {
        this();
        switch (type) {
            case WEAT:
                weatCount = initAmount;
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
        return clayCount + oreCount + woodCount + weatCount + woolCount;
    }

    //region increase
    /**
     * increases a raw material amount
     * @param type type of raw material to increase
     * @param i    value to be added
     */
    public void increase(RawMaterialType type, int i) {
        //the method increase must not decrease an amount
        if (i < 0) throw new IllegalArgumentException("increase cannot be called with negative value");

        switch (type) {
            case ORE:
                oreCount += i;
                break;
            case CLAY:
                clayCount += i;
                break;
            case WEAT:
                weatCount += i;
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
        this.weatCount += overview.weatCount;
    }

    //endregion

    //region decrease
    /**
     * decreases a raw material amount
     * @param type type of raw material to decrease
     * @param i    value to be subtracted
     */
    public void decrease(RawMaterialType type, int i) throws Exception {
        if (i < 0) throw new IllegalArgumentException("decrease cannot be called with negative value");

        int typeCount = getTypeCount(type);
        if (i > typeCount) throw new IllegalArgumentException("Cannot decrease more ore than existing");

        switch (type) {
            case ORE:
                oreCount -= i;
                break;
            case CLAY:
                clayCount -= i;
                break;
            case WEAT:
                weatCount -= i;
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
            throws IllegalArgumentException {
        if (this.oreCount < overview.oreCount)
            throw new IllegalArgumentException("Cannot decrease more ore than existing");

        if (this.weatCount < overview.weatCount)
            throw new IllegalArgumentException("Cannot decrease more weat than existing");

        if (this.woodCount < overview.woodCount)
            throw new IllegalArgumentException("Cannot decrease more wood than existing");

        if (this.woolCount < overview.woolCount)
            throw new IllegalArgumentException("Cannot decrease more wool than existing");

        if (this.clayCount < overview.clayCount)
            throw new IllegalArgumentException("Cannot decrease more clay than existing");

        this.oreCount -= overview.oreCount;
        this.clayCount -= overview.clayCount;
        this.woolCount -= overview.woolCount;
        this.woodCount -= overview.woodCount;
        this.weatCount -= overview.weatCount;
    }
    //endregion

    //region canAfford
    /**
     * decides whether there is enough raw material to afford a building
     * @param building building to afford
     * @return
     */
    public boolean canAfford(Building building) {
        return canAfford(building.getCost());
    }

    public boolean canAfford(RawMaterialOverview rmo) {
        if (this.weatCount < rmo.weatCount ||
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

    public int getWeatCount() {
        return weatCount;
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

    public void setWeatCount(int weatCount) {
        this.weatCount = weatCount;
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
     * @param count
     * @return
     */
    public boolean hasOnly(int count) {
        return oreCount == count && getTotalCount() == count ||
                clayCount == count && getTotalCount() == count ||
                woodCount == count && getTotalCount() == count ||
                woolCount == count && getTotalCount() == count ||
                weatCount == count && getTotalCount() == count;
    }

    /**
     * returns true if raw material overview contains only raw
     * materials with a special type
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

        else if (this.hasOnly(RawMaterialType.WEAT))
            type = RawMaterialType.WEAT;

        else if (this.hasOnly(RawMaterialType.ORE))
            type = RawMaterialType.ORE;

        return type;
    }

    /**
     * returns raw material amount of a special type
     * @param type
     * @return
     */
    public int getTypeCount(RawMaterialType type) {
        int typeCount = 0;

        switch (type) {
            case ORE:
                typeCount = oreCount;
                break;
            case WEAT:
                typeCount = weatCount;
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
}

