package de.lmu.settlebattle.catanclient.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;
import java.util.List;

public class MessagesListAdapter extends BaseAdapter {

  private Context context;
  private List<ChatMessage> messagesItems;

  public MessagesListAdapter(Context context, List<ChatMessage> navDrawerItems) {
    this.context = context;
    this.messagesItems = navDrawerItems;
  }

  @Override
  public int getCount() {
    return messagesItems.size();
  }

  @Override
  public Object getItem(int position) {
    return messagesItems.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ChatMessage msg = messagesItems.get(position);

    Player player = Storage.getPlayer(msg.getSenderId());
    boolean fromMe = Storage.isItMe(msg.getSenderId());

    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    if (fromMe) {
      convertView = mInflater.inflate(R.layout.list_item_message_right, null);
    } else {
      convertView = mInflater.inflate(R.layout.list_item_message_left, null);
    }

    TextView lblFrom = convertView.findViewById(R.id.lblMsgFrom);
    TextView txtMsg = convertView.findViewById(R.id.txtMsg);
    Drawable msgBackGround = colorizeMsgBackGround(player.color);

    txtMsg.setText(msg.getContent());
    txtMsg.setBackground(msgBackGround);
    lblFrom.setText(player.name);

    return convertView;
  }

  private Drawable colorizeMsgBackGround(String colorStr) {
    int colorId = context.getResources().getIdentifier(colorStr.toLowerCase() , "color", context.getPackageName());
    GradientDrawable bg = (GradientDrawable) context.getDrawable(R.drawable.bg_msg).mutate();
    bg.setColor(ContextCompat.getColor(context, colorId));

    return bg;
  }

}
