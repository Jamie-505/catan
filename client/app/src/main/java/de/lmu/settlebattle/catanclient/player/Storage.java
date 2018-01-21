package de.lmu.settlebattle.catanclient.player;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;

public class Storage {

  private final String TAG = this.getClass().getSimpleName();

  private static int ownId = -1;
  private static HashMap<Integer, Player> players = new HashMap<>();
  private static Gson gson = new Gson();

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

  public static Player getPlayer(int id) {
    return players.get(id);
  }

  public static String getAllPlayersAsJson() {
    // +1 because own player is also added not just opponents
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
    // +1 because own player is also added not just opponents
    Player[] allPlayers = new Player[players.size()];
    // make sure own player is on 1st index
    allPlayers[0] = players.get(ownId);
    int index = 0;
    for (Integer id : opponentIds) {
      index++;
      allPlayers[index] = getPlayer(id);
    }
    return allPlayers;
  }
}
