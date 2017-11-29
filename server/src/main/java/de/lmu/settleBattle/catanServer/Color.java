package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public enum Color {

    @Expose
    @SerializedName("Orange")
    ORANGE,

    @Expose
    @SerializedName("Blau")
    BLUE,

    @Expose
    @SerializedName("Weiß")
    WHITE,

    @Expose
    @SerializedName("Rot")
    RED;

    @Override
    public String toString(){
        switch (this){
            case ORANGE:
                return "Orange";
            case BLUE:
                return "Blau";
            case WHITE:
                return "Weiß";
            case RED:
                return "Rot";
            default: throw new AssertionError( "Unknown color" + this);

        }
    }
}
