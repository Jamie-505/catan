package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.KEY_SESSION_ID;
import static de.lmu.settlebattle.catanclient.utils.Constants.KEY_SHARED_PREF;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Storage {

  private final String TAG = this.getClass().getSimpleName();

  private SharedPreferences sharedPrefs;


  public Storage(Context context) {
    this.sharedPrefs = context.getSharedPreferences(KEY_SHARED_PREF, Context.MODE_PRIVATE);
  }

  public void storeSessionId(int sessionId) {
    Editor editor = sharedPrefs.edit();
    editor.putInt(KEY_SESSION_ID, sessionId);
    editor.apply();
  }

  public int getSessionId() { return sharedPrefs.getInt(KEY_SESSION_ID, 0);
  }

}
