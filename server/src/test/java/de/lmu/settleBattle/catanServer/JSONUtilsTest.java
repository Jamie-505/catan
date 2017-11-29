package de.lmu.settleBattle.catanServer;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSONUtilsTest {
    private static JSONUtils jsonUtils;
    private static Player player1;
    private static Player player2;
    private static Building building;
    private static Field field;
    private static Haven haven;
    private static Robber robber;
    private static RawMaterialOverview rmOverview;
    private static DevelopmentCardOverview dcOverview;

    //region initialize
    @BeforeClass
    public static void initialize()
    {
        jsonUtils = new JSONUtils();
        player1 = new Player(0);
        player1.setColor(Color.WHITE);
        player1.setName("Lucy");
        player1.setStatus(Constants.START_GAME);

        player2 = new Player(42);
        player2.setStatus(Constants.START_GAME);

        building = new Building(BuildingType.CITY, 42);
        Location l1 = new Location(1,0);
        Location l2 = new Location(1,1);
        Location l3 = new Location(2,0);
        building.build(new Location[] {l1, l2, l3});

        field = new Field(l1, RawMaterialType.WOOL, 3);
        haven = new Haven(new Location[] {l1,l2}, RawMaterialType.CLAY);
        robber = new Robber();
        rmOverview = new RawMaterialOverview(0,0,3,4,2);
        dcOverview = new DevelopmentCardOverview(0,1,2,3,4);
    }
    //endregion

    @Test
    public void welcomeNewPlayerShouldBeAnsweredRight() throws Exception {
        assertEquals("{\"Willkommen\":{\"id\":42}}",
                jsonUtils.welcomeNewPlayer(42));
    }

    @Test
    public void endGameJSONMessageShouldBeRight() throws Exception {
        assertEquals("{\"Spiel beendet\":{\"Nachricht\":\"Spieler Lucy hat das Spiel gewonnen.\",\"Sieger\":0}}",
                jsonUtils.endGame(player1));
    }

    @Test
    public void errorMessageShouldBeAnsweredRight() throws Exception {
        assertEquals("{\"Serverantwort\":\"Die gewählten Daten sind bereits vergeben.\"}",
                jsonUtils.errorMessage(Constants.PLAYER_DATA_INVALID));
    }

    @Test
    public void diceRequestShouldBeAnsweredRight() throws Exception {
        assertEquals("{\"Würfelwurf\":{\"Spieler\":42,\"Wurf\":[1,1]}}",
                jsonUtils.throwDice(42, new int[]{1,1}).replace(" ",""));
    }

    @Test
    public void statusUpdateShouldBeBuiltRight() throws Exception {
        assertEquals("{\"Statusupdate\":{\"Spieler\":{\"Status\":\"Spielstarten\",\"Farbe\":\"Weiß\",\"id\":0,\"Name\":\"Lucy\"}}}",
                jsonUtils.statusUpdate(player1).replace(" ",""));

        assertEquals("{\"Statusupdate\":\"Karten wegen Räuber abgeben\"}",
                jsonUtils.statusUpdate(Constants.EXTRACT_CARDS_DUE_TO_ROBBER));

        assertEquals("{\"Statusupdate\":{\"Spieler\":{\"Status\":\"Spiel starten\",\"id\":42}}}",
                jsonUtils.statusUpdate(player2));
    }

    @Test
    public void buildingJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Eigentümer\":42,\"Typ\":\"Stadt\"," +
                        "\"Ort\":[{\"x\":1,\"y\":0},{\"x\":1,\"y\":1},{\"x\":2,\"y\":0}]}",
                building.toJSONString());
    }

    @Test
    public void fieldJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Ort\":{\"x\":1,\"y\":0},\"Typ\":\"Wolle\",\"Zahl\":3}",
                field.toJSONString());
    }

    @Test
    public void havenJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Ort\":[{\"x\":1,\"y\":0},{\"x\":1,\"y\":1}],\"Typ\":\"Lehm Hafen\"}",
                haven.toJSONString());
    }

    @Test
    public void robberJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Ort\":{\"x\":0,\"y\":0}}",
                robber.toJSONString());
    }

    @Test
    public void rawMaterialOverviewJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Holz\":3,\"Lehm\":0,\"Wolle\":4,\"Getreide\":2,\"Erz\":0}",
                rmOverview.toJSONString());

        assertEquals("{\"Unbekannt\":9}",
                rmOverview.toJSONString_Unknown());
    }

    @Test
    public void developmentCardOverviewJSONObjectShouldBeRight() throws Exception {
        assertEquals("{\"Ritter\":0,\"Straßenbau\":1,\"Monopol\":2,\"Erfindung\":3,\"Siegpunkt\":4}",
                dcOverview.toJSONString());


        assertEquals("{\"Unbekannt\":10}",
                dcOverview.toJSONString_Unknown());
    }

   /* @Test
    public void startGameShouldBeBuiltRight() throws Exception {
        assertEquals("{\"Spielgestartet\":{\"Karte\":{\n" +
                        "\"Felder\" : [ ], // Array von Feldern " +
                        "\"Gebäude\" : [ ], // Array von Gebäuden " +
                        "\"Häfen\" : [ ], // Array von Häfen " +
                        "\"Räuber\" : { // Feldposition des Räubers \"x\" : 0, \"y\" : 0 }\n" +
                        "}}}",
                jsonUtils.startGame(new RawMaterialOverview(1,1,0,0,0)).replace(" ",""));
    }
*/
    //TODO tests for all messages that server sends to the clients (UML sequence diagram)

}