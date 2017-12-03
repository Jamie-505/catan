package de.lmu.settlebattle.catanclient.network;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.lmu.settlebattle.catanclient.utils.JSONUtils;
import de.lmu.settlebattle.catanclient.utils.Storage;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;


public class WebSocketService extends Service {


  private final String TAG = WebSocketService.class.getSimpleName();

  private boolean socketConnected = false;

  private final IBinder binder = new WebSocketsBinder();
  private JSONUtils jsonUtils = new JSONUtils();
  private Storage storage;
  private WebSocketClient webSocketClient;

  private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      boolean networkIsOn = intent.getBooleanExtra(ACTION_NETWORK_STATE_CHANGED, false);
      if (networkIsOn) {
        startSocket();
      } else {
        stopSocket();
      }
    }
  };

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommand");
    Intent connectionIntent = new Intent(ACTION_CONNECTION_ESTABLISHED);
    LocalBroadcastManager.getInstance(this).sendBroadcast(connectionIntent);
    if(!socketConnected){
      startSocket();
      socketConnected = true;
    }
    return START_STICKY;
  }


  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "onBind");
    storage = new Storage(getApplicationContext());
    LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
        new IntentFilter(ACTION_NETWORK_STATE_CHANGED));
    if(!socketConnected){
      startSocket();
      socketConnected = true;
    }
    return binder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.d(TAG, "onUnbind");
    LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    return false;
  }

  private void startSocket() {
    URI uri;
    try{
      uri = new URI(WebSocketClientConfig.URL_WEBSOCKET);
    }
    catch(URISyntaxException e){
      e.printStackTrace();
      return;
    }
    webSocketClient = new WebSocketClient(uri, new Draft_6455()){
      @Override
      public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "Websocket onConnect()");
        connectionOpened();
      }
      @Override
      public void onMessage(String message) {
        Log.d(TAG, "Websocket onMessage() -> " + message);
        messageReceived(message);
      }
      @Override
      public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "Websocket onDisconnect()");
      }
      @Override
      public void onError(Exception error) {
        Log.d(TAG, "Websocket onError()");
        if (webSocketClient != null) {
          Log.e(TAG, "Error", error);
        }
      }
    };
    webSocketClient.connect();
  }

  private void stopSocket() {
    if (webSocketClient != null) {
      webSocketClient.close();
      webSocketClient = null;
    }
  }

  private void messageReceived(String message) {
    Object[] mail = jsonUtils.parse(message);
    switch (mail[0].toString()) {
      case TO_SERVER:
        Intent protocolIntent = new Intent(PROTOCOL_SUPPORTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(protocolIntent);
        webSocketClient.send(mail[1].toString());
        break;
      case TO_STORAGE:
        storage.storeSessionId(Integer.parseInt(mail[1].toString(), 16));
        Log.d(TAG, mail[1].toString() + " --> TO_STORAGE");
        break;
      case ERROR:
        displayError(mail[1].toString());
      default:
        break;
    }
  }

  /**
   * broadcasts errors to any Activity so they can be displayed
   * @param errorMessage
   */
  private void displayError(String errorMessage) {
    Intent errorIntent = new Intent(DISPLAY_ERROR);
    errorIntent.putExtra(ERROR_MSG, errorMessage);
    LocalBroadcastManager.getInstance(this).sendBroadcast(errorIntent);
  }

  private void connectionOpened(){
    socketConnected = true;
    Intent intent = new Intent(ACTION_CONNECTION_ESTABLISHED);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
  }

  public final class WebSocketsBinder extends Binder {
    public WebSocketService getService() {
      return WebSocketService.this;
    }
  }
}