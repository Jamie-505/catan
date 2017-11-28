package de.lmu.settlebattle.catanclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import de.lmu.settlebattle.catanclient.network.WebSocketService;

public class StartActivity extends Activity {

  private final String DEBUG_TAG = StartActivity.class.getSimpleName();

  private Button btnConnect;
  private boolean connected = false;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      connected = true;
      btnConnect.setText(R.string.btn_join);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.startscreen);

    btnConnect = findViewById(R.id.connectButton);

    getActionBar().hide();

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
            new IntentFilter(WebSocketService.ACTION_CONNECTION_ESTABLISHED));
      } else if (connected) {
        Intent nextIntent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(nextIntent);
      }

    });
  }
}

