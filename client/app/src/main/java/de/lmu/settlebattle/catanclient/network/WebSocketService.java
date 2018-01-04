package de.lmu.settlebattle.catanclient.network;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.player.Player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.lmu.settlebattle.catanclient.trade.Trade;
import de.lmu.settlebattle.catanclient.utils.JSONUtils;
import de.lmu.settlebattle.catanclient.player.Storage;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;


public class WebSocketService extends Service {

  private final String TAG = WebSocketService.class.getSimpleName();

  private boolean socketConnected = false;

  private final IBinder binder = new WebSocketsBinder();
  private Gson gson = new Gson();
  private Storage storage;
  private WebSocketClient webSocketClient;
  private LocalBroadcastManager localBroadcastManager;

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

  public WebSocketService() {
    localBroadcastManager = LocalBroadcastManager.getInstance(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommand");
    storage = new Storage(getApplicationContext());
    Intent connectionIntent = new Intent(ACTION_CONNECTION_ESTABLISHED);
    if (localBroadcastManager == null) {
      localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }
    localBroadcastManager.sendBroadcast(connectionIntent);
    if(!socketConnected){
      startSocket();
      socketConnected = true;
    }
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (storage == null) {
      storage = new Storage(getApplicationContext());
    }
    if (localBroadcastManager == null) {
      localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }
    Log.d(TAG, "onBind");
    localBroadcastManager.registerReceiver(messageReceiver,
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
    localBroadcastManager.unregisterReceiver(messageReceiver);
    return false;
  }

  private void broadcast(Intent intent) {
    localBroadcastManager.sendBroadcast(intent);
  }

  private void broadcast(String action) {
    localBroadcastManager.sendBroadcast(new Intent(action));
  }

  private void messageReceived(String message) {
    Object[] mail = JSONUtils.parse(message);
    Player player;
    switch (mail[0].toString()) {
      case BUILD_TRADE:
        player = (Player) mail[1];
        if (player.id == storage.getSessionId()) {
          // should cause select player activity so switch to lobby
          storage.storePlayer(player);
        } else {
          storage.storeOpponent(player);
          // updates lobby with latest data
        }
        broadcast(PLAYER_UPDATE);
        break;
      case DICE_RESULT:
        Intent diceResult = new Intent(DICE_RESULT);
        diceResult.putExtra(DICE_THROW, mail[1].toString());
        broadcast(diceResult);
        break;
      case ERROR:
        displayError(mail[1].toString());
        break;
      case GAME_READY:
        player = (Player) mail[1];
        if (player.id == storage.getSessionId()) {
          // should cause select player activity so switch to lobby
          storage.storePlayer(player);
          broadcast(NEXT_ACTIVITY);
        } else {
          storage.storeOpponent(player);
          // updates lobby with latest data
          broadcast(PLAYER_UPDATE);
        }
        break;
      case GAME_START:
        Intent gameStart = new Intent(GAME_START);
        gameStart.putExtra(BOARD, mail[1].toString());
        broadcast(gameStart);
        break;
      case GAME_WAIT:
        player = (Player) mail[1];
        if (player.id == storage.getSessionId()) {
          // should cause select player activity so switch to lobby
          storage.storePlayer(player);
        } else {
          storage.storeOpponent(player);
          // updates lobby with latest data
        }
        broadcast(PLAYER_WAIT);
        break;
      case NEW_CONSTRUCT:
        Intent newConstruct = new Intent(NEW_CONSTRUCT);
        newConstruct.putExtra(NEW_CONSTRUCT, mail[1].toString());
        broadcast(newConstruct);
      case OK:
        broadcast(OK);
        break;
      case ROLL_DICE:
        player = (Player) mail[1];
        if (player.id == storage.getSessionId()) {
          broadcast(ROLL_DICE);
        } else {
          broadcast(PLAYER_WAIT);
        }
        break;
      case TO_SERVER:
        broadcast(PROTOCOL_SUPPORTED);
        webSocketClient.send(mail[1].toString());
        break;
      case TO_STORAGE:
        Log.d(TAG, ((Player) mail[1]).id + " --> TO_STORAGE");
        storage.storePlayer((Player) mail[1]);
        break;
      case TRD_ABORTED:
        broadcast(TRD_ABORTED);
        break;
      case TRD_ACC:
      case TRD_FIN:
        Trade trade = (Trade) mail[1];
        Intent i = new Intent(mail[0].toString());
        i.putExtra(TRADE, gson.toJson(trade));
        broadcast(i);
        break;
      case TRD_OFFER:
        trade = (Trade) mail[1];
        Intent tradeIntent;
        if (storage.getSessionId() == trade.player) {
          tradeIntent = new Intent(TRD_SENT);
        } else {
          tradeIntent = new Intent(TRD_OFFER);
        }
        tradeIntent.putExtra(TRADE, gson.toJson(trade));
        broadcast(tradeIntent);
        break;
      default:
        break;
    }
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


  /**
   * broadcasts errors to any Activity so they can be displayed
   * @param errorMessage
   */
  private void displayError(String errorMessage) {
    Intent errorIntent = new Intent(DISPLAY_ERROR);
    errorIntent.putExtra(ERROR_MSG, errorMessage);
    localBroadcastManager.sendBroadcast(errorIntent);
  }

  private void connectionOpened(){
    socketConnected = true;
    Intent intent = new Intent(ACTION_CONNECTION_ESTABLISHED);
    localBroadcastManager.sendBroadcast(intent);
  }

  public void sendMessage(String jsonMsg) {
    Log.d(TAG, "Send to server -> " + jsonMsg);
    webSocketClient.send(jsonMsg);
  }


  public final class WebSocketsBinder extends Binder {
    public WebSocketService getService() {
      return WebSocketService.this;
    }
  }
}