package de.lmu.settlebattle.catanclient.network;

import static de.lmu.settlebattle.catanclient.utils.Constants.BOARD;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_STREET;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_VILLAGE;
import static de.lmu.settlebattle.catanclient.utils.Constants.CHAT_IN;
import static de.lmu.settlebattle.catanclient.utils.Constants.CONNECTION_LOST;
import static de.lmu.settlebattle.catanclient.utils.Constants.COSTS;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_RESULT;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_THROW;
import static de.lmu.settlebattle.catanclient.utils.Constants.DISPLAY_ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.ERROR_MSG;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_READY;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_START;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.GET_ID;
import static de.lmu.settlebattle.catanclient.utils.Constants.HARVEST;
import static de.lmu.settlebattle.catanclient.utils.Constants.NEW_CONSTRUCT;
import static de.lmu.settlebattle.catanclient.utils.Constants.NEXT_ACTIVITY;
import static de.lmu.settlebattle.catanclient.utils.Constants.NO_CONNECTION;
import static de.lmu.settlebattle.catanclient.utils.Constants.OK;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_LEFT;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_UPDATE;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.PROTOCOL_SUPPORTED;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER_AT;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER_TO;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROLL_DICE;
import static de.lmu.settlebattle.catanclient.utils.Constants.STATUS_UPD;
import static de.lmu.settlebattle.catanclient.utils.Constants.STATUS_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.TOSS_CARDS;
import static de.lmu.settlebattle.catanclient.utils.Constants.TOSS_CARDS_REQ;
import static de.lmu.settlebattle.catanclient.utils.Constants.TO_SERVER;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ABORTED;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ACC;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_FIN;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_OFFER;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_SENT;

import android.app.Service;
import android.content.Intent;
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
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;


public class WebSocketService extends Service {

  private final String TAG = WebSocketService.class.getSimpleName();

  private boolean socketConnected = false;
  public static boolean mainActivityActive = false;

  private final IBinder binder = new WebSocketsBinder();
  private Gson gson = new Gson();
  private WebSocketClient webSocketClient;
  private LocalBroadcastManager localBroadcastManager;

  public WebSocketService() {
    try {
      localBroadcastManager = LocalBroadcastManager.getInstance(this);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommand");
    if (localBroadcastManager == null) {
      localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }
    if(socketConnected) {
      stopSocket();
    }
    startSocket();
    socketConnected = true;
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (localBroadcastManager == null) {
      localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }
    Log.d(TAG, "onBind");
    if(!socketConnected){
      startSocket();
      socketConnected = true;
    }
    return binder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.d(TAG, "onUnbind");
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
        if (Storage.isItMe(player.id)) itsMe = true;
        if (Storage.getSessionId() != -1) Storage.storePlayer(player);
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
        Storage.storePlayer(player);
        Intent buildTrade = new Intent(BUILD_TRADE);
        buildTrade.putExtra(PLAYER, gson.toJson(player));
        broadcast(buildTrade);
        break;
      case CHAT_IN:
        Intent newChat = new Intent(CHAT_IN);
        newChat.putExtra(CHAT_IN, mail[1].toString());
        broadcast(newChat);
        break;
      case COSTS:
      case HARVEST:
        String type = mail[0].toString();
        Intent harvest = new Intent(type);
        harvest.putExtra(type, mail[1].toString());
        broadcast(harvest);
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
        Storage.storePlayer(player);
        if (itsMe) {
          // should cause select player activity so switch to lobby
          broadcast(NEXT_ACTIVITY);
        } else {
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
        Storage.storePlayer(player);
        // updates lobby with latest data
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
      case PLAYER_LEFT:
        int id = Integer.valueOf(mail[1].toString());
        Intent playerLeft = new Intent(PLAYER_LEFT);
        playerLeft.putExtra(PLAYER_LEFT, id);
        broadcast(playerLeft);
        break;
      case ROBBER_AT:
        String robberStr = mail[1].toString();
        Robber robber = gson.fromJson(robberStr, Robber.class);
        if (Storage.isItMe(robber.player)) {
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
        Storage.storePlayer(player);
        broadcast(STATUS_UPD);
        break;
      case STATUS_WAIT:
        if (itsMe) broadcast(PLAYER_WAIT);
        else broadcast(STATUS_UPD);
        Storage.storePlayer(player);
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
      case GET_ID:
        Log.d(TAG, (player.id + " --> TO_STORAGE"));
        Storage.storeSessionId(player.id);
        Storage.storePlayer(player);
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
        if (Storage.isItMe(trade.player)) {
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
      try {
        uri = new URI(WebSocketClientConfig.URL_WEBSOCKET);
      } catch (URISyntaxException e) {
        e.printStackTrace();
        return;
      }
      webSocketClient = new WebSocketClient(uri, new Draft_6455()) {
        @Override
        public void onOpen(ServerHandshake handshakedata) {
          Log.d(TAG, "Websocket onConnect()");
          socketConnected = true;
        }

        @Override
        public void onMessage(String message) {
          Log.d(TAG, "Websocket onMessage() -> " + message);
          messageReceived(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
          Log.d(TAG,
              "Websocket onDisconnect()| code: " + code + " |reason: " + reason + " | remote: "
                  + remote);
          stopSocket();
          broadcast(CONNECTION_LOST);
        }

        @Override
        public void onError(Exception error) {
          Log.d(TAG, "Websocket onError()");
          if (webSocketClient != null) {
            Log.e(TAG, "Error", error);
            stopSocket();
            localBroadcastManager.sendBroadcast(new Intent(NO_CONNECTION));
          }
        }
      };
      webSocketClient.connect();
  }

  public void stopSocket() {
    Storage.clear();
    socketConnected = false;
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

  public void sendMessage(String jsonMsg) {
    Log.d(TAG, "Send to server -> " + jsonMsg);
    try {
      webSocketClient.send(jsonMsg);
    } catch (WebsocketNotConnectedException e) {
      broadcast(CONNECTION_LOST);
    }
  }

  public final class WebSocketsBinder extends Binder {
    public WebSocketService getService() {
      return WebSocketService.this;
    }
  }
}