package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public enum Color {

    @Expose
    @SerializedName(Constants.ORANGE)
    ORANGE,

    @Expose
    @SerializedName(Constants.BLUE)
    BLUE,

    @Expose
    @SerializedName(Constants.WHITE)
    WHITE,

    @Expose
    @SerializedName(Constants.RED)
    RED;

    @Override
    public String toString(){
        switch (this){
            case ORANGE:
                return Constants.ORANGE;
            case BLUE:
                return Constants.BLUE;
            case WHITE:
                return Constants.WHITE;
            case RED:
                return Constants.RED;
            default: throw new AssertionError( "Unknown color" + this);

        }
    }
}
