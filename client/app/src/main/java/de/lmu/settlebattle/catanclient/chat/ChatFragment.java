package de.lmu.settlebattle.catanclient.chat;

import static de.lmu.settlebattle.catanclient.utils.Constants.CHAT_OUT;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.Storage;

public class ChatFragment extends MainActivityFragment {

  // LogCat tag
  private static final String TAG = ChatFragment.class.getSimpleName();

  private EditText inputMsg;

  // Chat messages list adapter
  private MessagesListAdapter adapter;
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View chatView = inflater
        .inflate(R.layout.fragment_chat, container, false);

    Button btnSend = chatView.findViewById(R.id.btnSend);
    inputMsg = chatView.findViewById(R.id.inputMsg);
    ListView listViewMessages = chatView.findViewById(R.id.list_view_messages);

    // Getting the person name from previous screen

    btnSend.setOnClickListener(v -> {
      // Sending message to web socket server
      String input = inputMsg.getText().toString();
      ChatMessage cMsg = new ChatMessage(input);
      // check if not all whitespaces
      if (!input.isEmpty()) {
        fragHandler.sendMsgToServer(createJSONString(CHAT_OUT, cMsg));
      }

      // Clearing the input filed once message was sent
      inputMsg.setText("");
    });

    adapter = new MessagesListAdapter(getContext(), Storage.getChatMessages());
    listViewMessages.setAdapter(adapter);

    return chatView;
  }

  /**
   * Appending message to list view
   * */
  public void appendMessage(final ChatMessage m) {
    Storage.addChatMsg(m);
    if (adapter != null) adapter.notifyDataSetChanged();
  }
}