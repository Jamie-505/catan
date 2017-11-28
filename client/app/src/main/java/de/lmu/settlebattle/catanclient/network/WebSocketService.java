package de.lmu.settlebattle.catanclient.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;


public class WebSocketService extends Service {

  public static final String ACTION_CONNECTION_ESTABLISHED = "connectionEstablished";
  public static final String ACTION_MSG_RECEIVED = "msgReceived";
  public static final String ACTION_MSG_TO_SEND = "sendMsg";
  public static final String ACTION_NETWORK_STATE_CHANGED = "networkStateChanged";

  private static final String TAG = WebSocketService.class.getSimpleName();

  private boolean socketConnected = false;
  private final IBinder binder = new WebSocketsBinder();
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
//      if(intent.getAction().equals(ACTION_MSG_TO_SEND)){
//        Log.d(TAG, "sending to sever");
//        webSocketClient.send(intent.getStringExtra("JSONMsg"));
//      }
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

  public void createJson(String messageType, Serializable data){
    // TODO: also in JSONHelper
  }

  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "onBind");
    LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
        new IntentFilter(WebSocketService.ACTION_NETWORK_STATE_CHANGED));
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
        Log.d(TAG, "Websocket onMessage()");
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
    // TODO: also send broadcast with message received
    // TODO: handle message with JSONHelper
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