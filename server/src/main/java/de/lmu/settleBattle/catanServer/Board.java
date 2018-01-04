package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.Collections;


public class Board extends JSONStringBuilder {

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    @Expose
    @SerializedName(Constants.FIELDS)
    private Field[] fields;

    @Expose
    @SerializedName(Constants.ROAD)
    private ArrayList<Building> roads;

    @Expose
    @SerializedName(Constants.SETTLEMENT)
    private ArrayList<Building> settlements;

    @Expose
    @SerializedName(Constants.CITY)
    private ArrayList<Building> cities;


    @Expose
    @SerializedName(Constants.HAVEN)
    private Haven[] havens;

    @Expose
    @SerializedName(Constants.ROBBER)
    private Robber robber;

    private int[] numberOrder = {5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11};
    private Location[] fieldLocations = new Location[37];
    private Location[] havenLocations = new Location[9];


    public Board() {
        fields = new Field[37];
        roads = new ArrayList<>();
        settlements = new ArrayList<>();
        cities = new ArrayList<>();
        havens = new Haven[9];
        robber = new Robber();
        initializeFields();
        initializeHavens();
    }

//
//    /**
//     * this mehod creats a ring on fields
//     * @param level
//     */
//    public Location[] createFieldLocationsLevel(int level){
//        int numberOfFields = level*6;
//        int originX = 0;
//        int originY = 0;
//        Location[] locations = new Location[numberOfFields];
//        for(int i = 1; i <= numberOfFields; i++){
//            originY = originY - level;
//            if (originX == level){
//
//                for(int j = 1; j <= level; j++){
//                    locations[i] = new Location(originX, originY);
//                    i++;
//                }
//            }
//            if (originY == -level){
//                for(int j = 1; j <= level; j++){
//                    locations[i] = new Location(originX, originY);
//                    i++;
//                    originX++;
//                }
//            }
//            locations[i] = new Location(originX, originY);
//            ++originX;
//
//        }
//        return locations;
//    }
    /**
     * this method initialize the field
     */
    private void initializeFields() {
        //create a list of fields
        Field[] boardFields = new Field[18];
        //adding 3 tiles each
        for (int i = 0; i <= 2; i++) {
            boardFields[i*5] = new Field(RawMaterialType.CLAY);
            boardFields[i*5 + 1] = new Field(RawMaterialType.ORE);
            boardFields[i*5 + 2] = new Field(RawMaterialType.WOOD);
            boardFields[i*5 + 3] = new Field(RawMaterialType.WOOL);
            boardFields[i*5 + 4] = new Field(RawMaterialType.WEAT);
        }
        //adding 1 tile wood sheep and wheat each
        boardFields[15] = new Field(RawMaterialType.WOOD);
        boardFields[16] = new Field(RawMaterialType.WOOL);
        boardFields[17] = new Field(RawMaterialType.WEAT);


        // adding the fields in spiral way starting from bottom left corned (0,-2)
        // going counter clockwise towards the center
        // otherwise the numbers won't match the location
        fieldLocations[0] = new Location(0, -2);
        fieldLocations[1] = new Location(1, -2);
        fieldLocations[2] = new Location(2, -2);
        fieldLocations[3] = new Location(2, -1);
        fieldLocations[4] = new Location(2, 0);
        fieldLocations[5] = new Location(1, 1);
        fieldLocations[6] = new Location(0, 2);
        fieldLocations[7] = new Location(-1, 2);
        fieldLocations[8] = new Location(-2, 2);
        fieldLocations[9] = new Location(-2, 1);
        fieldLocations[10] = new Location(-2, 0);
        fieldLocations[11] = new Location(-1, -1);
        fieldLocations[12] = new Location(0, -1);
        fieldLocations[13] = new Location(1, -1);
        fieldLocations[14] = new Location(1, 0);
        fieldLocations[15] = new Location(0, 1);
        fieldLocations[16] = new Location(-1, 1);
        fieldLocations[17] = new Location(-1, 0);
        fieldLocations[18] = new Location(0, 0);

        //Waterfront
        fieldLocations[19] = new Location(0, -3);
        fieldLocations[20] = new Location(1, -3);
        fieldLocations[21] = new Location(2, -3);
        fieldLocations[22] = new Location(3, -3);
        fieldLocations[23] = new Location(3, -2);
        fieldLocations[24] = new Location(3, -1);
        fieldLocations[25] = new Location(3, 0);
        fieldLocations[26] = new Location(2, 1);
        fieldLocations[27] = new Location(1, 2);
        fieldLocations[28] = new Location(0, 3);
        fieldLocations[29] = new Location(-1, 3);
        fieldLocations[30] = new Location(-2, 3);
        fieldLocations[31] = new Location(-3, 3);
        fieldLocations[32] = new Location(-3, 2);
        fieldLocations[33] = new Location(-3, 1);
        fieldLocations[34] = new Location(-3, 0);
        fieldLocations[35] = new Location(-2, -1);
        fieldLocations[36] = new Location(-1, -2);

        //shuffle the fields and numbers
        Collections.shuffle(Arrays.asList(boardFields));
        //put back in array
        for (int i = 0; i < 18; i++) {
            Field field = boardFields[i];
            field.setNumber(numberOrder[i]);
            field.setLocation(fieldLocations[i]);
            boardFields[i] = field;
        }

        fields = new Field[19];
        System.arraycopy(boardFields, 0, fields, 0, boardFields.length);
        // add desert in the middle
        fields[18] = new Field(fieldLocations[18], RawMaterialType.DESERT);
    }

