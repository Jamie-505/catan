package de.lmu.settleBattle.catanServer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static de.lmu.settleBattle.catanServer.BuildingType.*;
import static org.junit.Assert.*;

public class BoardTest {

    Board board;
    List<Building> buildings;

    //region setUp
    @Before
    public void setUp() {
        board = new Board();
        buildings = new ArrayList<>();

        buildings.add(new Building(2, SETTLEMENT, new Location[]{new Location(-2, 2), new Location(-2, 1), new Location(-1, 1)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(-2, 1), new Location(-1, 1)}));
        buildings.add(new Building(0, SETTLEMENT, new Location[]{new Location(-1, 1), new Location(0, 1), new Location(0, 0)}));
        buildings.add(new Building(0, ROAD, new Location[]{new Location(-1, 1), new Location(0, 1)}));
        buildings.add(new Building(1, SETTLEMENT, new Location[]{new Location(1, -1), new Location(2, -1), new Location(2, -2)}));
        buildings.add(new Building(1, ROAD, new Location[]{new Location(1, -1), new Location(2, -2)}));

        buildings.add(new Building(1, ROAD, new Location[] {new Location(2,-2), new Location(3,-2)}));
        buildings.add(new Building(1, SETTLEMENT, new Location[] {new Location(2,-2), new Location(3,-2), new Location(3,-3)}));

        buildings.add(new Building(2, SETTLEMENT, new Location[]{new Location(1, 0), new Location(1, 1), new Location(2, 0)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(-2, 1), new Location(-1, 0)}));

        buildings.add(new Building(1, SETTLEMENT, (new Location[]{new Location(-2, 1), new Location(-1, 1), new Location(-1, 0)})));
        buildings.add(new Building(1, ROAD, new Location[]{new Location(-1, 1), new Location(-2, 1)}));
        buildings.add(new Building(0, SETTLEMENT, new Location[]{new Location(-2, 1), new Location(-2, 2), new Location(-1, 1)}));
        buildings.add(new Building(0, ROAD, new Location[]{new Location(1, -1), new Location(2, -2)}));

        buildings.add(new Building(2, ROAD, new Location[]{new Location(3,0), new Location(2,1)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(1,2), new Location(2,1)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(-3,2), new Location(-3,3)}));
    }
    //endregion

    //region build_initialPhase
    @Test
    public void build_initialPhase() {
        for (int i = 0; i < buildings.size(); i++) {
            boolean expected = i >= 10 || i == 6 ? false : true;
            int expectedSize = expected ? board.getBuildingsSize() + 1 : board.getBuildingsSize();

            Building bld = new Building(buildings.get(i).getOwner(), buildings.get(i).getType(),
                    buildings.get(i).getLocations());
            boolean built = board.placeBuilding(bld, true);

            assertTrue(expected == built);
            assertTrue(board.getBuildingsSize() == expectedSize);
        }
    }
    //endregion

    @Test
    public void isBuiltAroundHere() {
        //settlements
        Building settlement = buildings.get(0);
        assertTrue(settlement.isBuiltAroundHere(buildings.get(0).getLocations(), true));
        assertTrue(settlement.isBuiltAroundHere(buildings.get(0).getLocations(), false));

        assertFalse(settlement.isBuiltAroundHere(buildings.get(10).getLocations(), true));
        assertTrue(settlement.isBuiltAroundHere(buildings.get(10).getLocations(), false));

        assertFalse(settlement.isBuiltAroundHere(
                new Location[]{new Location(0, 0), new Location(0, 1), new Location(-1, 1)}, true));
        assertFalse(settlement.isBuiltAroundHere(
                new Location[]{new Location(0, 0), new Location(0, 1), new Location(-1, 1)}, false));


        Building road = buildings.get(1);
        assertFalse(road.isBuiltAroundHere(new Location[] {new Location(-2,2), new Location(-1,1)}, true));
        assertTrue(road.isBuiltAroundHere(new Location[] {new Location(-2,1), new Location(-1,1)}, true));

        //adjacent edges
        assertTrue(road.isBuiltAroundHere(new Location[] {new Location(-1,1), new Location(-2,2)}, false));
        assertTrue(road.isBuiltAroundHere(new Location[] {new Location(-2,1), new Location(-1,0)}, false));
        assertTrue(road.isBuiltAroundHere(new Location[] {new Location(-1,0), new Location(-1,1)}, false));
        assertTrue(road.isBuiltAroundHere(new Location[] {new Location(-2,1), new Location(-2,2)}, false));

        //non-adjacent edges
        assertFalse(road.isBuiltAroundHere(new Location[] {new Location(0,0), new Location(0,1)}, false));
        assertFalse(road.isBuiltAroundHere(new Location[] {new Location(2,-2), new Location(2,-1)}, false));
    }

    @Test
    public void build_gameStarted() {
        board.addRoad(new Building(2, ROAD, new Location[] {new Location(-2,2), new Location(-1,1)} ));
        board.addRoad(new Building(0, ROAD, new Location[] {new Location(-1,1), new Location(0,0)} ));
        board.addRoad(new Building(1, ROAD, new Location[] {new Location(2,-1), new Location(2,-2)} ));
        board.addRoad(new Building(1, ROAD, new Location[] {new Location(-2,1), new Location(-1,0)} ));

        for (int i = 0; i < buildings.size(); i++) {
            boolean expected = i >= 8 ? false : true;
            int expectedSize = expected ? board.getBuildingsSize() + 1 : board.getBuildingsSize();

            Building bld = new Building(buildings.get(i).getOwner(), buildings.get(i).getType(),
                    buildings.get(i).getLocations());
            boolean built = board.placeBuilding(bld, false);

            assertTrue(expected == built);
            assertTrue(board.getBuildingsSize() == expectedSize);
        }
    }

    @Test
    public void robberTest() {
        Location desert = new Location(0,0);
        assertFalse(board.getRobber().isValidNewLocation(desert));

        Location water = new Location(0,-3);
        assertFalse(board.getRobber().isValidNewLocation(water));

        Location land = new Location(-2,0);
        assertTrue(board.getRobber().isValidNewLocation(land));
    }
}