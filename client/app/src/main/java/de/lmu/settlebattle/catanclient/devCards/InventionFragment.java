package de.lmu.settlebattle.catanclient.devCards;

import static de.lmu.settlebattle.catanclient.utils.Constants.INVENTION;
import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIALS;
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


public class InventionFragment extends MainActivityFragment {

  private RadioButton rawMat1;
  private RadioButton rawMat2;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View fragmentView = inflater
        .inflate(R.layout.fragment_invention, container,false);
    // Inflate the layout for this fragment


    RadioGroup selection1 = fragmentView.findViewById(R.id.first_selection);
    RadioGroup selection2 = fragmentView.findViewById(R.id.second_selection);

    selection1.setOnCheckedChangeListener((group, checkedId) -> {
      if (rawMat1 != null) {
        rawMat1.setBackgroundColor(Color.TRANSPARENT);
      }
      rawMat1 = group.findViewById(checkedId);
      rawMat1.setBackgroundColor(Color.LTGRAY);
    });

    selection2.setOnCheckedChangeListener((group, checkedId) -> {
      if (rawMat2 != null) {
        rawMat2.setBackgroundColor(Color.TRANSPARENT);
      }
      rawMat2 = group.findViewById(checkedId);
      rawMat2.setBackgroundColor(Color.LTGRAY);
    });

    Button sendBtn = fragmentView.findViewById(R.id.send_btn);
    sendBtn.setOnClickListener((View v) -> sendInvention());

    return fragmentView;
  }

  private void sendInvention() {
    String type1 = rawMat1.getText().toString();
    String type2 = rawMat2.getText().toString();
    RawMaterialOverview rawMats = new RawMaterialOverview(type1, type2);
    Invention invention = new Invention(rawMats, null);
    String inventionMsg = createJSONString(INVENTION, invention);
    fragHandler.sendMsgToServer(inventionMsg);
    fragHandler.closeFragment(this);
  }
}
