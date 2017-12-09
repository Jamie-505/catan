package de.lmu.settleBattle.catanServer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Building extends JSONStringBuilder {

    @Expose
    @SerializedName(Constants.OWNER)
    private int owner;

    @Expose
    @SerializedName(Constants.TYPE)
    private BuildingType type;

    @Expose
    @SerializedName(Constants.PLACE)
    private Location[] locations;

    private RawMaterialOverview costs;

    //region Constructors
    public Building() { }

    public Building(BuildingType type) {
        this.type = type;
        this.costs = getCosts(type);
    }

    public Building(int playerID, BuildingType type, Location[] location) {
        this.owner = playerID;
        this.type = type;
        this.costs = getCosts(type);
        if (location.length == locations.length) {
            for (int i = 0; i < location.length; i++) {
                this.locations[i] = location[i];
            }
        } else throw new IllegalArgumentException();
    }

    public Building(BuildingType type, Location[] loc) {
        this.type = type;
        if (loc.length == locations.length) {
            for (int i = 0; i < loc.length; i++) {
                this.locations[i] = loc[i];
            }
        } else throw new IllegalArgumentException();
    }

    public Building(BuildingType type, int owner) {
        this(type);
        this.owner = owner;
    }
    //endregion

    public void build(Location[] loc) throws IllegalArgumentException {

        if (loc.length == 3) {
            this.locations = loc;
        }

        else throw new IllegalArgumentException();
    }


    public int getOwner() {
        return owner;
    }

    public Location[] getLocations() {
        return locations;
    }

    public boolean isBuiltHere(Location[] loc){
        boolean exist = false;
        int matchCount = 0;
        for (int i = 0; i > loc.length; i++){
            for (int j = 0; j > locations.length; j++) {
                if ((loc[i].compare(locations[j]))){
                    matchCount++;
                }
            }
        }
        return matchCount == 3;
    }

    public BuildingType getType() {
        return type;
    }

    public RawMaterialOverview getCost(){ return costs; }

    /**
     * get costs of a special Building type
     * @param type BuildingType
     * @return RawMaterialOverview object
     */
    public static RawMaterialOverview getCosts(BuildingType type) {
        switch(type) {
            case ROAD:
                return new RawMaterialOverview(1, 0, 1, 0, 0);
            case SETTLEMENT:
                return new RawMaterialOverview(1, 0, 1, 1, 1);
            case CITY:
                return new RawMaterialOverview(0, 3, 0, 0, 2);
        }
        return null;
    }
}
