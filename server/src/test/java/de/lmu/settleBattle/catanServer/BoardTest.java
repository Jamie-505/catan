package de.lmu.settleBattle.catanServer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.lmu.settleBattle.catanServer.BuildingType.*;
import static org.junit.Assert.*;

public class BoardTest {

    Board board;
    List<Building> buildings;
    Board roadBoard;
    ArrayList<Building> roads;

    //region setUp
    @Before
    public void setUp() throws CatanException {
        board = new Board();
        buildings = new ArrayList<>();

        buildings.add(new Building(2, SETTLEMENT, new Location[]{new Location(-2, 2), new Location(-2, 1), new Location(-1, 1)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(-2, 1), new Location(-1, 1)}));
        buildings.add(new Building(0, SETTLEMENT, new Location[]{new Location(-1, 1), new Location(0, 1), new Location(0, 0)}));
        buildings.add(new Building(0, ROAD, new Location[]{new Location(-1, 1), new Location(0, 1)}));
        buildings.add(new Building(1, SETTLEMENT, new Location[]{new Location(1, -1), new Location(2, -1), new Location(2, -2)}));
        buildings.add(new Building(1, ROAD, new Location[]{new Location(1, -1), new Location(2, -2)}));

        buildings.add(new Building(1, ROAD, new Location[]{new Location(2, -2), new Location(3, -2)}));
        buildings.add(new Building(1, SETTLEMENT, new Location[]{new Location(2, -2), new Location(3, -2), new Location(3, -3)}));

        buildings.add(new Building(2, SETTLEMENT, new Location[]{new Location(1, 0), new Location(1, 1), new Location(2, 0)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(-2, 1), new Location(-1, 0)}));

        buildings.add(new Building(1, SETTLEMENT, (new Location[]{new Location(-2, 1), new Location(-1, 1), new Location(-1, 0)})));
        buildings.add(new Building(1, ROAD, new Location[]{new Location(-1, 1), new Location(-2, 1)}));
        buildings.add(new Building(0, SETTLEMENT, new Location[]{new Location(-2, 1), new Location(-2, 2), new Location(-1, 1)}));
        buildings.add(new Building(0, ROAD, new Location[]{new Location(1, -1), new Location(2, -2)}));

        buildings.add(new Building(2, ROAD, new Location[]{new Location(3, 0), new Location(2, 1)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(1, 2), new Location(2, 1)}));
        buildings.add(new Building(2, ROAD, new Location[]{new Location(-3, 2), new Location(-3, 3)}));
    }
    //endregion

    //region build_initialPhase
    @Test
    public void build_initialPhase() throws CatanException {
        for (int i = 0; i < buildings.size(); i++) {
            boolean expected = i < 10 && i != 6;
            int expectedSize = expected ? board.getBuildingsSize() + 1 : board.getBuildingsSize();

            Building bld = new Building(buildings.get(i).getOwner(), buildings.get(i).getType(),
                    buildings.get(i).getLocations());

            boolean built;
            try {
                built = board.placeBuilding(bld, true);
            } catch (CatanException ex) {
                built = false;
            }


            assertTrue(expected == built);
            assertTrue(board.getBuildingsSize() == expectedSize);
        }
    }
    //endregion

    @Test
    public void isBuiltAroundHere() throws CatanException {
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
        assertFalse(road.isBuiltAroundHere(new Location[]{new Location(-2, 2), new Location(-1, 1)}, true));
        assertTrue(road.isBuiltAroundHere(new Location[]{new Location(-2, 1), new Location(-1, 1)}, true));

        //adjacent edges
        assertTrue(road.isBuiltAroundHere(new Location[]{new Location(-1, 1), new Location(-2, 2)}, false));
        assertTrue(road.isBuiltAroundHere(new Location[]{new Location(-2, 1), new Location(-1, 0)}, false));
        assertTrue(road.isBuiltAroundHere(new Location[]{new Location(-1, 0), new Location(-1, 1)}, false));
        assertTrue(road.isBuiltAroundHere(new Location[]{new Location(-2, 1), new Location(-2, 2)}, false));

        //non-adjacent edges
        assertFalse(road.isBuiltAroundHere(new Location[]{new Location(0, 0), new Location(0, 1)}, false));
        assertFalse(road.isBuiltAroundHere(new Location[]{new Location(2, -2), new Location(2, -1)}, false));
    }

