package de.lmu.settleBattle.catanServer;
public class Building {

    private BuildingType type;
    private Color color;
    private Location[] locations;
    private RawMaterialOverview costs;

    //region Constructors
    public Building() { }

    public Building(BuildingType type) {
        this.type = type;
        this.costs = getCosts(type);
    }

    public Building(BuildingType type, Color color) {
        this(type);
        this.color = color;
    }
    //endregion

    public void build(Location loc){
        //TODO
    }

    public Color getColor() {
        return color;
    }

    public Location[] getLocations() {
        return locations;
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
