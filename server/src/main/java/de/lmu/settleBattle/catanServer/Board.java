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
        fields = new Field[19];
        roads = new ArrayList<>();
        settlements = new ArrayList<>();
        cities = new ArrayList<>();
        havens = new Haven[9];
        robber = new Robber();

        try {
            initializeFields();
            initializeHavens();
        } catch (CatanException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * this method initializes the fields
     *
     * @throws CatanException
     */
    private void initializeFields() throws CatanException {
        //create a list of fields
        Field[] boardFields = new Field[18];
        //adding 3 tiles each
        for (int i = 0; i <= 2; i++) {
            boardFields[i * 5] = new Field(RawMaterialType.CLAY);
            boardFields[i * 5 + 1] = new Field(RawMaterialType.ORE);
            boardFields[i * 5 + 2] = new Field(RawMaterialType.WOOD);
            boardFields[i * 5 + 3] = new Field(RawMaterialType.WOOL);
            boardFields[i * 5 + 4] = new Field(RawMaterialType.WHEAT);
        }
        //adding 1 tile wood sheep and wheat each
        boardFields[15] = new Field(RawMaterialType.WOOD);
        boardFields[16] = new Field(RawMaterialType.WOOL);
        boardFields[17] = new Field(RawMaterialType.WHEAT);


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
        fields[18] = new Field(RawMaterialType.DESERT, fieldLocations[18]);
    }


    /**
     * this method initializes the havens
     *
     * @throws CatanException
     */
    private void initializeHavens() throws CatanException {

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
        havens[0] = new Haven(havenLocation_1, RawMaterialType.WHEAT);
        havens[1] = new Haven(havenLocation_2, RawMaterialType.WATER);
        havens[2] = new Haven(havenLocation_3, RawMaterialType.WOOD);
        havens[3] = new Haven(havenLocation_4, RawMaterialType.CLAY);
        havens[4] = new Haven(havenLocation_5, RawMaterialType.WATER);
        havens[5] = new Haven(havenLocation_6, RawMaterialType.WATER);
        havens[6] = new Haven(havenLocation_7, RawMaterialType.WOOL);
        havens[7] = new Haven(havenLocation_8, RawMaterialType.WATER);
        havens[8] = new Haven(havenLocation_9, RawMaterialType.ORE);

    }

    public Location getRandomFieldLoc() {
        Random random = new Random();
        return fields[random.nextInt(19)].getLocation();
    }

    /**
     * finds adjacent locations
     * @param location location object to find neighbors for
     * @return list of locations that are adjacent to a field ordered
     * as circle around location
     */
    public ArrayList<Location> getAdjacentLocations(Location location) {
        int x = location.getX();
        int y = location.getY();
        ArrayList<Location> neighbors = new ArrayList();

        try {
            neighbors.add(new Location(x, y + 1));
            neighbors.add(new Location(x + 1, y));
            neighbors.add(new Location(x + 1, y - 1));
            neighbors.add(new Location(x, y - 1));
            neighbors.add(new Location(x - 1, y));
            neighbors.add(new Location(x - 1, y + 1));
        } catch (CatanException ex) {
            ex.printStackTrace();
        }

        return neighbors;
    }

    /**
     * returns random adjacent road for road
     * finds road location array containing loc[0]
     * and adjacent to loc[1] if loc[0] is not water
     * field, otherwise the other way around
     *
     * @param loc Location array containing 2 locations
     * @return adjacent location or null if all are occupied
     */
    private Location[] getFreeRoadLoc(Location[] loc) {
        Location[] rLocs;

        int startLoc = loc[0].isWaterField() ? 1 : 0;
        int compareLoc = (startLoc + 1) % 2;

        Location l1 = loc[startLoc];
        Location lCompare = loc[compareLoc];

        List<Location> adjacentLocs = getAdjacentLocations(l1);
        int index = adjacentLocs.indexOf(lCompare);

        //there are 2 possibilities if loc[0] is fix and the other
        //location must be adjacent to loc[1] --> i < 2 in loop
        for (int i = 0; i < 2; i++) {
            //in 1st loop check one and in the other loop the other possibility
            int locInt = i == 0 ? (index + 1) : (index - 1);
            locInt = Math.floorMod(locInt, adjacentLocs.size());

            if (locInt == -1) locInt = adjacentLocs.size()-1;

            Location l2 = adjacentLocs.get(locInt);
            rLocs = new Location[]{l1, l2};

            //if location is free, take this one
            if (canBeBuiltHere(rLocs))
                return rLocs;
        }

        return null;
    }

    /**
     * returns random adjacent free road for road
     *
     * @param loc Location array containing 2 locations
     * @return adjacent location
     */
    public Location[] getFreeAdjacentRoad(Location[] loc) {
        if (loc.length != 2) return null;

        Location[] rLocs = getFreeRoadLoc(loc);

        //turn locations around so former loc[1] is fix and an adjacent road for loc[0] will be found
        if (rLocs == null && !loc[0].isWaterField()) rLocs = getFreeRoadLoc(new Location[] {loc[1], loc[0]});

        return rLocs;
    }




    /**
     * returns the harvest that is distributed for one building
     *
     * @param building
     * @return
     */
    public RawMaterialOverview getHarvest(Building building) throws CatanException {
        RawMaterialOverview overview = new RawMaterialOverview();

        if (building.isRoad() || building.getLocations().length <= 2) return overview;

        for (Location loc : building.getLocations()) {
            for (Field field : fields)
                if (loc.equals(field.getLocation())) {
                    overview.increase(field.getHarvest(), 1);
                    break;
                }
        }

        return overview;
    }

    public Location[] getFreeRoadLoc(Player player, boolean initialPhase) throws CatanException {
        List<Building> buildings = new ArrayList<Building>();

        if (initialPhase) buildings.add(player.getLastSettlement());
        else {
            //get any road connected to road or settlement of the player
            buildings.addAll(player.getSettlementsAndCities());
            buildings.addAll(player.roads);
        }

        Collections.shuffle(buildings);

        for (Building s : buildings) {
            if (s.isRoad()) {
                //find adjacent road
                Location[] rLocs = getFreeAdjacentRoad(s.getLocations());
                if (rLocs != null) return rLocs;

            } else {
                for (int i = 0; i < 3; i++) {
                    Location[] locs = new Location[2];

                    locs[0] = s.getLocations()[(i + 1) % 3];
                    locs[1] = s.getLocations()[i];

                    if (canBeBuiltHere(locs))
                        return locs;
                }
            }
        }

        throw new CatanException("Es wurde keine gültige Location für eine deiner Straßen gefunden", true);
    }

    /**
     * searches for free corners for settlements
     *
     * @return Location[] array to place a settlement at
     */
    public Location[] getRandomFreeSettlementLoc() {
        Location[] locs = null;

        while (!canBeBuiltHere(locs)) {
            locs = new Location[3];
            Random random = new Random();

            Location l1 = this.getFields()[random.nextInt(19)].getLocation();

            ArrayList<Location> adjacentLocations = this.getAdjacentLocations(l1);

            int index = random.nextInt(adjacentLocations.size());
            Location l2 = adjacentLocations.get(index);

            int index2 = index == adjacentLocations.size() - 1 ? 0 : index + 1;
            Location l3 = adjacentLocations.get(index2);

            locs[0] = l1;
            locs[1] = l2;
            locs[2] = l3;
        }

        return locs;
    }

    /**
     * checks if this location is already occupied or if it's a settlement
     * if distance rule is fulfilled
     *
     * @param locs locs to check for
     * @return true if the location is valid, false if not
     */
    public boolean canBeBuiltHere(Location[] locs) {
        if (locs == null) return false;

        boolean exactlyHere = false;
        ArrayList<Building> buildings = getSettlementsAndCities();

        if (locs.length == 2) {
            exactlyHere = true;
            buildings = getRoads();
            if (!isValidEdge(locs)) return false;
        } else if (!isValidCorner(locs)) return false;

        for (Building bld : buildings) {
            if (bld.isBuiltAroundHere(locs, exactlyHere))
                return false;
        }

        return true;
    }

    /**
     * checks if building is connected to a haven so the player is
     * allowed to trade at the havens' conditions
     *
     * @param building building object
     * @return true if the building is connected to a haven
     */
    public boolean isConnectedToHaven(Building building) {
        for (Haven haven : havens) {
            if (building.isBuiltAroundHere(haven.getLocations(), false)) {
                return true;
            }
        }

        return false;
    }

    /**
     * returns haven if the sLocs is connected with one
     * one sLocs can only be connected to one haven
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
     * places building on board
     *
     * @param bld          building to place
     * @param initialPhase initial phase is active
     * @return true if building was built, otherwise false
     */
    public boolean placeBuilding(Building bld, boolean initialPhase) throws CatanException {
        boolean built = false;

        switch (bld.getType()) {
            case SETTLEMENT:
                built = placeSettlement(bld, initialPhase);
                break;

            case CITY:
                built = placeCity(bld, initialPhase);
                break;

            case ROAD:
                built = placeRoad(bld);
                break;
        }
        return built;
    }

    private boolean placeSettlement(Building bld, boolean initialPhaseActive)
            throws CatanException {
        if (!bld.isSettlement()) return false;

        boolean cornerAroundIsOccupied = false;
        boolean edgeAroundIsOccupiedByMe = false;

        //create a building
        for (Building s : getSettlementsAndCities()) {
            if (s.isBuiltAroundHere(bld.getLocations(), false)) {
                cornerAroundIsOccupied = true;
                break;
            }
        }

        //if initial phase is active no edges need to be checked
        //if corner is already occupied, location is not buildable --> don't have to iterate over roads
        if (!initialPhaseActive && !cornerAroundIsOccupied) {
            for (Building r : roads) {
                if (r.isBuiltAroundHere(bld.getLocations(), false)) {
                    if (r.getOwner() == bld.getOwner()) {
                        edgeAroundIsOccupiedByMe = true;
                        break;
                    }
                }
            }
        }

        if (cornerAroundIsOccupied)
            //building cannot be placed because of distance rule or corner is already occupied
            throw new CatanException("Location bereits besetzt oder Abstandsregel nicht eingehalten", true);


        //in the initial phase a sLocs can be placed anywhere
        //afterwards it can only placed on corners that are connected to roads of the player
        if (initialPhaseActive || edgeAroundIsOccupiedByMe) {
            return this.addRoadOrSettlement(bld);
        }

        return false;
    }


    private boolean placeRoad(Building bld) throws CatanException {

        boolean cornerAroundIsOccupiedByMe = false;
        boolean edgeIsOccupied = false;
        boolean edgeAroundIsOccupiedByMe = false;

        //check if a building of owner is placed around
        for (Building s : getSettlementsAndCities()) {
            if (s.isBuiltAroundHere(bld.getLocations(), false) &&
                    s.getOwner() == bld.getOwner()) {
                cornerAroundIsOccupiedByMe = true;
                break;
            }
        }

        //check if road is connected to road or edge is already occupied
        for (Building r : roads) {
            if (r.isBuiltAroundHere(bld.getLocations(), true)) {
                edgeIsOccupied = true;
                break;
            } else if (r.isBuiltAroundHere(bld.getLocations(), false) &&
                    r.getOwner() == bld.getOwner()) {
                edgeAroundIsOccupiedByMe = true;
            }
        }

        if (edgeIsOccupied)
            throw new CatanException("Diese Kante ist bereits besetzt.", true);

        // roads can only be placed on edges that are connected to a sLocs/city of the player
        // or another road of the player
        if ((cornerAroundIsOccupiedByMe || edgeAroundIsOccupiedByMe)) {
            return this.addRoadOrSettlement(bld);
        }

        return false;
    }

    private boolean placeCity(Building bld, boolean initialPhaseActive)
            throws CatanException {

        if (initialPhaseActive)
            throw new CatanException("Stadt kann nicht in initialer Bauphase gebaut werden", true);

        boolean cornerIsOccupied = false;
        Building foundCorner = null;

        //check if settlement of player is already there
        for (Building s : getSettlements()) {
            if (s.getOwner() == bld.getOwner() &&
                    s.isBuiltAroundHere(bld.getLocations(), true)) {
                cornerIsOccupied = true;
                foundCorner = s;
                break;
            }
        }

        //Upgrading a sLocs happens here
        if (cornerIsOccupied) {
            return this.addCity(bld, foundCorner);
        }

        return false;
    }


    //region add building
    private boolean addCity(Building city, Building formerSettlement) {
        boolean ret = false;

        if (city.isBuiltAroundHere(formerSettlement.getLocations(), true) &&
                settlements.contains(formerSettlement)) {
            settlements.remove(formerSettlement);
            cities.add(city);

            ret = true;
        }

        if (ret) changes.firePropertyChange("new building", "", city);

        return ret;
    }

    private boolean addRoadOrSettlement(Building bld) {
        boolean ret = false;

        switch (bld.getType()) {
            case ROAD:
                if (isValidEdge(bld.getLocations())) {
                    ret = true;
                    roads.add(bld);
                }

            case SETTLEMENT:
                if (isValidCorner(bld.getLocations())) {
                    ret = true;
                    settlements.add(bld);
                }
        }


        if (ret) changes.firePropertyChange("new building", "", bld);

        return ret;
    }
    //endregion

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
    public Map<Building, RawMaterialType> getBuildingsOnDistributingFields(int number)
            throws CatanException {

        Map<Building, RawMaterialType> buildingList = new HashMap<>();

        Field[] fields = getFieldsWithNumber(number);

        for (Field field : fields) {

            //fields on which robber is placed do not distribute raw materials
            if (field.getLocation().equals(robber.getLocation()))
                continue;

            //iterate over cities and settlements
            List<Building> settlementsAndCities = getSettlementsAndCities();

            for (Building bld : settlementsAndCities) {

                //exclude road from distribution
                //this case cannot occur, but now it's tested
                if (bld.isRoad())
                    throw new CatanException("Für eine Straße werden keine Rohstoffe ausgeschüttet.", true);

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


    /**
     * get owners of settlements or cities near a specific Location
     *
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
    public void addRoad(Building bld) {
        if (bld.isRoad()) roads.add(bld);
    }

    public void addSettlement(Building bld) {
        if (bld.isSettlement()) settlements.add(bld);
    }

    //region unused

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


    //endregion
}
