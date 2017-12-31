package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Field extends JSONStringBuilder {
    @Expose
    @SerializedName(Constants.PLACE)
    private Location location;

    @Expose
    @SerializedName(Constants.TYPE)
    private RawMaterialType harvest;

    @Expose
    @SerializedName(Constants.NUMBER)
    private int number;

    public Field(RawMaterialType harvest) {
        this.harvest = harvest;
    }

    public Field(RawMaterialType harvest, Location location) {
        this(harvest);
        this.location = location;
    }

    public Field(RawMaterialType harvest, Location location, int number) {
        this(harvest, location);
        this.number = number;
    }

    public RawMaterialType getHarvest() {
        return this.harvest;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location){
        this.location = location;
    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
