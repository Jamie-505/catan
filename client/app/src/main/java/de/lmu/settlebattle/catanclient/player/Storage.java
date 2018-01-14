package de.lmu.settlebattle.catanclient.player;

import static de.lmu.settlebattle.catanclient.utils.Constants.KEY_PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.KEY_SESSION_ID;
import static de.lmu.settlebattle.catanclient.utils.Constants.KEY_SHARED_PREF;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.Gson;
import java.util.ArrayList;

public class Storage {

  private final String TAG = this.getClass().getSimpleName();

  private SharedPreferences sharedPrefs;
  private Gson gson = new Gson();

  // needs to be static so all instances of storage can access it
  private static ArrayList<Integer> opponentIds = new ArrayList<>();


  public Storage(Context context) {
    this.sharedPrefs = context.getSharedPreferences(KEY_SHARED_PREF, Context.MODE_PRIVATE);
  }

  public void storeSessionId(int sessionId) {
    Editor editor = sharedPrefs.edit();
    editor.putInt(KEY_SESSION_ID, sessionId);
    editor.apply();
  }

  public int getSessionId() { return sharedPrefs.getInt(KEY_SESSION_ID, 0); }

  public void storePlayer(Player player) {
    storeSessionId(player.id);
    String pJSON = gson.toJson(player);
    Editor editor = sharedPrefs.edit();
    editor.putString(KEY_PLAYER, pJSON);
    editor.apply();
  }

  public Player getPlayer() {
    String playerString = sharedPrefs.getString(KEY_PLAYER, null);
    return gson.fromJson(playerString, Player.class);
  }

  public void storeOpponent(Player opponent) {
    Integer id = opponent.id;
    if (!opponentIds.contains(id)) {
      opponentIds.add(id);
    }
    String oJSON = gson.toJson(opponent);
    Editor editor = sharedPrefs.edit();
    // maybe the key needs to be something like player2
    // then I need a map or array to connect the id to each player
    editor.putString(String.valueOf(id), oJSON);
    editor.apply();
  }

  public Player getOpponent(int id) {
    String opponentString = sharedPrefs.getString(String.valueOf(id), null);
    return gson.fromJson(opponentString, Player.class);
  }

  public String getAllPlayersAsJson() {
    // +1 because own player is also added not just opponents
    Player[] allPlayers = new Player[opponentIds.size()+1];
    allPlayers[0] = gson.fromJson(sharedPrefs.getString(KEY_PLAYER, null), Player.class);
    int index = 0;
    for (Integer id : opponentIds) {
      index++;
      allPlayers[index] = getOpponent(id);
    }
    return gson.toJson(allPlayers);
  }

  public Player[] getAllPlayers() {
    // +1 because own player is also added not just opponents
    Player[] allPlayers = new Player[opponentIds.size()+1];
    allPlayers[0] = gson.fromJson(sharedPrefs.getString(KEY_PLAYER, null), Player.class);
    int index = 0;
    for (Integer id : opponentIds) {
      index++;
      allPlayers[index] = getOpponent(id);
    }
    return allPlayers;
  }

  public void clear() {
    sharedPrefs.edit().clear().apply();
  }
}
