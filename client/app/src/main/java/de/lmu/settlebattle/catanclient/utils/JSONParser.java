package de.lmu.settlebattle.catanclient.utils;

public class JSONParser {

  // TODO: implement JSON parser

//  /**
//   *
//   * @param msg
//   */
//  private void parseMessage(final String msg) {
//
//    try {
//      JSONObject jObj = new JSONObject(msg);
//
//      // JSON node 'flag'
//      String flag = jObj.getString("flag");
//
//      // if flag is 'self', this JSON contains session id
//      if (flag.equalsIgnoreCase(TAG_SELF)) {
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
//
//    } catch (JSONException e) {
//      e.printStackTrace();
//    }
//
//}

}
