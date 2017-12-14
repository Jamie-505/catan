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
        initializeLocations(type);
        this.costs = getCosts(type);
    }


    public Building(int owner, BuildingType type, Location[] loc) {
        this(type, owner);

        if (loc.length == locations.length) {
            this.locations = loc;
        } else throw new IllegalArgumentException();
    }

    public Building(BuildingType type, Location[] loc) {
        this(type);

        if (loc.length == locations.length) {
            this.locations = loc;
        } else throw new IllegalArgumentException();
    }

    public Building(BuildingType type, int owner) {
        this(type);
        this.owner = owner;
    }


    private void initializeLocations(BuildingType type) {
        int count = type.equals(BuildingType.ROAD) ? 2:3;
        locations = new Location[count];
    }
    //endregion

    public void build(Location[] loc) throws IllegalArgumentException {

        if (((this.type == BuildingType.SETTLEMENT || this.type == BuildingType.CITY) && loc.length == 3) ||
                this.type == BuildingType.ROAD && loc.length == 2) {
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

    public boolean isBuiltAroundHere(Location[] loc){
        int matchCount = 0;
        for (int i = 0; i < loc.length; i++){
            for (int j = 0; j < locations.length; j++) {
                if ((loc[i].compare(locations[j]))){
                    matchCount++;
                }
            }
        }
        return matchCount >= 2;
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