    @Test
    public void build_gameStarted() throws CatanException {
        board.addRoad(new Building(2, ROAD, new Location[]{new Location(-2, 2), new Location(-1, 1)}));
        board.addRoad(new Building(0, ROAD, new Location[]{new Location(-1, 1), new Location(0, 0)}));
        board.addRoad(new Building(1, ROAD, new Location[]{new Location(2, -1), new Location(2, -2)}));
        board.addRoad(new Building(1, ROAD, new Location[]{new Location(-2, 1), new Location(-1, 0)}));

        for (int i = 0; i < buildings.size(); i++) {
            boolean expected = i < 8;
            int expectedSize = expected ? board.getBuildingsSize() + 1 : board.getBuildingsSize();

            Building bld = new Building(buildings.get(i).getOwner(), buildings.get(i).getType(),
                    buildings.get(i).getLocations());

            boolean built;
            try {
                built = board.placeBuilding(bld, false);
            } catch (CatanException ex) {
                built = false;
            }

            assertTrue(expected == built);
            assertTrue(board.getBuildingsSize() == expectedSize);
        }
    }

    @Test
    public void robberTest() throws CatanException {
        Location desert = new Location(0, 0);
        assertFalse(board.getRobber().isValidNewLocation(desert));

        Location water = new Location(0, -3);
        assertFalse(board.getRobber().isValidNewLocation(water));

        Location land = new Location(-2, 0);
        assertTrue(board.getRobber().isValidNewLocation(land));
    }

    @Test
    public void getFreeRoadLoc() throws CatanException {

        Building s = new Building(1, BuildingType.SETTLEMENT, new Location[]{
                new Location(0, 0), new Location(0, 1), new Location(1, 0)
        });

        Player player = new Player(1);

        //place settlement on the board
        board.addSettlement(s);
        player.addBuilding(s);

        for (int i = 0; i < 15; i++) {
            Location[] rLocs = board.getFreeRoadLoc(player, false);

            Building road = new Building(1, BuildingType.ROAD, rLocs);
            assertTrue(board.placeBuilding(road, false));

            player.addBuilding(road);
        }
    }

    private void longestRoadScenario1(Player player2) throws CatanException {
        roadBoard = new Board();
        player2.roads.clear();
        roads = new ArrayList<>();

        roads.add(new Building(2, ROAD, new Location[]{new Location(0, 2), new Location(1, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, 2), new Location(0, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, 0), new Location(0, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, 0), new Location(1, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, 0), new Location(0, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, 0), new Location(2, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, 0), new Location(1, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, 1), new Location(2, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, 1), new Location(2, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, 1), new Location(1, 2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, 0), new Location(2, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, 1), new Location(0, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, 1), new Location(0, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, 2), new Location(0, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -1), new Location(1, 0)}));

        for (int i = 0; i < roads.size(); i++) {
            roadBoard.addRoad(roads.get(i));
            player2.addBuilding(roads.get(i));
        }
    }

