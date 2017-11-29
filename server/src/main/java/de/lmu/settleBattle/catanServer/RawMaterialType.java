package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public enum RawMaterialType {

    @Expose
    @SerializedName("Lehm")
    CLAY,

    @Expose
    @SerializedName("Erz")
    ORE,

    @Expose
    @SerializedName("Holz")
    WOOD,

    @Expose
    @SerializedName("Wolle")
    WOOL,

    @Expose
    @SerializedName("Getreide")
    WEAT,
    NONE;

    @Override
    public String toString(){
        switch (this){
            case CLAY:
                return "Lehm";
            case ORE:
                return "Erz";
            case WOOD:
                return "Holz";
            case WOOL:
                return "Wolle";
            case WEAT:
                return "Getreide";
            case NONE:
                return "Keins";
            default: throw new AssertionError( "Unknown building type" + this);

        }
    }
}
