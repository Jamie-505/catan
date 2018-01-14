package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.DISPLAY_ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ABORTED;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ACC;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_REJ;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_REQ;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_SEL;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_SENT;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.lmu.settlebattle.catanclient.MainActivity;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.RawMaterialOverview;
import de.lmu.settlebattle.catanclient.utils.JSONUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DomTradeFragment extends MainActivityFragment {

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null) {
        switch (action) {
          case DISPLAY_ERROR:
            if (sendTradeBtn != null) {
              sendTradeBtn.setClickable(true);
              sendTradeBtn.setText(R.string.offer_trade);
            }
            break;
          case TRD_ABORTED:
            mainActivity.onBackPressed();
            break;
          case TRD_ACC:
            trade = gson.fromJson(intent.getStringExtra(TRADE), Trade.class);
            updateTradeStatus(trade.opponent, trade.accept);
            break;
          case TRD_SENT:
            trade = gson.fromJson(intent.getStringExtra(TRADE), Trade.class);
            if (sendTradeBtn != null && tradeStatus != null) {
              sendTradeBtn.setVisibility(View.GONE);
              tradeStatus.setVisibility(View.VISIBLE);
              cancelBtn.setVisibility(View.VISIBLE);
            }
            updateTradeStatus(-1, false);
            break;
        }
      }
    }
  };

  private Player[] players = new Player[3];
  private Gson gson = new Gson();
  private Button cancelBtn;
  private Button sendTradeBtn;
  private CardView tradeStatus;
  private MainActivity mainActivity;
  private Map<String, int[]> tradeInfo = new HashMap<>();
  private String[] fields = new String[] { "Clay", "Ore", "Wheat", "Wood", "Wool" };
  private String[] types = new String[] { "offer", "req" };
  private Trade trade;
  private View fragmentView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    fragmentView = inflater.inflate(R.layout.fragment_dom_trade, container, false);
    // Inflate the layout for this fragment
    mainActivity = (MainActivity) getActivity();


    IntentFilter filter = new IntentFilter();
    filter.addAction(DISPLAY_ERROR);
    filter.addAction(TRD_ACC);
    filter.addAction(TRD_SENT);
    filter.addAction(TRD_ABORTED);
    LocalBroadcastManager.getInstance(mainActivity.getApplicationContext())
        .registerReceiver(broadcastReceiver, filter);

    return fragmentView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    cancelBtn = (Button) fragmentView.findViewById(R.id.cancelBtn);
    sendTradeBtn = (Button) fragmentView.findViewById(R.id.sendTradeBtn);
    tradeStatus = (CardView) fragmentView.findViewById(R.id.tradeStatus);

    cancelBtn.setOnClickListener((View v) -> cancelTrade());

    sendTradeBtn.setOnClickListener((View v) -> {
      sendTradeRequest();
      sendTradeBtn.setClickable(false);
      sendTradeBtn.setText(R.string.trade_sent);
    });
  }

  @Override
  public void onResume() {
    super.onResume();

    // set everything to 0
    for (String type : types) {
      for (String resource : fields) {
        int resId = getResources().getIdentifier(
            type + resource + "Cnt",
            "id",
            getActivity().getPackageName());
        EditText resourceEdtxt = (EditText) fragmentView.findViewById(resId);
        resourceEdtxt.setText("0");
      }
    }
  }

  private void cancelTrade() {
    trade.offer = null;
    trade.opponent = null;
    trade.player = null;
    trade.request = null;
    trade.accept = null;
    String reply;
    reply = createJSONString(TRD_REJ, trade);
    mainActivity.mService.sendMessage(reply);
  }

  private void sendTradeRequest() {
    for (String type : types) {
      int[] qnts = new int[5];
      int i = 0;
      for (String resource : fields) {
        int resId = getResources().getIdentifier(
            type + resource + "Cnt",
            "id",
            mainActivity.getPackageName());
        EditText resourceEdtxt = (EditText) fragmentView.findViewById(resId);
        qnts[i++] = Integer.valueOf(resourceEdtxt.getText().toString(), 10);
      }
      tradeInfo.put(type, qnts);
    }
    RawMaterialOverview offer = new RawMaterialOverview(tradeInfo.get("offer"));
    RawMaterialOverview req = new RawMaterialOverview(tradeInfo.get("req"));
    Trade trade = new Trade(offer, req);

    String tradeMsg = createJSONString(TRD_REQ, trade);
    mainActivity.mService.sendMessage(tradeMsg);
  }

  private void setPlayers(String playersJSON) {
    Type type = new TypeToken<Player[]>(){}.getType();
    Player[] allPlayers = gson.fromJson(playersJSON, type);
    System.arraycopy(allPlayers, 1, players, 0, allPlayers.length-1);
  }

  private void updateTradeStatus(int id, boolean accepted) {
    String allPlayers = mainActivity.storage.getAllPlayersAsJson();
    setPlayers(allPlayers);

    for (int i = 0; i < players.length; i++) {
      if (players[i] != null) {
        int resIdCon = getResources().getIdentifier(
            "p" + i, "id", mainActivity.getPackageName()
        );
        int resIdNam = getResources().getIdentifier(
            "p"+i+"Name", "id", mainActivity.getPackageName()
        );
        int resIdSta = getResources().getIdentifier(
            "p"+i+"Status", "id", mainActivity.getPackageName()
        );
        LinearLayout playerView = (LinearLayout) fragmentView.findViewById(resIdCon);
        playerView.setVisibility(View.VISIBLE);
        TextView playerName = (TextView) fragmentView.findViewById(resIdNam);
        playerName.setText(players[i].name);
        ImageButton playerStatus = (ImageButton) fragmentView.findViewById(resIdSta);
        int colorId = getResources().getIdentifier(
            players[i].color.toLowerCase(), "color", mainActivity.getPackageName()
        );
        playerStatus.setBackgroundColor(getResources().getColor(colorId));
        if (players[i].id == id) {
          if (accepted) {
            playerStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_success));
            playerStatus.setOnClickListener((View v) -> {
              Trade finalTrade = new Trade(null, null);
              finalTrade.id = trade.id;
              finalTrade.opponent = id;
              String answer = JSONUtils.createJSONString(TRD_SEL, finalTrade);
              mainActivity.mService.sendMessage(answer);
            });
            playerStatus.setClickable(true);
          } else {
            playerStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_decline));
            playerStatus.setClickable(false);
          }
        }
      }
    }
  }
}
