package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.KEY_SESSION_ID;
import static de.lmu.settlebattle.catanclient.utils.Constants.KEY_SHARED_PREF;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_COLOR;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_ID;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.utils.Message.Player;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class Storage {

  private Gson gson = new Gson();

  private final String TAG = this.getClass().getSimpleName();

  private SharedPreferences sharedPrefs;


  public Storage(Context context) {
    this.sharedPrefs = context.getSharedPreferences(KEY_SHARED_PREF, Context.MODE_PRIVATE);
  }

  public void storePlayer(Player player) {
    try {
      JSONObject pJSON = new JSONObject(gson.toJson(player));
      Editor editor = sharedPrefs.edit();
      Iterator<String> keys = pJSON.keys();
      while (keys.hasNext()){
        String key = keys.next();
        if (key.equals(PLAYER_ID)){
          editor.putInt(KEY_SESSION_ID, pJSON.getInt(PLAYER_ID));
        } else {
          editor.putString(key, pJSON.getString(key));
        }
      }
      editor.apply();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void storeSessionId(int sessionId) {
    Editor editor = sharedPrefs.edit();
    editor.putInt(KEY_SESSION_ID, sessionId);
    editor.apply();
  }

  public void storeNameColorString(String name, String color) {
    Editor editor = sharedPrefs.edit();
    editor.putString(PLAYER_NAME, name);
    editor.putString(PLAYER_COLOR, color);
    editor.apply();
  }



  public int getSessionId() { return sharedPrefs.getInt(KEY_SESSION_ID, 0);
  }

}
