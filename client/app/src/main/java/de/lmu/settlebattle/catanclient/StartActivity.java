package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.NO_CONNECTION;
import static de.lmu.settlebattle.catanclient.utils.Constants.PROTOCOL_SUPPORTED;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import de.lmu.settlebattle.catanclient.network.WebSocketService;
import java.util.Locale;

public class StartActivity extends Activity {

  private final String TAG = StartActivity.class.getSimpleName();

  private Button btnConnect;
  private CountDownTimer countDown;
  private static Thread wSThread;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(PROTOCOL_SUPPORTED.equals(intent.getAction())) {
        Intent nextIntent = new Intent(StartActivity.this, SelectPlayerActivity.class);
        startActivity(nextIntent);
      } else if (NO_CONNECTION.equals(intent.getAction())) {
        countDown.cancel();
        btnConnect.setEnabled(true);
        btnConnect.setText(R.string.try_reconnect);
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.startscreen);

    IntentFilter filter = new IntentFilter(PROTOCOL_SUPPORTED);
    filter.addAction(NO_CONNECTION);
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,filter);

    wSThread = new Thread("webSocketThread") {
      public void run() {
        Intent serviceIntent = new Intent(StartActivity.this,
            WebSocketService.class);
        startService(serviceIntent);
      }
    };

    btnConnect = findViewById(R.id.connectButton);

    btnConnect.setOnClickListener((View v) -> {
      btnConnect.setEnabled(false);
      countDown = new CountDownTimer(5000, 1000) {

        public void onTick(long millisUntilFinished) {
          String text = String.format(Locale.GERMAN, "Verbindung wird hergestellt... (%d)", millisUntilFinished/1000);
          btnConnect.setText(text);
        }

        public void onFinish() {
          btnConnect.setText(R.string.try_reconnect);
          btnConnect.setEnabled(true);
        }
      }.start();
      wSThread.start();
    });
  }
}

