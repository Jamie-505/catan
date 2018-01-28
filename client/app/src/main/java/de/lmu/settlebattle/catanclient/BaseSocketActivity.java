package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_LEFT;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import de.lmu.settlebattle.catanclient.network.WebSocketService;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;

public abstract class BaseSocketActivity extends AppCompatActivity {

  protected IntentFilter filter;
  protected BroadcastReceiver superBcReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(PLAYER_LEFT)) {
        Player p = Storage.getPlayer(intent.getIntExtra(PLAYER_LEFT, -1));
        displaySnackBar(
            String.format("%s hat die Verbindung verloren - eine KI übernimmt",
            p.name));
      }
    }
  };

  // disable back button so you can't redo any steps
  @Override
  public void onBackPressed() {}

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    filter = new IntentFilter(PLAYER_LEFT);
    LocalBroadcastManager.getInstance(this).registerReceiver(superBcReceiver, filter);

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

  public void displaySnackBar(String msg){
    View layout = findViewById(R.id.contain);
    Snackbar snackbar = Snackbar.make(layout, msg, Snackbar.LENGTH_LONG);
    snackbar.show();
  }

}
