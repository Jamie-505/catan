package de.lmu.settleBattle.catanServer;

import static de.lmu.settleBattle.catanServer.Constants.*;

import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CatanSocketHandler extends TextWebSocketHandler {

  final static Gson gson = new Gson();
  private boolean buildingPhaseActive = false;
  private boolean gameStarted = false;

  private static GameController gameCtrl = new GameController();

  // set to store all live sessions
  private static final Set<WebSocketSession> sessions =
      Collections.synchronizedSet(new HashSet<WebSocketSession>());

  //region afterConnectionEstablished
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    System.out.println(session.getId() + " has opened a connection");
    super.afterConnectionEstablished(session);

    // Add session to session list
    sessions.add(session);
    Player player = new Player(Integer.parseInt(session.getId(), 16));

    try {
      gameCtrl.addPlayer(player);
      // sending session id to the client that just connected
      TextMessage verifyProtocol = CatanMessage.serverProtocol();
      session.sendMessage(verifyProtocol);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      session.sendMessage(new TextMessage("Cannot join this game"));
      e.printStackTrace();
    }
  }
  //endregion

  //region handleTextMessage
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message)
      throws InterruptedException, IOException {

    System.out.println("Message from " + session.getId() + ": " + message.getPayload());

    try {
      String type = JSONUtils.getMessageType(message);

      switch (type) {
        case HANDSHAKE:
          this.performHandshake(session);
          break;
        case PLAYER:
          this.assignPlayerData(session, message);
          break;
        case DICE_RESULT:
          int sum = this.dice(session);
          if (sum == 7)
            extractCardsDueToRobber();
          break;
        case START_GAME:
          this.handleStartGameMessage(session, message);
          break;
        case TOSS_CARDS:
          if (!this.tossRawMaterials(session.getId(), message))
            sendError(session, OPERATION_NOT_PERMITTED);
          else {
            //TODO: send cost message
            sendOK(session);
          }
          break;
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  //endregion

  //region afterConnectionClosed
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    super.afterConnectionClosed(session, status);
    System.out.println("Session " + session.getId() + " has ended");

    //remove player from GameController
    Player player = gameCtrl.getPlayer(session.getId());
    gameCtrl.removePlayer(player);

    // remove the session from sessions list
    sessions.remove(session);

    // TODO: Replace player with KI !
  }
  //endregion

  //region performHandshake
  public void performHandshake(WebSocketSession session) throws InterruptedException, IOException {
    TextMessage idAllocation = CatanMessage.welcomeNewPlayer(Integer.parseInt(session.getId(), 16));
    session.sendMessage(idAllocation);

    //update status of new player
    Player player = gameCtrl.getPlayer(session.getId());
    player.setStatus(Constants.START_GAME);

    for (Player p : gameCtrl.getPlayers())
      sendMessageToAll(CatanMessage.statusUpdate(p));

  }
  //endregion

  //region assignPlayerData
  public void assignPlayerData(WebSocketSession session, TextMessage message)
      throws InterruptedException, IOException {
    JSONObject json = JSONUtils.createJSON(message);
    Player gPlayer = gson.fromJson(json.get(PLAYER).toString(), Player.class);
    Color color = gPlayer.getColor();
    String name = gPlayer.getName();

        boolean validName = gameCtrl.isValidName(name);
        boolean validColor = gameCtrl.isValidColor(color);

    if (validColor && validName) {
      Player player = gameCtrl.getPlayer(session.getId());
      player.setName(name);
      player.setColor(color);

      sendMessageToAll(CatanMessage.statusUpdate(player));
    } else {
      if (!validName)
        session.sendMessage(CatanMessage.error(Constants.NAME_ALREADY_ASSIGNED));

      if (!validColor)
        session.sendMessage(CatanMessage.error(Constants.COLOR_ALREADY_ASSIGNED));
    }
  }
  //endregion

  //region dice
  public int dice(WebSocketSession session) {
    int[] dice = Player.throwDice();
    TextMessage diceMessage = CatanMessage.throwDice(
        gameCtrl.getPlayer(session.getId()).getId(), dice);

    sendMessageToAll(diceMessage);

    return dice[0] + dice[1];
  }
  //endregion

  //region setStatus
  public void setStatus(String id, String status) {
    Player player = gameCtrl.getPlayer(id);
    player.setStatus(status);
  }
  //endregion

  private void initializeBuildingPhase() throws IOException {
    buildingPhaseActive = true;

    gameCtrl.defineTurnOrder();
    Player player = gameCtrl.getNext();
    gameCtrl.setPlayerActive(player.getId(), Constants.BUILD_SETTLEMENT);
    sendStatusUpdate();
  }

  //region handleStartGameMessage
  public void handleStartGameMessage(WebSocketSession session, TextMessage message) throws IOException {
    JSONObject json = (JSONUtils.createJSON(message)).
        getJSONObject(Constants.START_GAME);

    if (json.length() == 0) {
      setStatus(session.getId(), Constants.START_CON);
      boolean ready = gameCtrl.readyToStartGame();

      if (ready) {
        //TODO: sendMessageToAll(jsonUtils.startGame());
        gameStarted = true;

        initializeBuildingPhase();
      }
    }
  }
  //endregion

  //region extractCardsDueToRobber
  public void extractCardsDueToRobber() throws IOException {
    for (Player player : gameCtrl.getPlayers()) {
      if (player.hasToExtractCards()) {
        player.setStatus(Constants.EXTRACT_CARDS_DUE_TO_ROBBER);

        sendStatusUpdate(player);
      }
    }
  }
  //endregion

  //region tossRawMaterials
  public boolean tossRawMaterials(String sessionId, TextMessage message) {
    JSONObject json = JSONUtils.createJSON(message);
    JSONObject rawMaterialJSON = json.getJSONObject(Constants.RAW_MATERIALS);
    RawMaterialOverview overview = gson.fromJson(rawMaterialJSON.toString(), RawMaterialOverview.class);
    Player player = gameCtrl.getPlayer(sessionId);

    try {
      player.decreaseRawMaterials(overview);
    } catch (IllegalArgumentException ex) {
      return false;
    }
    return true;
  }
  //endregion

  //region getSession
  public WebSocketSession getSession(String sessionId) {
    for (WebSocketSession session : sessions) {
      if (session.getId().equals(sessionId))
        return session;
    }

    return null;
  }
  //endregion

  //region sendStatusUpdate
  private void sendStatusUpdate() throws IOException {

    //for every player send status update to everybody
    for (Player player : gameCtrl.getPlayers()) {
      for (WebSocketSession session : sessions) {
        if (Integer.parseInt(session.getId(), 16) == player.getId()) {
          session.sendMessage(CatanMessage.statusUpdate(player));
        } else session.sendMessage(CatanMessage.statusUpdateHidden(player));
      }
    }
  }

  private void sendStatusUpdate(Player player) throws IOException {
    for (WebSocketSession session : sessions) {
      if (Integer.parseInt(session.getId(), 16) == player.getId()) {
        session.sendMessage(CatanMessage.statusUpdate(player));
      } else session.sendMessage(CatanMessage.statusUpdateHidden(player));
    }
  }
  //endregion

  //region sendMessageToAll

  /**
   * Method to send message to all connected clients
   *
   * @param message message to be sent to clients
   */
  private void sendMessageToAll(TextMessage message) {

    // Looping through all the sessions and sending the message individually
    for (WebSocketSession s : sessions) {

      try {
        System.out.println("Sending Message To: " + s.getId() + ", "
            + message);

        s.sendMessage(message);
      } catch (IOException e) {
        System.out.println("error in sending. " + s.getId() + ", "
            + e.getMessage());
        e.printStackTrace();
      }
    }
  }
  //endregion


  private void sendOK(WebSocketSession session) {
    try {
      session.sendMessage(CatanMessage.OK());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendError(WebSocketSession session, String errorMessage) {
    try {
      session.sendMessage(CatanMessage.error(errorMessage));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}