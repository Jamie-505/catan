package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import static de.lmu.settleBattle.catanServer.Constants.*;

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

    @Expose
    @SerializedName(Constants.VIEW_ID)
    private String viewID;

    private RawMaterialOverview costs;

    //region Constructors
    public Building() { }

    public Building(BuildingType type) {
        this.type = type;
        initializeLocations(type);
        this.costs = getCosts(type);
        this.viewID = null;
    }

    public Building(BuildingType type, int owner) {
        this(type);
        this.owner = owner;
    }


    public Building(BuildingType type, Location[] loc) throws CatanException {
        this(type);

        if (loc.length == locations.length) {
            this.locations = loc;
        } else throw new CatanException(
                String.format(INVALID_LOC_ARRAY, type.toString(), Arrays.toString(loc)),
                false);
    }

    public Building(int owner, BuildingType type, Location[] loc) throws CatanException {
        this(type, loc);
        this.owner = owner;
    }

    public Building(int owner, String viewID, BuildingType type, Location[] loc) throws CatanException {
        this(owner, type, loc);
        this.viewID = viewID;
    }

    private void initializeLocations(BuildingType type) {
        int count = type.equals(BuildingType.ROAD) ? 2 : 3;
        locations = new Location[count];
    }
    //endregion

    public void build(Location[] loc) throws CatanException {

        if (((this.isSettlement() || this.isCity()) && loc.length == 3) ||
                this.isRoad() && loc.length == 2) {
            this.locations = loc;
        } else throw new CatanException(String.format(INVALID_LOC_ARRAY, Arrays.toString(loc), this.type.toString()),
                false);
    }

    public int getOwner() {
        return owner;
    }

    public Location[] getLocations() {
        return locations;
    }

    /**
     * when given a location it would return true if the location is adjacent
     * @param loc
     * @return
     */
    public boolean edgesAreAdjacent(Location[] loc) {

        if (!this.isRoad() || loc.length != 2) return false;

        for (int i = 0; i < loc.length; i++) {
            for (int j = 0; j < this.locations.length; j++) {
                if (loc[i].equals(this.locations[j])) {
                    int iLocal = j == 0 ? 1 : 0;
                    int iRemote = i == 0 ? 1 : 0;

                    boolean xEquals = loc[iRemote].getX() == this.locations[iLocal].getX();
                    boolean yEquals = loc[iRemote].getY() == this.locations[iLocal].getY();

                    if (xEquals && yEquals) return false;

                    else if ((xEquals && Math.abs(loc[iRemote].getY() - this.locations[iLocal].getY()) == 1 ||
                        yEquals && Math.abs(loc[iRemote].getX() - this.locations[iLocal].getX()) == 1) ||
                        ( ( loc[iRemote].getX() - this.locations[iLocal].getX() ) +
                            ( loc[iRemote].getY() - this.locations[iLocal].getY() )  ) == 0  &&
                            Math.abs(loc[iRemote].getX() - this.locations[iLocal].getX()) == 1 &&
                            Math.abs(loc[iRemote].getY() - this.locations[iLocal].getY()) == 1)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * returns true if two Location[] Objects are adjacent and exactlyHere == false or
     * two Location[] Objects are equal and exactlyHere == true
     * otherwise false
     *
     * @param loc loc to search for buildings
     * @param exactlyHere specifies if searched for exact location or adjacent
     * @return true if around loc a building is built
     */
    public boolean isBuiltAroundHere(Location[] loc, boolean exactlyHere) {
        int matchCount = 0;
        boolean edgesAreAdjacent = false;

        if (this.isRoad() && loc.length == 2 && !exactlyHere)
            edgesAreAdjacent = edgesAreAdjacent(loc);

        else {
            for (Location aLoc : loc) {
                for (Location bLoc : locations) {
                    if ((aLoc.compare(bLoc))) {
                        matchCount++;
                    }
                }
            }
        }

        if (exactlyHere) {
            //if edge and corner are compared they can't be placed exactly on the same location
            if (this.isRoad() && loc.length != 2 || !this.isRoad() && loc.length == 2)
                return false;

            //one edge and one corner
            if (this.isRoad() || loc.length == 2) return matchCount > 1;

            //two corners
            else return matchCount > 2;

        } else {
            //two edges
            if (this.isRoad() && loc.length == 2) return edgesAreAdjacent;

            //one edge and one corner
            else if (this.isRoad() || loc.length == 2) return matchCount > 1;

            //two corners
            else return matchCount >= 2;
        }
    }

    public boolean isSettlement() { return this.type != null && this.type.equals(BuildingType.SETTLEMENT); }

    public boolean isCity() { return this.type != null && this.type.equals(BuildingType.CITY); }

    public boolean isRoad() { return this.type != null && this.type.equals(BuildingType.ROAD); }

    public BuildingType getType() {
        return type;
    }

    public RawMaterialOverview getCost() {
        return costs;
    }

    /**
     * get costs of a special Building type
     * @param type BuildingType to get costs of
     * @return costs of building type
     */
    public static RawMaterialOverview getCosts(BuildingType type) {
        switch (type) {
            case ROAD:
                return new RawMaterialOverview(1, 0, 1, 0, 0);
            case SETTLEMENT:
                return new RawMaterialOverview(1, 0, 1, 1, 1);
            case CITY:
                return new RawMaterialOverview(0, 3, 0, 0, 2);
        }
        return null;
    }

    @Override
    public String toString() {
        String locString = "";

        for (Location loc : this.locations) {
            locString += "<"+loc.toString()+"> ";
        }

        return this.type.toString() + ":" + locString;
    }
}
