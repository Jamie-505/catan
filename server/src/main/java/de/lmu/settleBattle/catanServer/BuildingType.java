package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public enum BuildingType {

    @Expose
    @SerializedName(Constants.ROAD)
    ROAD,

    @Expose
    @SerializedName(Constants.SETTLEMENT)
    SETTLEMENT,

    @Expose
    @SerializedName(Constants.CITY)
    CITY;

    @Override
    public String toString(){
        switch (this){
            case ROAD:
                return Constants.ROAD;
            case SETTLEMENT:
                return Constants.SETTLEMENT;
            case CITY:
                return Constants.CITY;
            default: throw new AssertionError( "Unknown building type" + this);

        }
    }
}