    private void longestRoadScenario2(Player player2) throws CatanException {
        roadBoard = new Board();
        player2.roads.clear();
        roads = new ArrayList<>();

        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 2), new Location(-1, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 1), new Location(-1, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 2), new Location(-2, 1)}));

        roads.add(new Building(2, ROAD, new Location[]{new Location(3, -1), new Location(2, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(3, -2), new Location(2, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(3, -2), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(3, -3), new Location(2, -2)}));

        for (int i = 0; i < roads.size(); i++) {
            roadBoard.addRoad(roads.get(i));
            player2.roads.add(roads.get(i));
        }
    }

    private void longestRoadScenario3(Player player2) throws CatanException {
        roadBoard = new Board();
        player2.roads.clear();
        roads = new ArrayList<>();

        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 2), new Location(-1, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 1), new Location(-1, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 2), new Location(-2, 1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 2), new Location(-3, 2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, 1), new Location(-3, 2)}));

        for (int i = 0; i < roads.size(); i++) {
            roadBoard.addRoad(roads.get(i));
            player2.roads.add(roads.get(i));
        }
    }

    private void longestRoadScenario4(Player player2) throws CatanException {
        roadBoard = new Board();
        player2.roads.clear();
        roads = new ArrayList<>();

        Building road = new Building(2, ROAD, new Location[]{new Location(-2, 2), new Location(-1, 1)});

        roadBoard.addRoad(road);
        roads.add(road);
        player2.roads.add(road);

    }

    private void longestRoadScenario5(Player player2) throws CatanException {
        roadBoard = new Board();
        player2.roads.clear();
        roads = new ArrayList<>();

        roads.add(new Building(2, ROAD, new Location[]{new Location(-3, 1), new Location(-2, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-3, 0), new Location(-2, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, -1), new Location(-2, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, -1), new Location(-2, 0)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, -1), new Location(-1, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, -2), new Location(-1, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, -1), new Location(0, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, -2), new Location(0, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, -3), new Location(0, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, -2), new Location(1, -3)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -2), new Location(1, -3)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -2), new Location(2, -3)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -2), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(2, -3), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(3, -3), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(3, -2), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(2, -1), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(2, -1), new Location(3, -2)}));

        for (int i = 0; i < roads.size(); i++) {
            roadBoard.addRoad(roads.get(i));
            player2.roads.add(roads.get(i));
        }

        Collections.shuffle(roadBoard.getRoads());
    }

    //
    private void longestRoadScenario6(Player player2) throws CatanException {
        roadBoard = new Board();
        player2.roads.clear();
        player2.stock = new BuildingStock();
        roads = new ArrayList<>();

        roads.add(new Building(2, ROAD, new Location[]{new Location(-2, -1), new Location(-1, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, -2), new Location(-1, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(-1, -1), new Location(0, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, -1), new Location(-1, -1)}));

        roads.add(new Building(2, ROAD, new Location[]{new Location(0, -1), new Location(0, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, -1), new Location(1, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(0, -2), new Location(1, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -3), new Location(1, -2)}));

        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -2), new Location(2, -3)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -2), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(1, -2), new Location(1, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(2, -2), new Location(1, -1)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(2, -3), new Location(2, -2)}));

        roads.add(new Building(2, ROAD, new Location[]{new Location(3, -2), new Location(2, -2)}));
        roads.add(new Building(2, ROAD, new Location[]{new Location(2, -1), new Location(2, -2)}));

        for (int i = 0; i < roads.size(); i++) {
            roadBoard.addRoad(roads.get(i));
            player2.addBuilding(roads.get(i));
        }
    }

    //region longestRoadTest
    @Test
    public void longestRoadTest() throws CatanException {
        Player player2 = new Player(2);
        longestRoadScenario1(player2);

        assertEquals(14, roadBoard.getLongestRoad(roads.get(roads.size() - 1), player2, false, false).size());

        longestRoadScenario2(player2);

        assertEquals(4, roadBoard.getLongestRoad(roads.get(roads.size() - 1), player2, false, false).size());

        longestRoadScenario3(player2);

        assertEquals(3, roadBoard.getLongestRoad(roads.get(roads.size() - 1), player2, false, false).size());

        longestRoadScenario4(player2);

        assertEquals(1, roadBoard.getLongestRoad(roads.get(roads.size() - 1), player2, false, false).size());

        longestRoadScenario5(player2);

        assertEquals(14, roadBoard.getLongestRoad(roads.get(roads.size() - 1), player2, false, false).size());

        longestRoadScenario6(player2);

        assertEquals(11, roadBoard.getLongestRoad(roads.get(roads.size() - 1), player2, false, false).size());


    }
    //endregion
}