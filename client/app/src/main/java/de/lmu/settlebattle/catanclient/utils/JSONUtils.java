package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.utils.Message.Player;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

  private static final String TAG = JSONUtils.class.getSimpleName();

  private Gson gson = new Gson();

  /**
   * parses JSON Strings received from the backend server
   * @param msg JSON string to be interpreted
   */
  public Object[] parse(final String msg) {

    try {
      JSONObject jObj = new JSONObject(msg);

      String msgType = jObj.keys().next();

      switch (msgType) {
        case HANDSHAKE:
          return completeHandshake(jObj.getJSONObject(HANDSHAKE));
        case GET_ID:
          Player player = gson.fromJson(jObj.getString(GET_ID), Player.class);
          return new Object[] { TO_STORAGE, player.id };
        case STATUS_UPD:
          // TODO: handle different Status updates
          // TODO: go to select Player Activity
        case GAME_START:
          // TODO: handle game start
          // TODO: go to Main Activity, render board, etc...
        default:
          return displayError("Protokoll wird nicht unterstützt");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private Object[] displayError(String errorMsg) {
    return new String[] { ERROR, errorMsg };
  }

  private String[] completeHandshake(JSONObject jsonMsg) {
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

  private String createJSONString(String messageType, HashMap information) {
    try {
      JSONObject jObj = new JSONObject();
      JSONObject payload = new JSONObject(information);
      jObj.put(messageType, payload);
      return jObj.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
  }

//
//        String sessionId = jObj.getString("sessionId");
//
//        // Save the session id in shared preferences
//        storage.storeSessionId(sessionId);
//
//        Log.i(TAG, "Your session id: " + storage.getSessionId());
//
//      } else if (flag.equalsIgnoreCase(TAG_NEW)) {
//        // If the flag is 'new', new person joined the room
//        String name = jObj.getString("name");
//        String message = jObj.getString("message");
//
//        // number of people online
//        String onlineCount = jObj.getString("onlineCount");
//
//        showToast(name + message + ". Currently " + onlineCount
//            + " people online!");
//
//      } else if (flag.equalsIgnoreCase(TAG_MESSAGE)) {
//        // if the flag is 'message', new message received
//        String fromName = name;
//        String message = jObj.getString("message");
//        String sessionId = jObj.getString("sessionId");
//        boolean isSelf = true;
//
//        // Checking if the message was sent by you
//        if (!sessionId.equals(storage.getSessionId())) {
//          fromName = jObj.getString("name");
//          isSelf = false;
//        }
//
//        JsonMessage m = new JsonMessage(fromName, message, isSelf);
//
//        // Appending the message to chat list
//        appendMessage(m);
//
//      } else if (flag.equalsIgnoreCase(TAG_EXIT)) {
//        // If the flag is 'exit', somebody left the conversation
//        String name = jObj.getString("name");
//        String message = jObj.getString("message");
//
//        showToast(name + message);
//      }

}
