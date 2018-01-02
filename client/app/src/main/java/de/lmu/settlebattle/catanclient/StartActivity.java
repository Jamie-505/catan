package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.ACTION_CONNECTION_ESTABLISHED;
import static de.lmu.settlebattle.catanclient.utils.Constants.PROTOCOL_SUPPORTED;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import de.lmu.settlebattle.catanclient.network.WebSocketService;
import de.lmu.settlebattle.catanclient.network.WebSocketService.WebSocketsBinder;
import de.lmu.settlebattle.catanclient.player.Storage;

public class StartActivity extends Activity {

  private final String TAG = StartActivity.class.getSimpleName();

  private Button btnConnect;
  private boolean connected = false;
  private boolean compatible = false;
  private WebSocketService wsService;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      WebSocketsBinder binder = (WebSocketsBinder) service;
      wsService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      Log.e(TAG, "onServiceDisconnected");
    }
  };

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(ACTION_CONNECTION_ESTABLISHED)) {
        connected = true;
      }
      if(intent.getAction().equals(PROTOCOL_SUPPORTED)) {
        compatible = true;
        btnConnect.setText(R.string.btn_join);
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.startscreen);

    // erase old data
    Storage storage = new Storage(this);
    storage.clear();

    try {
      getActionBar().hide();
    } catch (NullPointerException e) {
      Log.i(TAG, "No ActionBar to hide...");
    }


    btnConnect = (Button) findViewById(R.id.connectButton);

    btnConnect.setOnClickListener((View v) -> {
      if (!connected) {
        btnConnect.setText(R.string.btn_connect);
        new Thread("webSocketThread") {
          public void run() {
            Intent serviceIntent = new Intent(StartActivity.this,
                WebSocketService.class);
            startService(serviceIntent);
          }
        }.start();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
            new IntentFilter(ACTION_CONNECTION_ESTABLISHED));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
            new IntentFilter(PROTOCOL_SUPPORTED));
        Intent bindIntent = new Intent(this, WebSocketService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
      } else if (compatible) {
        Intent nextIntent = new Intent(StartActivity.this, SelectPlayerActivity.class);
        startActivity(nextIntent);
      }

    });
  }
}

