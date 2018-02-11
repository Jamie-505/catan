package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.SEA_TRADE;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.RawMaterialOverview;


public class SeaTradeFragment extends MainActivityFragment {

  private RadioButton offerBtn;
  private RadioButton reqBtn;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View fragmentView = inflater.inflate(R.layout.fragment_sea_trade, container, false);
    // Inflate the layout for this fragment

    RadioGroup offerSelection = fragmentView.findViewById(R.id.trade_offer_selection);
    RadioGroup reqSelection = fragmentView.findViewById(R.id.trade_req_selection);

    offerSelection.setOnCheckedChangeListener((group, checkedId) -> {
      if (offerBtn != null) {
        offerBtn.setBackgroundColor(Color.TRANSPARENT);
      }
      offerBtn = group.findViewById(checkedId);
      offerBtn.setBackgroundColor(Color.LTGRAY);
    });

    reqSelection.setOnCheckedChangeListener((group, checkedId) -> {
      if (reqBtn != null) {
        reqBtn.setBackgroundColor(Color.TRANSPARENT);
      }
      reqBtn = group.findViewById(checkedId);
      reqBtn.setBackgroundColor(Color.LTGRAY);
    });

    Button sendTradeBtn = fragmentView.findViewById(R.id.send_trade_btn);
    sendTradeBtn.setOnClickListener((View v) -> sendTradeRequest());

    return fragmentView;
  }

  private void sendTradeRequest() {
    try {
      RawMaterialOverview offer = new RawMaterialOverview(offerBtn.getText().toString(), 1);
      RawMaterialOverview req = new RawMaterialOverview(reqBtn.getText().toString(), 1);
      Trade seaTrade = new Trade(offer, req);

      String tradeMsg = createJSONString(SEA_TRADE, seaTrade);
      fragHandler.sendMsgToServer(tradeMsg);
      fragHandler.popBackstack(this);
    } catch (NullPointerException e) {
      fragHandler.displayFragMsg("Bitte wähle deine Resourcen für den Tausch aus");
    }
  }
}
