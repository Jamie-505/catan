package de.lmu.settleBattle.catanServer;

import org.json.JSONObject;
import org.junit.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.lmu.settleBattle.catanServer.Constants.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class CatanSocketHandlerTest {
    CatanSocketHandler handler;
    List<TestWebSocketSession> sessions;
    TestWebSocketSession session1;
    TestWebSocketSession session2;
    TestWebSocketSession session3;

    @Before
    public void setUp() throws Exception {
        handler = new CatanSocketHandler();
        session1 = new TestWebSocketSession();
        session2 = new TestWebSocketSession();
        session3 = new TestWebSocketSession();

        sessions = Arrays.asList(session1, session2, session3);

        for (TestWebSocketSession s : sessions) {
            handler.afterConnectionEstablished(s);
            handler.getUtils().performHandshake(s);
        }
    }

    //region startGameMessage
    private TextMessage startGameMessage() {
        JSONObject payload = new JSONObject();
        payload.put(START_GAME, new JSONObject());
        return new TextMessage(payload.toString());
    }
    //endregion

    //region setPlayerData
    private TextMessage setPlayerData(String name, Color color) {
        JSONObject payload = new JSONObject();
        payload.put(PLAYER_NAME, name);
        payload.put(PLAYER_COLOR, color.toString());
        payload = JSONUtils.setJSONType(PLAYER, payload);
        return new TextMessage(payload.toString());
    }
    //endregion

    //region wsMessageToJSON
    private JSONObject wsMessageToJSON(WebSocketMessage<?> message) {
        TextMessage msg = (TextMessage) message;
        return JSONUtils.createJSON(msg);
    }
    //endregion

    //region afterConnectionEstablished
    @Test
    public void afterConnectionEstablished() throws Exception {
        //handler.afterConnectionEstablished(session1);
        JSONObject payload = wsMessageToJSON(session1.getSentMessages().get(0));

        assertTrue(payload.has(HANDSHAKE));

        assertTrue(handler.getUtils().getGameCtrl().getPlayer(
                Integer.parseInt(session1.getId(), 16)) != null);
    }
    //endregion


    @Test
    public void performHandshake() throws Exception {
        JSONObject payload = wsMessageToJSON(session1.getSentMessages().get(1));
        assertTrue(payload.has(Constants.GET_ID));

        JSONObject payload2 = wsMessageToJSON(session1.getSentMessages().get(2));
        assertTrue(payload2.has(Constants.STATUS_UPD));
        assertEquals(Constants.START_GAME, handler.getGameCtrl().getCurrent().getStatus());
    }


    private TestWebSocketSession getCurrentSession() {
        Player player = handler.getGameCtrl().getCurrent();

        for (TestWebSocketSession s : sessions) {
            if (SocketUtils.toInt(s.getId()) == player.getId())
                return s;
        }

        return null;
    }

    //region initializeAssignments
    private void initializeAssignments() throws Exception {
        Player p1 = handler.getGameCtrl().getPlayer(session1.getId());
        p1.setName("Eminem");
        p1.setColor(Color.RED);
        p1.setStatus(START_GAME);

        Player p2 = handler.getGameCtrl().getPlayer(session2.getId());
        p2.setName("Batman");
        p2.setColor(Color.BLUE);
        p2.setStatus(START_GAME);

        Player p3 = handler.getGameCtrl().getPlayer(session3.getId());
        p3.setName("Britney");
        p3.setColor(Color.WHITE);
        p3.setStatus(START_GAME);
    }
    //endregion

    //region initializeStartGame
    private void initializeStartGame() throws Exception {
        initializeAssignments();
        Player p1 = handler.getGameCtrl().getPlayer(session1.getId());
        Player p2 = handler.getGameCtrl().getPlayer(session2.getId());
        Player p3 = handler.getGameCtrl().getPlayer(session3.getId());

        p1.setStatus(WAIT_FOR_GAME_START);
        p2.setStatus(WAIT_FOR_GAME_START);
        p3.setStatus(WAIT_FOR_GAME_START);

        handler.getGameCtrl().startGame();
    }

    //endregion

    //region assignPlayerData
    @Test
    public void assignPlayerData() throws Exception {
        handler.getUtils().assignPlayerData(session1, setPlayerData("Eminem", Color.RED));

        Player p1 = CatanMessage.statusUpdateToPlayer(session1.getLast());
        assertTrue(p1.getName().equals("Eminem"));
        assertTrue(p1.getStatus().equals(START_GAME));
        assertTrue(p1.getColor().equals(Color.RED));

        //assign same data to cause error message
        handler.getUtils().assignPlayerData(session2, setPlayerData("Eminem", Color.RED));

        Player p2 = handler.getGameCtrl().getPlayer(session2.getId());
        assertTrue(p2.getColor() == null);
        assertTrue(p2.getName() == null);
        JSONObject error2 = JSONUtils.createJSON(session2.getLast());
        JSONObject error1 = JSONUtils.createJSON(session2.get(session2.getLastIndex() - 1));
        assertTrue(error2.has(ERROR));
        assertTrue(error1.has(ERROR));
        assertTrue(error1.getJSONObject(ERROR).get(MESSAGE).equals(NAME_ALREADY_ASSIGNED));
        assertTrue(error2.getJSONObject(ERROR).get(MESSAGE).equals(COLOR_ALREADY_ASSIGNED));

        handler.getUtils().assignPlayerData(session2, setPlayerData("Batman", Color.BLUE));

        Player p22 = CatanMessage.statusUpdateToPlayer(session2.getLast());
        assertTrue(p22.getStatus().equals(START_GAME));
        assertTrue(p22.getName().equals("Batman"));
        assertTrue(p22.getColor().equals(Color.BLUE));
    }
    //endregion

    //region handleStartGameMessage
    @Test
    public void handleStartGameMessage() throws Exception {
        initializeAssignments();
        boolean started = handler.getUtils().handleStartGameMessage(session1, startGameMessage());
        assertTrue(!started);
        Player p1 = CatanMessage.statusUpdateToPlayer(session1.getLast());
        assertTrue(p1.getStatus().equals(WAIT_FOR_GAME_START));

        started = handler.getUtils().handleStartGameMessage(session2, startGameMessage());
        assertTrue(!started);
        Player p2 = CatanMessage.statusUpdateToPlayer(session2.getLast());
        assertTrue(p2.getStatus().equals(WAIT_FOR_GAME_START));

        started = handler.getUtils().handleStartGameMessage(session3, startGameMessage());
        assertTrue(started);
        Player p3 = CatanMessage.statusUpdateToPlayer(session3.getLast());
        assertTrue(p3.getStatus().equals(WAIT_FOR_GAME_START));

        handler.sendMessageToAll(CatanMessage.startGame(handler.getGameCtrl().getBoard()));
        Board board = CatanMessage.extractBoard(session1.getLast());
        assertTrue(board.getBuildingsSize() == 0);

        handler.getUtils().getGameCtrl().startGame();

        p3 = CatanMessage.statusUpdateToPlayer(session3.getLast());
        p2 = CatanMessage.statusUpdateToPlayer(session3.get(session3.getLastIndex() - 1));
        p1 = CatanMessage.statusUpdateToPlayer(session3.get(session3.getLastIndex() - 2));

        assertTrue(
                (p3.getStatus().equals(BUILD_SETTLEMENT) && p2.getStatus().equals(WAIT) && p1.getStatus().equals(WAIT)) ||
                        (p3.getStatus().equals(WAIT) && p2.getStatus().equals(BUILD_SETTLEMENT) && p1.getStatus().equals(WAIT)) ||
                        (p3.getStatus().equals(WAIT) && p2.getStatus().equals(WAIT) && p1.getStatus().equals(BUILD_SETTLEMENT)));
    }
    //endregion

    //region build_BuildingPhase
    @Test
    public void build_BuildingPhase() throws Exception {
        initializeStartGame();

        buildBuildingWithValidLocation(BuildingType.SETTLEMENT,
                new Location[]{new Location(-2, 2), new Location(-1, 2), new Location(-1, 1)});

        buildBuildingWithValidLocation(
                BuildingType.ROAD, new Location[]{new Location(-1, 2), new Location(-1, 1)});

        buildBuildingWithInvalidLocation(BuildingType.SETTLEMENT,
                new Location[]{new Location(-2, 2), new Location(-2, 1), new Location(-1, 1)});

        buildBuildingWithValidLocation(BuildingType.SETTLEMENT,
                new Location[]{new Location(1, 0), new Location(2, 0), new Location(2, -1)});
    }
    //endregion

    //region buildBuildingWithInvalidLocation
    private void buildBuildingWithInvalidLocation(BuildingType type, Location[] locs)
            throws IOException {
        TestWebSocketSession session = getCurrentSession();
        String id = session.getId();

        Building building = new Building(
                SocketUtils.toInt(session.getId()), type, locs);

        int fromIndex = session.getLastIndex();
        //check if sLocs was built
        Board board = handler.getGameCtrl().getBoard();
        int builtBuildingCnt = board.getBuildingsSize();

        boolean sBuilt = handler.getUtils().build(SocketUtils.toInt(session.getId()), CatanMessage.sendBuildMessage(building));
        assertTrue(board.getBuildingsSize() == builtBuildingCnt);
        assertTrue(!sBuilt);

        String expectedStatus = type.equals(BuildingType.SETTLEMENT) ? BUILD_STREET : BUILD_SETTLEMENT;
        validateBuildData(fromIndex, session.getLastIndex(), session, locs, true, expectedStatus);

        assertTrue(id.equals(getCurrentSession().getId()));
    }
    //endregion

    //region buildBuildingWithValidLocation
    private void buildBuildingWithValidLocation(BuildingType type, Location[] locs)
            throws IOException {
        TestWebSocketSession session = getCurrentSession();

        Building building = new Building(
                handler.getUtils().toInt(session.getId()), type, locs);

        int fromIndex = session.getLastIndex();
        //check if sLocs was built
        Board board = handler.getGameCtrl().getBoard();
        int builtBuildingCnt = board.getBuildingsSize();

        boolean sBuilt = handler.getUtils().build(SocketUtils.toInt(session.getId()), CatanMessage.sendBuildMessage(building));
        assertTrue(board.getBuildingsSize() == builtBuildingCnt+1);
        assertTrue(sBuilt);

        String expectedStatus = type.equals(BuildingType.SETTLEMENT) ? BUILD_STREET : BUILD_SETTLEMENT;
        validateBuildData(fromIndex, session.getLastIndex(), session, locs, true, expectedStatus);

    }
    //endregion

    //region validateBuildData
    private void validateBuildData(int oldLastIndex, int newLastIndex, TestWebSocketSession session,
                                   Location[] builtLocs, boolean builtExpected, String activeStatusExpected) {
        List<Object> objects = newMessagesToObject(oldLastIndex, newLastIndex, session);
        List<Player> changedPlayers = new ArrayList<>();

        for (Object o : objects) {
            if (o instanceof Building)
                assertTrue(((Building) o).isBuiltAroundHere(builtLocs, true) == builtExpected);

            else if (o instanceof Player) {
                changedPlayers.add((Player) o);
            }
        }

        int activeStatusCount = 0;
        int waitCount = 0;
        int otherCnt = 0;

        //if there were no player messages, nothing is to be validated
        if (changedPlayers.size() == 0) return;

        for (Player p : changedPlayers) {
            if (p.getStatus().equals(activeStatusExpected))
                activeStatusCount++;
            else if (p.getStatus().equals(WAIT))
                waitCount++;
            else {
                System.out.printf("Player has unexpected status %s", p.getStatus());
                otherCnt++;
            }
        }

        assertTrue(activeStatusCount == 1);
        assertTrue(otherCnt == 0);
        assertTrue(waitCount < handler.getGameCtrl().getPlayers().size());
    }
    //endregion

    //region newMessagesToObject
    private ArrayList<Object> newMessagesToObject(int fromIndex, int newLastIndex, TestWebSocketSession session) {
        ArrayList<Object> objects = new ArrayList<>();

        for (int i = fromIndex+1; i <= newLastIndex; i++) {
            objects.add(CatanMessage.messageToObject(session.get(i)));
        }

        return objects;
    }
    //endregion

    @After
    public void tearDown() throws Exception {
        for (TestWebSocketSession s : sessions) {
            s.close();
            handler.afterConnectionClosed(s, s.getCloseStatus());
        }
    }

}