    /**
     * this method initialize the havens
     */
    private void initializeHavens() {


        //initialize the locations
        havenLocations[0] = new Location(1, -3);
        havenLocations[1] = new Location(3, -3);
        havenLocations[2] = new Location(3, -1);
        havenLocations[3] = new Location(2, 1);
        havenLocations[4] = new Location(0, 3);
        havenLocations[5] = new Location(-2, 3);
        havenLocations[6] = new Location(-3, 2);
        havenLocations[7] = new Location(-3, 0);
        havenLocations[8] = new Location(-1, -2);

        //create arrays for haven location by joining
        //the haven location with its adjacent field location to crete the havens
        Location[] havenLocation_1 = {havenLocations[0], fieldLocations[1]};
        Location[] havenLocation_2 = {havenLocations[1], fieldLocations[2]};
        Location[] havenLocation_3 = {havenLocations[2], fieldLocations[3]};
        Location[] havenLocation_4 = {havenLocations[3], fieldLocations[5]};
        Location[] havenLocation_5 = {havenLocations[4], fieldLocations[6]};
        Location[] havenLocation_6 = {havenLocations[5], fieldLocations[7]};
        Location[] havenLocation_7 = {havenLocations[6], fieldLocations[9]};
        Location[] havenLocation_8 = {havenLocations[7], fieldLocations[10]};
        Location[] havenLocation_9 = {havenLocations[8], fieldLocations[11]};

        //create the havens
        havens[0] = new Haven(havenLocation_1, RawMaterialType.WEAT);
        havens[1] = new Haven(havenLocation_2, RawMaterialType.WATER);
        havens[2] = new Haven(havenLocation_3, RawMaterialType.WOOD);
        havens[3] = new Haven(havenLocation_4, RawMaterialType.CLAY);
        havens[4] = new Haven(havenLocation_5, RawMaterialType.WATER);
        havens[5] = new Haven(havenLocation_6, RawMaterialType.WATER);
        havens[6] = new Haven(havenLocation_7, RawMaterialType.WOOL);
        havens[7] = new Haven(havenLocation_8, RawMaterialType.WATER);
        havens[8] = new Haven(havenLocation_9, RawMaterialType.ORE);

    }

