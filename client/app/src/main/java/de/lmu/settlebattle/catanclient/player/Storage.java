package de.lmu.settlebattle.catanclient.player;

import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.chat.ChatMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {

  private final String TAG = this.getClass().getSimpleName();

  private static int ownId = -1;
  private static HashMap<Integer, Player> players = new HashMap<>();
  private static Gson gson = new Gson();
  private static List<ChatMessage> chatMessages = new ArrayList<>();
  private static int[] turnOrder;

  // needs to be static so all instances of storage can access it
  private static ArrayList<Integer> opponentIds = new ArrayList<>();

  public static boolean isItMe(Player p) {
    return p.id == ownId;
  }

  public static boolean isItMe(int id) {
    return id == ownId;
  }

  public static void storeSessionId(int sessionId) {
    ownId = sessionId;
  }

  public static int getSessionId() { return ownId; }

  public static Player getSelf() {
    return players.get(ownId);
  }

  public static void storePlayer(Player player) {
    if (!opponentIds.contains(player.id) && player.id != ownId) {
      opponentIds.add(player.id);
    }
    players.put(player.id, player);
  }

  public static void addChatMsg(ChatMessage msg) {
    chatMessages.add(msg);
  }

  public static List<ChatMessage> getChatMessages() {
    return chatMessages;
  }

  public static Player getPlayer(int id) {
    return players.get(id);
  }

  public static String getAllPlayersAsJson() {
    Player[] allPlayers = new Player[players.size()];
    allPlayers[0] = players.get(ownId);
    int index = 0;
    if (opponentIds.size() > 0) {
      for (Integer id : opponentIds) {
        index++;
        allPlayers[index] = getPlayer(id);
      }
    }
    return gson.toJson(allPlayers);
  }

  public static Player[] getAllPlayers() {
    Player[] allPlayers = new Player[players.size()];
    int index = 0;
    for (int id : turnOrder) {
      allPlayers[index++] = getPlayer(id);
    }
    return allPlayers;
  }

  public static void saveTurnOrder(int[] order) {
    turnOrder = order;
  }
}
