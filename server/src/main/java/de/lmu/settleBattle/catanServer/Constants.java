package de.lmu.settleBattle.catanServer;


public class Constants {
    //Versions Client/Server
    public static final String VERSION_SERVER = "CatanBackendServer 0.1 (sepgroup03)";
    public static final String VERSION_CLIENT = "AndroidClient 0.1 (sepgroup03)";
    public static final String VERSION = "Version";
    public static final String PROTOCOL = "Protokoll";

    //Status Constants
    public final static String START_GAME = "Spiel starten";
    public final static String WAIT_FOR_GAME_START = "Wartet auf Spielbeginn";
    public final static String BUILD_SETTLEMENT = "Dorf bauen";
    public final static String BUILD_STREET = "Straße bauen";
    public final static String BUILD_TOWN = "Stadt bauen";
    public final static String DICE = "Würfeln";
    public final static String EXTRACT_CARDS_DUE_TO_ROBBER = "Karten wegen Räuber abgeben";
    public final static String MOVE_ROBBER = "Räuber versetzen";
    public final static String TRADE_OR_BUILD = "Handeln oder bauen";
    public final static String WAIT = "Warten";

    //Message Types
    public final static String BUILD = "Bauen";
    public final static String CARD_BUY = "Entwicklungskarte kaufen";
    public final static String CARD_KNIGHT = "Ritter ausspielen";
    public final static String CARD_RD_CON = "Straßenbaukarte ausspielen";
    public final static String CARD_SOLD = "Entwicklungskarte gekauft";
    public final static String CHAT_IN = "Chatnachricht";
    public final static String CHAT_OUT = "Chatnachricht senden";
    public final static String COSTS = "Kosten";
    public final static String DICE_RESULT = "Würfelwurf";
    public final static String DICE_THROW = "Wurf";
    public final static String END_TURN = "Zug beenden";
    public final static String ERROR = "Fehler";
    public final static String GAME_OVER = "Spiel beendet";
    public final static String GET_ID = "Willkommen";
    public final static String HANDSHAKE = "Hallo";
    public final static String HARVEST = "Ertrag";
    public final static String LARGEST_ARMY = "Größte Rittermacht";
    public final static String LONGEST_RD = "Längste Handelsstrasse";
    public final static String NEW_BUILDING ="Bauvorgang";
    public final static String ROBBER_AT = "Räuber versetzt";
    public final static String ROBBER_TO ="Räuber versetzten";
    public final static String ROLL_DICE = "Würfeln";
    public final static String SEA_TRADE = "Seehandel";
    public final static String SERVER_RES = "Serverantwort";
    public final static String START_CON = "Spiel gestartet";
    public final static String STATUS_UPD = "Statusupdate";
    public final static String TOSS_CARDS = "Karten abgeben";
    public final static String TRD_ABORTED = "Handelsangebot abgebrochen";
    public final static String TRD_ACC = "Handelsangebot angenommen";
    public final static String TRD_FIN = "Handel ausgefuehrt";
    public final static String TRD_OFFER = "Handelsangebot";
    public final static String TRD_REJ = "Handel abbrechen";
    public final static String TRD_REQ = "Handel anbieten";
    public final static String TRD_RES = "Handel annehmen";
    public final static String TRD_SEL = "Handel abschliessen";
    public final static String ACCEPT = "Annehmen";
    public final static String FELLOW_PLAYER = "Mitspieler";
    public final static String ARMY = "Rittermacht";
    public final static String DEV_CARDS = "Entwicklungskarten";

    //Message Objects
    public final static String CARD = "Karte";
    public final static String OWNER = "Eigentümer";
    public final static String TYPE = "Typ";
    public final static String PLACE = "Ort";
    public final static String ROAD = "Straße";
    public final static String SETTLEMENT = "Siedlung";
    public final static String CITY = "Stadt";
    public final static String NUMBER = "Zahl";
    public final static String HAVEN = "Hafen";
    public final static String FIELDS = "Felder";
    public final static String BUILDINGS = "Gebäude";
    public final static String ROBBER = "Räuber";
    public final static String UNKNOWN = "Unbekannt";
    public final static String KNIGHT = "Ritter";
    public final static String INVENTION = "Erfindung";
    public final static String MONOPOLE = "Monopol";
    public final static String ROAD_CONSTRUCTION = "Straßenbau";
    public final static String VICTORY_PTS = "Siegpunkte";
    public final static String VICTORY_PT = "Siegpunkt";
    public final static String WEAT = "Getreide";
    public final static String ORE = "Erz";
    public final static String CLAY = "Lehm";
    public final static String WOOD = "Holz";
    public final static String WOOL = "Wolle";
    public final static String WATER = "Wasser";
    public final static String NOTIFICATION = "Nachricht";
    public final static String WINNER = "Sieger";
    public final static String RAW_MATERIALS = "Rohstoffe";
    public final static String RAW_MATERIAL = "Rohstoff";
    public final static String DESTINATION = "Ziel";
    public final static String TRADE_ID = "Handel id";
    public final static String OFFER = "Angebot";
    public final static String REQUEST = "Nachfrage";
    public final static String MESSAGE = "Meldung";
    public final static String OK = "OK";
    public final static String ORANGE = "Orange";
    public final static String BLUE = "Blau";
    public final static String RED = "Rot";
    public final static String WHITE = "Weis";

    //Player Constants
    public final static String PLAYER = "Spieler";
    public final static String PLAYER_ID = "id";
    public final static String PLAYER_COLOR = "Farbe";
    public final static String PLAYER_STATE = "Status";
    public final static String PLAYER_NAME = "Name";
    public final static String PLAYER_ID_WON_GAME = "Spieler %1$s hat das Spiel gewonnen.";
    public final static String NAME_ALREADY_ASSIGNED = "Name bereits vergeben.";
    public final static String COLOR_ALREADY_ASSIGNED = "Farbe bereits vergeben.";

    //Development cards description
    public final static String KNIGHT_DESC = "";
    public final static String ROAD_CONSTRUCTION_DESC = "Wer diese Karte ausspielt, darf ohne Rohstoff Kosten 2 neue Straßen auf den Spielplan legen. Hierbei  müssen die üblichen Regeln für den Bau von Straßen beachtet werden.";
    public final static String INVENTION_DESC = "Wer diese Karte ausspielt, darf sich 2 beliebige Rohstoffkarten von den Vorratsstapeln nehmen. Hat der Spieler die Bauphase noch vor sich, darf er diese Rohstoffkarte(n) zum Bauen verwenden";
    public final static String MONOPOLE_DESC = "Wer diese Karte ausspielt, wählt einen Rohstoff  aus. Alle Mitspieler müssen ihm alle Rohstoffkarten geben,  die sie von dieser Sorte auf der Hand halten. Wer keine  solche Rohstoffkarte besitzt, muss auch nichts abgeben.";
    public final static String VICTORY_POINT_DESC = "";
}
