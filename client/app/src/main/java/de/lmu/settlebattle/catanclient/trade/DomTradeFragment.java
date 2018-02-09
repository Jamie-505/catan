package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.DISPLAY_ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIALS;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.NumberPicker;
import de.lmu.settlebattle.catanclient.MainActivity;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.RawMaterialOverview;
import de.lmu.settlebattle.catanclient.player.Storage;
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
            closeFragment();
            break;
          case TRD_ACC:
            trade = gson.fromJson(intent.getStringExtra(TRADE), Trade.class);
            updateTradeStatus(trade.opponent, trade.accept);
            break;
          case TRD_SENT:
            trade = gson.fromJson(intent.getStringExtra(TRADE), Trade.class);
            if (sendTradeBtn != null && tradeStatus != null) {
              sendTradeBtn.setVisibility(View.GONE);
              disableNumPicks();
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
  private RawMaterialOverview rawMat;
  private String[] fields = new String[] { "wood", "clay", "wool", "wheat", "ore" };
  private String[] types = new String[] { "offer_", "req_" };
  private Trade trade;
  private View fragmentView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    if (getArguments() != null) {
      rawMat = gson.fromJson(getArguments().getString(RAW_MATERIALS),
          RawMaterialOverview.class);
    }
    fragmentView = inflater
        .inflate(R.layout.fragment_dom_trade, container, false);
    // Inflate the layout for this fragment
    mainActivity = (MainActivity) getActivity();

    NumberPicker woodCnt = fragmentView.findViewById(R.id.offer_wood_cnt);
    woodCnt.setMax(rawMat.getWoodCount());
    woodCnt.setUnit(1);

    NumberPicker clayCnt = fragmentView.findViewById(R.id.offer_clay_cnt);
    clayCnt.setMax(rawMat.getClayCount());
    clayCnt.setUnit(1);

    NumberPicker woolCnt = fragmentView.findViewById(R.id.offer_wool_cnt);
    woolCnt.setMax(rawMat.getWoolCount());
    woolCnt.setUnit(1);

    NumberPicker wheatCnt = fragmentView.findViewById(R.id.offer_wheat_cnt);
    wheatCnt.setMax(rawMat.getWheatCount());
    wheatCnt.setUnit(1);

    NumberPicker oreCnt = fragmentView.findViewById(R.id.offer_ore_cnt);
    oreCnt.setMax(rawMat.getOreCount());
    oreCnt.setUnit(1);

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
    Button initCnclBtn = fragmentView.findViewById(R.id.init_cancel_btn);
    ImageButton cancelBtn = fragmentView.findViewById(R.id.cancel_btn);
    Button sendTradeBtn = fragmentView.findViewById(R.id.send_trade_btn);
    tradeStatus = fragmentView.findViewById(R.id.trade_status);

    cancelBtn.setOnClickListener((View v) -> cancelTrade());

    cancelBtn.setOnClickListener((View v) -> fragHandler.closeFragment(this));

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
            type + resource + "_cnt",
            "id",
            getActivity().getPackageName());
        NumberPicker resourceEdtxt = fragmentView.findViewById(resId);
        resourceEdtxt.setValue(0);
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
    fragHandler.sendMsgToServer(reply);
  }

  private void closeFragment() {
    fragHandler.closeFragment(this);
  }

  private void disableNumPicks() {
    for (String type : types) {
      for (String resource : fields) {
        int resId = getResources().getIdentifier(
            type + resource + "_cnt",
            "id",
            mainActivity.getPackageName());
        NumberPicker resourceEdtxt = fragmentView.findViewById(resId);
        resourceEdtxt.setActionEnabled(ActionEnum.DECREMENT, false);
        resourceEdtxt.setActionEnabled(ActionEnum.INCREMENT, false);
      }
    }
  }

  private void sendTradeRequest() {
    for (String type : types) {
      int[] qnts = new int[5];
      int i = 0;
      for (String resource : fields) {
        int resId = getResources().getIdentifier(
            type + resource + "_cnt",
            "id",
            mainActivity.getPackageName());
        NumberPicker resourceEdtxt = fragmentView.findViewById(resId);
        qnts[i++] = resourceEdtxt.getValue();
      }
      tradeInfo.put(type, qnts);
    }
    RawMaterialOverview offer = new RawMaterialOverview(tradeInfo.get("offer_"));
    RawMaterialOverview req = new RawMaterialOverview(tradeInfo.get("req_"));
    Trade trade = new Trade(offer, req);

    String tradeMsg = createJSONString(TRD_REQ, trade);
    fragHandler.sendMsgToServer(tradeMsg);
  }

  private void setPlayers(String playersJSON) {
    Type type = new TypeToken<Player[]>(){}.getType();
    Player[] allPlayers = gson.fromJson(playersJSON, type);
    System.arraycopy(allPlayers, 1, players, 0, allPlayers.length-1);
  }

  private void updateTradeStatus(int id, boolean accepted) {
    try {
      String allPlayers = Storage.getAllPlayersAsJson();
      setPlayers(allPlayers);

      for (int i = 0; i < players.length; i++) {
        if (players[i] != null) {
          int resIdCon = getResources().getIdentifier(
              "p" + i, "id", mainActivity.getPackageName()
          );
          int resIdNam = getResources().getIdentifier(
              "p" + i + "Name", "id", mainActivity.getPackageName()
          );
          int resIdSta = getResources().getIdentifier(
              "p" + i + "Status", "id", mainActivity.getPackageName()
          );
          LinearLayout playerView = fragmentView.findViewById(resIdCon);
          playerView.setVisibility(View.VISIBLE);
          TextView playerName = fragmentView.findViewById(resIdNam);
          playerName.setText(players[i].name);
          ImageButton playerStatus = fragmentView.findViewById(resIdSta);
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
                fragHandler.sendMsgToServer(answer);
                closeFragment();
              });
              playerStatus.setClickable(true);
            } else {
              playerStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_decline));
              playerStatus.setClickable(false);
            }
          }
        }
      }
    } catch (Exception e) {
      if (isAdded()) {
        fragHandler.closeFragment(this);
        fragHandler.displayFragMsg("Hoppla, da ist wohl was schief gelaufen");
      }
    }
  }
}
