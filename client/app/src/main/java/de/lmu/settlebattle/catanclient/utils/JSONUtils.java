package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;
import de.lmu.settlebattle.catanclient.trade.Trade;
import de.lmu.settlebattle.catanclient.utils.Message.Error;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

  private static final String TAG = JSONUtils.class.getSimpleName();

  private static Gson gson = new Gson();

  /**
   * parses JSON Strings received from the backend server
   * @param msg JSON string to be interpreted
   */
  public static Object[] parse(final String msg) {
    try {
      JSONObject jObj = new JSONObject(msg);
      String msgType = jObj.keys().next();

      switch (msgType) {
        case CHAT_IN:
          return new String[] { CHAT_IN, jObj.getString(msgType)};
        case DICE_RESULT:
          String dice = jObj.getString(DICE_RESULT);
          return new String[] { DICE_RESULT, dice };
        case ERROR:
          Error error = gson.fromJson(jObj.getString(ERROR), Error.class);
          return displayError(error.Message);
        case GAME_START:
          JSONObject game = new JSONObject(jObj.getString(GAME_START));
          int[] order = gson.fromJson(game.getString(TURN_ORDER), int[].class);
          Storage.saveTurnOrder(order);
          return new String[] { GAME_START, game.getString(BOARD) };
        case GET_ID:
          Player player = gson.fromJson(jObj.getString(GET_ID), Player.class);
          return new Object[] { GET_ID, player };
        case HANDSHAKE:
          return completeHandshake(jObj.getJSONObject(HANDSHAKE));
        case MESSAGE:
          JSONObject pLeft = jObj.getJSONObject(MESSAGE);
          return new String[] { PLAYER_LEFT, pLeft.getString(PLAYER_LEFT) };
        case NEW_CONSTRUCT:
          String construct = jObj.getString(NEW_CONSTRUCT);
          return new String[] { NEW_CONSTRUCT, construct };
        case SERVER_RES:
          return new String[] { OK };
        case STATUS_UPD:
          JSONObject status = jObj.getJSONObject(STATUS_UPD);
          player = gson.fromJson(status.getString(PLAYER), Player.class);
          return handleStatusUpdate(player);
        case TRD_OFFER:
          Trade trade = gson.fromJson(jObj.getString(TRD_OFFER), Trade.class);
          return new Object[] { TRD_OFFER, trade };
        case TRD_ABORTED:
        case TRD_ACC:
        case TRD_FIN:
          trade = gson.fromJson(jObj.getString(msgType), Trade.class);
          return new Object[] { msgType, trade };
        case ROBBER_AT:
          return new String[] { msgType, jObj.getString(msgType) };
        case HARVEST:
        case COSTS:
          return new String[] { msgType, jObj.getString(msgType) };
        case DEV_CARD_BOUGHT:
          JSONObject obj = jObj.getJSONObject(msgType);
          player = Storage.getPlayer(obj.getInt(PLAYER));
          return new Object[] { DEV_CARD_BOUGHT, player, obj.getString(DEV_CARD) };
        case DEV_CARD_PLAYED:
          obj = jObj.getJSONObject(msgType);
          Integer pId = obj.getInt(PLAYER);
          String type = obj.getString(TYPE);
          return new Object[] { DEV_CARD_PLAYED, pId, type };
        default:
          return displayError("Protokoll wird nicht unterstützt");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Object[] handleStatusUpdate(Player player) {
    switch (player.status) {
      case BUILD_VILLAGE:
      case BUILD_STREET:
      case BUILD_TRADE:
      case GAME_READY:
      case GAME_START:
      case GAME_WAIT:
      case TOSS_CARDS_REQ:
      case ROBBER_TO:
      case RD_CON1:
      case RD_CON2:
      case ROLL_DICE:
      case STATUS_WAIT:
        return new Object[] { player.status, player };
      default:
        return new Object[] { STATUS_UPD, player };
    }
  }

  private static Object[] displayError(String errorMsg) {
    return new String[] { ERROR, errorMsg };
  }

  private static String[] completeHandshake(JSONObject jsonMsg) {
    try {
      if (jsonMsg.get(PROTOCOL).equals(VERSION_PROTOCOL)) {
        HashMap<String, String> clientInfo = new HashMap<>();
        clientInfo.put("Version", VERSION_CLIENT);
        String msg = createJSONString(HANDSHAKE, clientInfo);
        return new String[] { TO_SERVER, msg };
      }
      return new String[] { ERROR, PROTOCOL_MISSMATCH };
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String createJSONString(String messageType, HashMap information) {
    try {
      JSONObject jObj = new JSONObject();
      JSONObject payload = new JSONObject();
      if (information != null) {
        payload = new JSONObject(information);
      }
      jObj.put(messageType, payload);
      return jObj.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String createJSONString(String messageType, Object body) {
    Map<String, Object> msgMap = new HashMap<>();
    msgMap.put(messageType, body);
    return gson.toJson(msgMap);
  }

}
