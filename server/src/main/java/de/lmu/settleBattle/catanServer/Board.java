package de.lmu.settleBattle.catanServer;
import java.util.*;

public class Board {

    public Map<Integer, Field[]> boardNumbers;
    public Field[] fields;
    public Building[] buildings;
    public Haven[] havens;
    public Robber robber;
    public DevelopmentCard[] cardDeck;

    //TODO: maybe we do not need this as the costs are stored in the Building class
    public Map<Building, RawMaterialOverview> buildingCost;  //Objects here is RawMaterialOverview


    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public void initializeBoard (){
        //TODO
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

    public boolean placeBuilding(Building b, Location loc){
        //TODO
        return false;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public Map<Integer,Field[]> mapNumbersWithFields(){
        //TODO
        return null;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public void placeFields(){
        //TODO
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public void placeHaven(){
        //TODO
    }
}