    public ArrayList<Location> getAdjacentLocations(Location location) {
        int x = location.getX();
        int y = location.getY();
        ArrayList<Location> neighbors = new ArrayList();
        neighbors.add(new Location(x, y + 1));
        neighbors.add(new Location(x, y - 1));
        neighbors.add(new Location(x + 1, y));
        neighbors.add(new Location(x - 1, y));
        neighbors.add(new Location(x + 1, y - 1));
        neighbors.add(new Location(x - 1, y + 1));
        return neighbors;
    }

    /**
     * <method name: none>
     * <description: none>
     * <preconditions: none>
     * <postconditions: none>
     */
    public Location[] getValidLocation(Building b) {
        //TODO
        return null;
    }

    public boolean isConnectedToHaven(Building building) {
        for (Haven haven : havens) {
            if (building.isBuiltAroundHere(haven.getLocations(), false)) {
                return true;
            }
        }

        return false;
    }

    /**
     * returns haven if the settlement is connected with one
     * one settlement can only be connected to one haven
     *
     * @param building
     * @return
     */
    public Haven getConnectedHaven(Building building) {
        for (Haven haven : havens) {
            if (building.isBuiltAroundHere(haven.getLocations(), false))
                return haven;
        }

        return null;
    }

    /**
     * <method name: none>
     * <description: none>
     * <preconditions: none>
     * <postconditions: none>
     */

    public boolean placeBuilding(int playerID, Location[] loc, BuildingType type, boolean initialPhase) {
        boolean cornerIsOccupied = false;
        boolean edgeIsOccupied = false;
        boolean edgeIsAround = false;
        Building foundCorner = null;
        Building foundEdge = null;



        //create a building
        Building bld = new Building(playerID, type, loc);
        for (Building s : settlements) {
            if (s.isBuiltAroundHere(loc, false)) {
                cornerIsOccupied = true;
                foundCorner = s;
                break;
            }
        }
        for (Building c : cities) {
            if (c.isBuiltAroundHere(loc, false)) {
                cornerIsOccupied = true;
                foundCorner = c;
                break;
            }

        }

        for (Building r : roads) {
            if (r.isBuiltAroundHere(loc, true)) {
                edgeIsOccupied = true;
                break;
            }

            else if (r.isBuiltAroundHere(loc, false)) {
                edgeIsAround = true;
                foundEdge = r;
            }
        }

        if (type.equals(BuildingType.SETTLEMENT) && !cornerIsOccupied) {
            //in the initial phase a settlement can be placed anywhere
            //afterwards it can only placed on corners that are connected to roads of the player
            if (initialPhase || edgeIsAround && foundEdge.getOwner() == playerID) {
                return this.addBuilding(bld);
            }

        } else if (type.equals(BuildingType.CITY)) {
            if (initialPhase) {
                return false;
                //Upgrading a settlement happens here
            } else if (cornerIsOccupied && foundCorner.getOwner() == playerID &&
                    foundCorner.isSettlement()) {
                return this.addBuilding(bld);
            }
        } else if (type.equals(BuildingType.ROAD) && !edgeIsOccupied) {
            // roads can only be placed on edges that are connected to a settlement/city of the player or
            // another road of the player
            if (cornerIsOccupied && foundCorner.getOwner() == playerID ||
                    edgeIsAround && foundEdge.getOwner() == playerID) {
                return this.addBuilding(bld);
            }
        }

        return false;
    }

    public ArrayList<Building> getRoads() {
        return roads;
    }

    public ArrayList<Building> getSettlements() {
        return settlements;
    }

    public ArrayList<Building> getCities() {
        return cities;
    }

    public Field[] getFields() {
        return fields;
    }

    public Haven[] getHavens() {
        return havens;
    }

    public Robber getRobber() {
        return robber;
    }

    //region getFieldsWithNumber

