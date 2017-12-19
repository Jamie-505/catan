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
    @SerializedName(Constants.BUILDINGS)
    private ArrayList<Building> buildings;

    @Expose
    @SerializedName(Constants.HAVEN)
    private Haven[] havens;

    @Expose
    @SerializedName(Constants.ROBBER)
    private Robber robber;

    private int[] numberOrder = {5,2,6,3,8,10,9,12,11,4,8,10,9,4,5,6,3,11};
    private Location[] fieldLocations = new Location[37];
    private Location[] havenLocations = new Location[9];



    public Board() {
        fields = new Field[37];
        buildings = new ArrayList<>();
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
        ArrayList<Field> listOfFields = new ArrayList<>();
        //adding 3 tiles each
        for (int i=1; i <= 3; i++ ){
            listOfFields.add(new Field(RawMaterialType.CLAY));
            listOfFields.add(new Field(RawMaterialType.ORE));
            listOfFields.add(new Field(RawMaterialType.WOOD));
            listOfFields.add(new Field(RawMaterialType.WOOL));
            listOfFields.add(new Field(RawMaterialType.WEAT));
        }
        //adding 1 tile wood sheep and wheat each
            listOfFields.add(new Field(RawMaterialType.WOOD));
            listOfFields.add(new Field(RawMaterialType.WOOL));
            listOfFields.add(new Field(RawMaterialType.WEAT));




        // adding the fields in spiral way starting from bottom left corned (0,-2)
        // going counter clockwise towards the center
        // otherwise the numbers won't match the location
        fieldLocations[0] = new Location(0,-2);
        fieldLocations[1] = new Location(1, -2);
        fieldLocations[2] = new Location(2, -2);
        fieldLocations[3] = new Location(2,-1);
        fieldLocations[4] = new Location(2,0);
        fieldLocations[5] = new Location(1,1);
        fieldLocations[6] = new Location(0,2);
        fieldLocations[7] = new Location(-1,2);
        fieldLocations[8] = new Location(-2,2);
        fieldLocations[9] = new Location(-2,1);
        fieldLocations[10] = new Location(-2,0);
        fieldLocations[11] = new Location(-1,-1);
        fieldLocations[12] = new Location(0,-1);
        fieldLocations[13] = new Location(1,-1);
        fieldLocations[14] = new Location(1,0);
        fieldLocations[15] = new Location(0,1);
        fieldLocations[16] = new Location(-1,1);
        fieldLocations[17] = new Location(-1,0);
        fieldLocations[18] = new Location(0,0);

        //Waterfront
        fieldLocations[19] = new Location(0,-3);
        fieldLocations[20] = new Location(1,-3);
        fieldLocations[21] = new Location(2,-3);
        fieldLocations[22] = new Location(3,-3);
        fieldLocations[23] = new Location(3,-2);
        fieldLocations[24] = new Location(3,-1);
        fieldLocations[25] = new Location(3,0);
        fieldLocations[26] = new Location(2,1);
        fieldLocations[27] = new Location(1,2);
        fieldLocations[28] = new Location(0,3);
        fieldLocations[29] = new Location(-1,3);
        fieldLocations[30] = new Location(-2,3);
        fieldLocations[31] = new Location(-3,3);
        fieldLocations[32] = new Location(-3,2);
        fieldLocations[33] = new Location(-3,1);
        fieldLocations[34] = new Location(-3,0);
        fieldLocations[35] = new Location(-2,-1);
        fieldLocations[36] = new Location(-1,-2);

        //shuffle the fields and numbers
        Collections.shuffle(listOfFields);
        //put back in array
        for (int i=0; i <18; i++ ){
            listOfFields.get(i).setNumber(numberOrder[i]);
            listOfFields.get(i).setLocation(fieldLocations[i]);
            fields[i] = listOfFields.get(i);
        }

        // add desert in the middle
        fields[18] = new Field(fieldLocations[18], RawMaterialType.DESERT);

        // add water
        for (int i=19; i <37; i++ ){
            fields[i] = new Field(RawMaterialType.WATER);
        }
    }

    /**
     * this method initialize the havens
     */
    private void initializeHavens() {


        //initialize the locations
        havenLocations[0] = new Location(0,-3);
        havenLocations[1] = new Location(2,-3);
        havenLocations[2] = new Location(3,-2);
        havenLocations[3] = new Location(3,0);
        havenLocations[4] = new Location(1,2);
        havenLocations[5] = new Location(-1,3);
        havenLocations[6] = new Location(-3,3);
        havenLocations[7] = new Location(-3,1);
        havenLocations[8] = new Location(-2,-1);

        //create arrays for haven location by joining
        //the haven location with its adjacent field location to crete the havens
        Location[] havenLocation_1 = {havenLocations[0],fieldLocations[0]};
        Location[] havenLocation_2 = {havenLocations[1],fieldLocations[1]};
        Location[] havenLocation_3 = {havenLocations[2],fieldLocations[3]};
        Location[] havenLocation_4 = {havenLocations[3],fieldLocations[4]};
        Location[] havenLocation_5 = {havenLocations[4],fieldLocations[5]};
        Location[] havenLocation_6 = {havenLocations[5],fieldLocations[7]};
        Location[] havenLocation_7 = {havenLocations[6],fieldLocations[8]};
        Location[] havenLocation_8 = {havenLocations[7],fieldLocations[9]};
        Location[] havenLocation_9 = {havenLocations[8],fieldLocations[11]};

        //create the havens
        havens[0] = new  Haven(havenLocation_1,RawMaterialType.ORE);
        havens[1] = new  Haven(havenLocation_2,RawMaterialType.WOOL);
        havens[2] = new  Haven(havenLocation_3,RawMaterialType.WATER);
        havens[3] = new  Haven(havenLocation_4,RawMaterialType.WATER);
        havens[4] = new  Haven(havenLocation_5,RawMaterialType.WATER);
        havens[5] = new  Haven(havenLocation_6,RawMaterialType.WOOD);
        havens[6] = new  Haven(havenLocation_7,RawMaterialType.WEAT);
        havens[7] = new  Haven(havenLocation_8,RawMaterialType.WATER);
        havens[8] = new  Haven(havenLocation_9,RawMaterialType.CLAY);

    }

    public ArrayList<Location> getAdjacentLocations(Location location){
        int x = location.getX();
        int y = location.getY();
        ArrayList<Location> neighbors = new ArrayList();
        neighbors.add(new Location(x,y+1));
        neighbors.add(new Location(x,y-1));
        neighbors.add(new Location(x+1,y));
        neighbors.add(new Location(x-1,y));
        neighbors.add(new Location(x+1,y-1));
        neighbors.add(new Location(x-1,y+1));
        return neighbors;
    }



    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public void updateBoard(){
        //TODO
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public Location[] getValidLocation(Building b){
        //TODO
        return null;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */

    public boolean placeBuilding(int playerID, Location[] loc, BuildingType type ){
        boolean isOccupied = false;
        //create a building
        Building bld = new Building(playerID, type , loc);
        //check if the location is occupied
        Iterator<Building> iter = buildings.iterator();
        if (type == BuildingType.SETTLEMENT || type == BuildingType.CITY){
            if(loc.length != 3) return false;
            while (iter.hasNext()) {
                if (iter.next().isBuiltAroundHere(loc)){
                    isOccupied = true;
                    break;  //if the field is occupied the rest of the list can be ignored
                }
            }
            if (isOccupied == false){
                addBuilding(bld);
                return true;
            }
        }else if (type == BuildingType.ROAD){
            if(loc.length != 2) return false;
            while (iter.hasNext()) {
                Building nextBuilding = iter.next();
                if ((nextBuilding.isBuiltAroundHere(loc) && nextBuilding.getOwner() == playerID)) {
                    addBuilding(bld);
                    return true;
                }
            }
        }
        return false;
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

            for (Building bld : this.getBuildings()) {

                //exclude road from distribution
                if (bld.getType().equals(BuildingType.ROAD))
                    continue;

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

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    private void addBuilding(Building bld) {
        buildings.add(bld);
        changes.firePropertyChange("buildings", "", bld);
    }

    public void addPropertyChangeListener( PropertyChangeListener l )
    {
        changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l )
    {
        changes.removePropertyChangeListener( l );
    }
}



