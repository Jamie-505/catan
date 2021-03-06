package de.lmu.settlebattle.catanclient.utils;

public final class Constants {

  // Message Type Constants
  public static final String ADD_KI = "addKI";
  public static final String BUILD = "Bauen";
  public static final String CARD_BUY = "Entwicklungskarte kaufen";
  public static final String CARD_KNIGHT = "Ritter ausspielen";
  public static final String CARD_RD_CON = "Strassenbaukarte ausspielen";
  public static final String CHAT_IN = "Chatnachricht";
  public static final String CHAT_OUT = "Chatnachricht senden";
  public static final String COSTS = "Kosten";
  public static final String DICE_RESULT = "Würfelwurf";
  public static final String END_TURN = "Zug beenden";
  public static final String ERROR = "Fehler";
  public static final String GAME_OVER = "Spiel beendet";
  public static final String GAME_START = "Spiel gestartet";
  public static final String GAME_READY = "Spiel starten";
  public static final String GAME_WAIT = "Wartet auf Spielbeginn";
  public static final String GET_ID = "Willkommen";
  public static final String HANDSHAKE = "Hallo";
  public static final String HARVEST = "Ertrag";
  public static final String LONGEST_RD = "Längste Handelsstrasse";
  public static final String NEW_CONSTRUCT ="Bauvorgang";
  public static final String OPPONENT ="Mitspieler";
  public static final String PLAYER = "Spieler";
  public static final String ROBBER = "Räuber";
  public static final String ROBBER_AT = "Räuber versetzt";
  public static final String ROBBER_TO ="Räuber versetzen";
  public static final String ROLL_DICE = "Würfeln";
  public static final String SEA_TRADE = "Seehandel";
  public static final String SERVER_RES = "Serverantwort";
  public static final String STATUS_UPD = "Statusupdate";
  public static final String TOSS_CARDS = "Karten abgeben";
  public static final String TOSS_CARDS_REQ = "Karten wegen Räuber abgeben";
  public static final String TRADE = "trade";
  public static final String TRD_ABORTED = "Handelsangebot abgebrochen";
  public static final String TRD_ACC = "Handelsangebot angenommen";
  public static final String TRD_FIN = "Handel ausgefuehrt";
  public static final String TRD_ID = "Handel id";
  public static final String TRD_OFFER = "Handelsangebot";
  public static final String TRD_REJ = "Handel abbrechen";
  public static final String TRD_REQ = "Handel anbieten";
  public static final String TRD_RES = "Handel annehmen";
  public static final String TRD_SEL = "Handel abschliessen";
  public static final String TRD_SENT = "Handel wird angeboten";
  public static final String VICTORY_PTS = "Siegpunkte";

  // Broadcast
  public static final String CONNECTION_LOST = "lost the connection";
  public static final String DISPLAY_ERROR = "displayMessage";
	public static final String ERROR_MSG = "errorMsg";
  public static final String NEXT_ACTIVITY = "nextActivity";
  public static final String OK = "ok";
  public static final String PLAYER_UPDATE = "playerUpdate";
  public static final String PLAYER_WAIT = "playerWait";
	public static final String PROTOCOL_SUPPORTED = "protocolMatch";
  public static final String NO_CONNECTION = "no connection";

  // Dev Cards
  public final static String NEW = "inaktiv (neu)";
  public static final String INVENTION = "Erfindung";
  public final static String KNIGHT = "Ritter";
  public final static String MONOPOLE = "Monopol";
  public final static String PLAYED = "(ausgespielt)";
  public final static String RD_CONSTR = "Strassenbau";
  public final static String VICTORY_PT = "Siegpunkt";

  // Error Handling
  public static final String MESSAGE = "Meldung";
  public static final String PROTOCOL_MISSMATCH = "Protocol Version Unsupported";

  // Message Objects
  public final static String ACCEPT = "Annehmen";
  public final static String BOARD = "Karte";
  public final static String CITY = "Stadt";
  public final static String CLAY = "Lehm";
  public final static String DICE_THROW = "Wurf";
  public final static String FIELDS = "Felder";
  public final static String CONTENT = "Nachricht";
  public final static String NUMBER = "Zahl";
  public final static String OFFER = "Angebot";
  public final static String ORE = "Erz";
  public final static String OWNER = "Owner";
  public final static String LOCATION = "Ort";
  public final static String PLAYER_LEFT = "Spieler ist weg";
  public final static String PROTOCOL = "Protokoll";
  public final static String RAW_MATERIAL = "Rohstoff";
  public final static String RAW_MATERIALS = "Rohstoffe";
  public final static String REQUEST = "Nachfrage";
  public final static String SENDER = "Absender";
  public final static String SETTLEMENT = "Siedlung";
  public final static String STREET = "Strasse";
  public final static String TARGET = "Ziel";
  public final static String TURN_ORDER = "Reihenfolge";
  public final static String TYPE = "Typ";
  public final static String UNKNOWN = "Unbekannt";
  public final static String VIEW_ID = "viewID";
  public final static String WHEAT = "Getreide";
  public final static String WINNER = "Sieger";
  public final static String WOOD = "Holz";
  public final static String WOOL = "Wolle";

  // Player Constants
  public final static String ARMY = "Rittermacht";
  public final static String BIGGEST_ARMY = "Grösste Rittermacht";
  public final static String BLUE = "Blau";
  public final static String BUILD_CITY = "Stadt bauen";
  public final static String BUILD_SETTLEMENT = "Siedlung bauen";
  public final static String BUILD_STREET = "Strasse bauen";
  public final static String BUILD_TRADE = "Handeln oder bauen";
  public final static String BUILD_VILLAGE = "Dorf bauen";
  public final static String DEV_CARD = "Entwicklungskarte";
  public final static String DEV_CARDS = "Entwicklungskarten";
  public final static String DEV_CARD_BOUGHT = "Entwicklungskarte gekauft";
  public final static String DEV_CARD_PLAYED = "Entwicklungskarte ausgespielt";
  public final static String ORANGE = "Orange";
  public final static String PLAYER_COLOR = "Farbe";
  public final static String PLAYER_ID = "id";
  public final static String PLAYER_NAME = "Name";
  public final static String PLAYER_STATE = "Status";
  public final static String RD_CON1 = "Strassenbau1";
  public final static String RD_CON2 = "Strassenbau2";
  public final static String RED = "Rot";
  public final static String STATUS_WAIT = "Warten";
  public final static String WHITE = "Weis";

  // Service
  public static final String ALL_PLAYERS = "allPlayers";
  public static final String TO_SERVER = "Send To Server";

  // Version Constants
  public static final String VERSION_PROTOCOL = "1.0";
  public static final String VERSION_CLIENT = "AndroidClient 0.1 (sepgroup03)";

}
