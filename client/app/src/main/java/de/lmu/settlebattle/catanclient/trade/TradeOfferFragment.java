package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ABORTED;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ACC;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_RES;
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
import de.lmu.settlebattle.catanclient.MainActivity;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.Player;
import java.lang.reflect.Type;

public class TradeOfferFragment extends MainActivityFragment {

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null) {
        Trade tradeUpd = gson.fromJson(intent.getStringExtra(TRADE), Trade.class);
        switch (action) {
          case TRD_ACC:
            updateTradeStatus(tradeUpd.opponent, tradeUpd.accept);
            break;
          case TRD_ABORTED:
            mainActivity.onBackPressed();
        }
      }
    }
  };

  private Button accBtn;
  private Button cancelBtn;
  private Button decBtn;
  private CardView tradeStatus;
  private Gson gson = new Gson();
  private MainActivity mainActivity;
  private Player trader;
  private Player[] players = new Player[3];
  private String[] fields = new String[] { "Clay", "Ore", "Wheat", "Wood", "Wool" };
  private Trade trade;
  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_trade_offer, null);

    mainActivity = (MainActivity) getActivity();

    if (getArguments() != null) {
      trade = gson.fromJson(getArguments().getString(TRADE), Trade.class);
      trader = mainActivity.storage.getOpponent(trade.player);
      TextView playerName = (TextView) view.findViewById(R.id.playerName);
      playerName.setText(trader.name);
      ImageButton playerIcon = (ImageButton) view.findViewById(R.id.playerIcon);
      int colorId = getResources().getIdentifier(trader.color.toLowerCase(),
          "color", mainActivity.getPackageName());
      playerIcon.setBackgroundColor(getResources().getColor(colorId));

      int i = 0;
      // fill fields with correct amounts
      for (String resource : fields) {
        int resIdOffer = getResources().getIdentifier("offer" + resource + "Cnt","id", mainActivity.getPackageName());
        TextView offerAmount = (TextView) view.findViewById(resIdOffer);
        offerAmount.setText(String.valueOf(trade.offer.getQnts()[i]));
        int resIdReq = getResources().getIdentifier("req" + resource + "Cnt","id", mainActivity.getPackageName());
        TextView reqAmount = (TextView) view.findViewById(resIdReq);
        reqAmount.setText(String.valueOf(trade.request.getQnts()[i++]));
      }
    }

    accBtn = (Button) view.findViewById(R.id.accBtn);
    cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
    decBtn = (Button) view.findViewById(R.id.decBtn);
    tradeStatus = (CardView) view.findViewById(R.id.tradeStatus);

    setOnClickListener();

    IntentFilter filter = new IntentFilter();
    filter.addAction(TRD_ACC);
    filter.addAction(TRD_ABORTED);
    LocalBroadcastManager.getInstance(mainActivity.getApplicationContext())
        .registerReceiver(broadcastReceiver, filter);

    return view;
  }

  private void setOnClickListener() {
    accBtn.setOnClickListener((View v) -> {
      sendAnswer(true);
      cancelBtn.setVisibility(View.VISIBLE);
    });
    decBtn.setOnClickListener((View v) -> {
      sendAnswer(false);
    });
    cancelBtn.setOnClickListener((View v) -> {
      cancelBtn.setVisibility(View.GONE);
      sendAnswer(false);
    });
  }

  private void sendAnswer(Boolean answer) {
    // alter gui
    accBtn.setVisibility(View.GONE);
    decBtn.setVisibility(View.GONE);
    tradeStatus.setVisibility(View.VISIBLE);

    // actually send trade request
    // only send necessary info
    trade.offer = null;
    trade.opponent = null;
    trade.player = null;
    trade.request = null;
    trade.accept = answer;
    String reply;
    reply = createJSONString(TRD_RES, trade);
    mainActivity.mService.sendMessage(reply);
  }

  private void setPlayers(String playersJSON) {
    Type type = new TypeToken<Player[]>(){}.getType();
    Player[] allPlayers = gson.fromJson(playersJSON, type);
    int i = 0;
    // copy all non traders in players[]
    for (Player p : allPlayers) {
      if (p.id != trader.id) {
        players[i++] = p;
      }
    }
  }

  private void updateTradeStatus(int id, boolean accepted) {
    String allPlayers = mainActivity.storage.getAllPlayers();
    setPlayers(allPlayers);

    for (int i = 0; i < players.length; i++) {
      if (players[i] != null && players[i].id != trader.id) {
        // trader is not shown therefor no update for him
        int resIdCon = getResources().getIdentifier(
            "p" + i, "id", mainActivity.getPackageName()
        );
        int resIdNam = getResources().getIdentifier(
            "p"+i+"Name", "id", mainActivity.getPackageName()
        );
        int resIdSta = getResources().getIdentifier(
            "p"+i+"Status", "id", mainActivity.getPackageName()
        );
        LinearLayout playerView = (LinearLayout) view.findViewById(resIdCon);
        playerView.setVisibility(View.VISIBLE);
        TextView playerName = (TextView) view.findViewById(resIdNam);
        playerName.setText(players[i].name);
        ImageButton playerStatus = (ImageButton) view.findViewById(resIdSta);
        int colorId = getResources().getIdentifier(
            players[i].color.toLowerCase(), "color", mainActivity.getPackageName()
        );
        playerStatus.setBackgroundColor(getResources().getColor(colorId));
        if (players[i].id == id) {
          if (accepted) {
            playerStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_success));
          } else {
            playerStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_decline));
          }
        }
      }
    }
  }
}
