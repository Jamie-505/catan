package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RawMaterialOverview extends JSONStringBuilder{

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
    //endregion

    public int getTotalCount() {
        return clayCount+oreCount+woodCount+weatCount+woolCount;
    }

    //region increase
    /**
     * increases a raw material amount
     * @param type type of raw material to increase
     * @param i value to be added
     */
    public void increase(RawMaterialType type, int i) {
        //the method increase must not decrease an amount
        if (i < 0) return;

        switch(type) {
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
    //endregion

    //region decrease
    /**
     * decreases a raw material amount
     * @param type type of raw material to decrease
     * @param i value to be subtracted
     */
    public void decrease(RawMaterialType type, int i) throws Exception {
        if (i < 0) throw new Exception();


        switch(type) {
            case ORE:
                if(i>oreCount)throw new Exception();
                oreCount -= i;
                break;
            case CLAY:
                if(i>clayCount)throw new Exception();
                clayCount -= i;
                break;
            case WEAT:
                if(i>weatCount)throw new Exception();
                    weatCount -= i;
                break;
            case WOOD:
                if(i>woodCount)throw new Exception();
                    woodCount -= i;
                break;
            case WOOL:
                if(i>woolCount)throw new Exception();
                    woolCount -= i;
                break;
        }
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
}

