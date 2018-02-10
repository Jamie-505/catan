package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.CONNECTION_LOST;
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
import com.golovin.fluentstackbar.FluentSnackbar;
import com.jakewharton.processphoenix.ProcessPhoenix;
import de.lmu.settlebattle.catanclient.network.WebSocketService;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;

public abstract class BaseSocketActivity extends AppCompatActivity {

  protected FluentSnackbar snackbar;
  protected IntentFilter filter;
  protected BroadcastReceiver superBcReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (PLAYER_LEFT.equals(intent.getAction())) {
        Player p = Storage.getPlayer(intent.getIntExtra(PLAYER_LEFT, -1));
        // player might try to join after game started and if this player leaves
        // p is null
        if (p != null) {
          displaySnackBar(
              String.format("%s hat die Verbindung verloren - eine KI übernimmt",
                  p.name), p.id);
        } else {
          displaySnackBar("Jemand hat versucht nachträglich dem Spiel beizutreten", null);
        }
      } else if (CONNECTION_LOST.equals(intent.getAction())) {
        displayConnectionLoss();
      }
    }
  };

  // disable back button so you can't redo any steps
  @Override
  public void onBackPressed() {}

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    snackbar = FluentSnackbar.create(this);


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
  protected void onResume() {
    super.onResume();

    filter = new IntentFilter(PLAYER_LEFT);
    filter.addAction(CONNECTION_LOST);
    LocalBroadcastManager.getInstance(this).registerReceiver(superBcReceiver, filter);
  }

  @Override
  protected void onPause() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(superBcReceiver);
    super.onPause();

  }

  @Override
  protected void onStop() {
    unbindService(mConnection);
    super.onStop();
  }

  public void displaySnackBar(String msg, Integer playerId){
    int color = R.color.colorLightGray;
    if (playerId != null) {
      Player p = Storage.getPlayer(playerId);
      color = getResources().getIdentifier(p.color.toLowerCase() , "color", getPackageName());
    }
    snackbar.create(msg).textColorRes(color).important().show();
  }

  public void displayConnectionLoss() {
    snackbar.create(R.string.connection_lost)
        .duration(Snackbar.LENGTH_INDEFINITE)
        .errorBackgroundColor()
        .maxLines(8)
        .important()
        .actionText("OK")
        .action(v -> {
          Intent backToStart = new Intent(this, StartActivity.class);
          ProcessPhoenix.triggerRebirth(this, backToStart);
          this.finish();
        })
        .show();
  }
}