    /**
     * get fields that are mapped with diced number
     *
     * @param number
     * @return field array containing all fields that are mapped with number
     */
    public Field[] getFieldsWithNumber(int number) {
        Field[] fieldArray = new Field[2];
        int maxSize = 2;

        if (number == 2 || number == 12) {
            fieldArray = new Field[1];
            maxSize = 1;
        }

        int index = 0;

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getNumber() == number) {
                fieldArray[index] = fields[i];
                index++;
            }

            if (index == maxSize)
                break;
        }

        return fieldArray;
    }
    //endregion

    //region getBuildingsOnDistributingFields

    /**
     * returns all buildings that are placed on fields that
     * are mapped with the diced number and on where the
     * robber is not placed on
     *
     * @param number
     * @return
     */
    public Map<Building, RawMaterialType> getBuildingsOnDistributingFields(int number) {
        Map<Building, RawMaterialType> buildingList = new HashMap<>();

        Field[] fields = getFieldsWithNumber(number);

        for (Field field : fields) {

            //fields on which robber is placed do not distribute raw materials
            if (field.getLocation().equals(robber.getLocation()))
                continue;

            //iterate over cities and settlements
            List<Building> settlementsAndCities = new ArrayList<>();
            settlementsAndCities.addAll(cities);
            settlementsAndCities.addAll(settlements);

            for (Building bld : settlementsAndCities) {

                //exclude road from distribution
                //this case cannot occur, but now it's tested
                if (bld.isRoad())
                    throw new IllegalArgumentException("No road should be in settlements or cities list");

                //player retrieves raw materials for building
                for (Location loc : bld.getLocations()) {
                    if (loc.equals(field.getLocation())) {
                        buildingList.put(bld, field.getHarvest());
                    }
                }
            }
        }

        return buildingList;
    }
    //endregion

    private boolean addBuilding(Building bld) {
        boolean ret = false;

        switch (bld.getType()) {
            case ROAD:
                if (isValidEdge(bld.getLocations())) {
                    ret = true;
                    roads.add(bld);
                }
            case CITY:
                if (isValidCorner(bld.getLocations())) {
                    ret = true;
                    cities.add(bld);
                }
            case SETTLEMENT:
                if (isValidCorner(bld.getLocations())) {
                    ret = true;
                    settlements.add(bld);
                }
        }


        if (ret) changes.firePropertyChange("buildings", "", bld);
        return ret;
    }

    /**
     * get owners of settlements or cities near a specific Location
     * @param loc
     * @return
     */
    public HashSet<Integer> getOwnersToRobFrom(int currentId, Location loc) {
        HashSet<Integer> owners = new HashSet<>();
        ArrayList<Building> buildings = getSettlementsAndCities();

        for (Building bld : buildings) {

            //cannot rob himself
            if (bld.getOwner() == currentId) continue;

            for (Location l : bld.getLocations()) {
                if (l.equals(loc) && !bld.isRoad()) {
                    owners.add(bld.getOwner());
                    continue;
                }
            }
        }

        return owners;
    }

    /**
     * @return array list containing all settlements and cities on board
     */
    public ArrayList<Building> getSettlementsAndCities() {
        ArrayList<Building> buildings = new ArrayList<>();
        buildings.addAll(settlements);
        buildings.addAll(cities);

        return buildings;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }

    public int getBuildingsSize() {
        return roads.size() + settlements.size() + cities.size();
    }

    public int getWaterFieldCount(Location[] locs) {
        int matchCount = 0;

        for (Location loc : locs) {
            if (loc.isWaterField()) matchCount++;
        }

        return matchCount;
    }

    public boolean isValidCorner(Location[] locs) {
        if (locs.length != 3) return false;
        return getWaterFieldCount(locs) < 3;
    }

    public boolean isValidEdge(Location[] locs) {
        if (locs.length != 2) return false;
        return getWaterFieldCount(locs) < 2;
    }



    //____________FOR TESTING___________________________
    public void addRoad(Building bld) { if (bld.isRoad()) roads.add(bld); }
    public void addSettlement(Building bld) {if (bld.isSettlement()) settlements.add(bld); }
}
