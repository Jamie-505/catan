package de.lmu.settlebattle.catanclient.network;

import static de.lmu.settlebattle.catanclient.utils.Constants.ACTION_CONNECTION_ESTABLISHED;
import static de.lmu.settlebattle.catanclient.utils.Constants.ACTION_NETWORK_STATE_CHANGED;
import static de.lmu.settlebattle.catanclient.utils.Constants.BOARD;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_STREET;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_VILLAGE;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_RESULT;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_THROW;
import static de.lmu.settlebattle.catanclient.utils.Constants.DISPLAY_ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.ERROR_MSG;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_READY;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_START;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.NEW_CONSTRUCT;
import static de.lmu.settlebattle.catanclient.utils.Constants.NEXT_ACTIVITY;
import static de.lmu.settlebattle.catanclient.utils.Constants.OK;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_UPDATE;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.PROTOCOL_SUPPORTED;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER_AT;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER_TO;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROLL_DICE;
import static de.lmu.settlebattle.catanclient.utils.Constants.STATUS_UPD;
import static de.lmu.settlebattle.catanclient.utils.Constants.TOSS_CARDS;
import static de.lmu.settlebattle.catanclient.utils.Constants.TOSS_CARDS_REQ;
import static de.lmu.settlebattle.catanclient.utils.Constants.TO_SERVER;
import static de.lmu.settlebattle.catanclient.utils.Constants.TO_STORAGE;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ABORTED;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ACC;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_FIN;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_OFFER;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_SENT;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;
import de.lmu.settlebattle.catanclient.robber.Robber;
import de.lmu.settlebattle.catanclient.trade.Trade;
import de.lmu.settlebattle.catanclient.utils.JSONUtils;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;


public class WebSocketService extends Service {

  private final String TAG = WebSocketService.class.getSimpleName();

  private boolean socketConnected = false;
  public static boolean mainActivityActive = false;

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
    Player player = new Player();
    boolean itsMe = false;
    try {
      if (mail[1] instanceof Player) {
        player = (Player) mail[1];
        itsMe = storage.isItMe(player);
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      // nothing to do here
      // just means that no further info was send in the mail
    }
    switch (mail[0].toString()) {
      case BUILD_VILLAGE:
        if (itsMe) {
          while(!mainActivityActive) {
            try {
              Thread.sleep(50);
            } catch (InterruptedException e) {
              Log.e(TAG, "NEW CONSTRUCT WAIT FAILED");
              e.printStackTrace();
            }
          }
          broadcast(BUILD_VILLAGE);
        }
        break;
      case BUILD_STREET:
        if (itsMe) {
          broadcast(BUILD_STREET);
        }
        break;
      case BUILD_TRADE:
        if (itsMe) {
          // should cause select player activity so switch to lobby
          storage.storePlayer(player);
        } else {
          storage.storeOpponent(player);
          // updates lobby with latest data
        }
        Intent buildTrade = new Intent(BUILD_TRADE);
        buildTrade.putExtra(PLAYER, gson.toJson(player));
        broadcast(buildTrade);
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
        if (itsMe) {
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
        if (itsMe) {
          // should cause select player activity so switch to lobby
          storage.storePlayer(player);
        } else {
          storage.storeOpponent(player);
          // updates lobby with latest data
        }
        broadcast(PLAYER_WAIT);
        break;
      case NEW_CONSTRUCT:
        while(!mainActivityActive) {
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            Log.e(TAG, "NEW CONSTRUCT WAIT FAILED");
            e.printStackTrace();
          }
        }
        Intent newConstruct = new Intent(NEW_CONSTRUCT);
        newConstruct.putExtra(NEW_CONSTRUCT, mail[1].toString());
        broadcast(newConstruct);
        break;
      case OK:
        broadcast(OK);
        break;
      case ROBBER_AT:
        String robberStr = mail[1].toString();
        Robber robber = gson.fromJson(robberStr, Robber.class);
        if (storage.isItMe(robber.player)) {
          broadcast(ROBBER_AT);
        } else {
          Intent robberIntent = new Intent(ROBBER);
          robberIntent.putExtra(ROBBER, robberStr);
          broadcast(robberIntent);
        }
        break;
      case ROBBER_TO:
        if (itsMe) {
          broadcast(ROBBER_TO);
        }
        break;
      case ROLL_DICE:
        if (itsMe) {
          broadcast(ROLL_DICE);
        } else {
          broadcast(PLAYER_WAIT);
        }
        break;
      case STATUS_UPD:
        if (itsMe) {
          storage.storePlayer(player);
        } else {
          storage.storeOpponent(player);
        }
        broadcast(STATUS_UPD);
        break;
      case TOSS_CARDS_REQ:
        if (itsMe) {
          broadcast(TOSS_CARDS);
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
        if (storage.isItMe(trade.player)) {
          tradeIntent = new Intent(TRD_SENT);
        } else {
          tradeIntent = new Intent(TRD_OFFER);
        }
        tradeIntent.putExtra(TRADE, gson.toJson(trade));
        broadcast(tradeIntent);
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
        Log.d(TAG, "Websocket onDisconnect()| code: " + code + " |reason: " + reason + " | remote: " + remote);
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