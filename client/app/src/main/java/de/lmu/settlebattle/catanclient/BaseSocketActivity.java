package de.lmu.settlebattle.catanclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import de.lmu.settlebattle.catanclient.network.WebSocketService;

public abstract class BaseSocketActivity extends AppCompatActivity {

  // disable back button so you can't redo any steps
  @Override
  public void onBackPressed() {}

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      getActionBar().hide();
    } catch (NullPointerException e) {
      Log.i(TAG, "No ActionBar to hide...");
    }

  }

  private static final String TAG = BaseSocketActivity.class.getSimpleName();

  public WebSocketService mService;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      Log.i(TAG, "onServiceConnected");
      WebSocketService.WebSocketsBinder binder = (WebSocketService.WebSocketsBinder) service;
      mService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.i(TAG, "onServiceDisconnected");
      mService = null;
    }
  };

  @Override
  protected void onStart() {
    super.onStart();
    Intent intent = new Intent(this, WebSocketService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    unbindService(mConnection);
    super.onStop();
  }
}
