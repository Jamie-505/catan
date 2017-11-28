//package de.lmu.settlebattle.catanclient;
//
//import android.app.Activity;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//import de.lmu.settlebattle.catanclient.network.JsonMessage;
//import java.util.List;
//
//public class MessagesListAdapter extends BaseAdapter {
//
//  private Context context;
//  private List<JsonMessage> messagesItems;
//
//  public MessagesListAdapter(Context context, List<JsonMessage> navDrawerItems) {
//    this.context = context;
//    this.messagesItems = navDrawerItems;
//  }
//
//  @Override
//  public int getCount() {
//    return messagesItems.size();
//  }
//
//  @Override
//  public Object getItem(int position) {
//    return messagesItems.get(position);
//  }
//
//  @Override
//  public long getItemId(int position) {
//    return position;
//  }
//
//  @Override
//  public View getView(int position, View convertView, ViewGroup parent) {
//    JsonMessage msg = messagesItems.get(position);
//
//    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//    if (messagesItems.get(position).isSelf()) {
//      convertView = mInflater.inflate(R.layout.list_item_message_right, null);
//    } else {
//      convertView = mInflater.inflate(R.layout.list_item_message_left, null);
//    }
//
//    TextView lblFrom = convertView.findViewById(R.id.lblMsgFrom);
//    TextView txtMsg = convertView.findViewById(R.id.txtMsg);
//
//    txtMsg.setText(msg.getPayload());
//    lblFrom.setText(msg.getType());
//
//    return convertView;
//  }
//
//}
