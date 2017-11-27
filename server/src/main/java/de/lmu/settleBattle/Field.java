package de.lmu.settleBattle;

public class Field {
    private Location location;
    private RawMaterialType harvestEarnings;

    public Field(Location location, RawMaterialType harvestEarnings) {
        this.location = location;
        this.harvestEarnings = harvestEarnings;
    }

    public RawMaterialType getHarvestEarnings() {
        return this.harvestEarnings;
    }

    public Location getLocation() {
        return this.location;
    }
}
