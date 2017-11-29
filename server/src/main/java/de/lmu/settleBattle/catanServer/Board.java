package de.lmu.settleBattle.catanServer;
import java.util.*;

public class Board {

    //public Map<Integer, Field[]> boardNumbers;
    public Field[] fields;
    public Building[] buildings;
    public Haven[] havens;
    public Robber robber;
    public DevelopmentCardOverview cardDeck;

    public Board() {
        fields = new Field[37];

        //each player gets 5 settlements, 4 cities and 15 streets. with 4 player there are 96 buildings
        buildings = new Building[96];
        havens = new Haven[7];
        robber = new Robber();
        cardDeck = new DevelopmentCardOverview();
    }

    public Board(int playerCount) {
        this();
        buildings = new Building[24*playerCount];
    }

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
    public void mapNumbersWithFields(){
        //TODO
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

