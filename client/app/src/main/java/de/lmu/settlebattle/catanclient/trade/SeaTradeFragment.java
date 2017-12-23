package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.SEA_TRADE;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import de.lmu.settlebattle.catanclient.MainActivity;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;


public class SeaTradeFragment extends MainActivityFragment {

  MainActivity mainActivity;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View fragmentView = inflater.inflate(R.layout.fragment_sea_trade, container, false);
    // Inflate the layout for this fragment
    mainActivity = (MainActivity) getActivity();

    Button sendTradeBtn = (Button) fragmentView.findViewById(R.id.sendTradeBtn);

    Spinner offerSpinner = (Spinner) fragmentView.findViewById(R.id.offeredResource);
    Spinner requestSpinner = (Spinner) fragmentView.findViewById(R.id.requestedResources);
    // Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
				R.array.raw_materials, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
		offerSpinner.setAdapter(adapter);
    requestSpinner.setAdapter(adapter);

    sendTradeBtn.setOnClickListener((View v) -> {
      String offer = offerSpinner.getSelectedItem().toString();
      String req = requestSpinner.getSelectedItem().toString();
      sendTradeRequest(offer, req);
    });

    return fragmentView;
  }

  private void sendTradeRequest(String offerRes, String reqRes) {
    RawMaterialOverview offer = new RawMaterialOverview(offerRes, 1);
    RawMaterialOverview req = new RawMaterialOverview(reqRes, 1);
    SeaTrade seaTrade = new SeaTrade(offer, req);

    String tradeMsg = createJSONString(SEA_TRADE, seaTrade);
    mainActivity.mService.sendMessage(tradeMsg);
  }
}
