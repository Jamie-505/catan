package de.lmu.settlebattle.catanclient.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.json.JSONException;
import org.json.JSONObject;

public class Storage {

  private final String DEBUG_TAG = this.getClass().getSimpleName();

  private SharedPreferences sharedPrefs;

  private static final String KEY_SHARED_PREF = "CATAN_APP";
  private static final String KEY_SESSION_ID = "sessionId";

  public Storage(Context context) {
    this.sharedPrefs = context.getSharedPreferences(KEY_SHARED_PREF, Context.MODE_PRIVATE);
  }

  public void storeSessionId(String sessionId) {
    Editor editor = sharedPrefs.edit();
    editor.putString(KEY_SESSION_ID, sessionId);
    editor.apply();
  }

  public String getSessionId() {
    return sharedPrefs.getString(KEY_SESSION_ID, null);
  }

}
