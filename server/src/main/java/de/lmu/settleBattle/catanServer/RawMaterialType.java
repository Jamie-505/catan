package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Random;

public enum RawMaterialType {

    @Expose
    @SerializedName(Constants.CLAY)
    CLAY,

    @Expose
    @SerializedName(Constants.ORE)
    ORE,

    @Expose
    @SerializedName(Constants.WOOD)
    WOOD,

    @Expose
    @SerializedName(Constants.WOOL)
    WOOL,

    @Expose
    @SerializedName(Constants.WEAT)
    WEAT,
    DESERT,
    WATER;

    @Override
    public String toString(){
        switch (this){
            case CLAY:
                return Constants.CLAY;
            case ORE:
                return Constants.ORE;
            case WOOD:
                return Constants.WOOD;
            case WOOL:
                return Constants.WOOL;
            case WEAT:
                return Constants.WEAT;
            case WATER:
                return Constants.WATER;
            default: throw new AssertionError( "Unknown building type" + this);

        }
    }

    public boolean isValidTradingType() {
        return this.equals(RawMaterialType.CLAY) || this.equals(RawMaterialType.ORE) ||
                this.equals(RawMaterialType.WEAT) || this.equals(RawMaterialType.WOOL) ||
                this.equals(RawMaterialType.WOOD);
    }
}
