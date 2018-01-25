package de.lmu.settlebattle.catanclient.devCards;

import android.widget.RadioButton;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import de.lmu.settlebattle.catanclient.R;

import static de.lmu.settlebattle.catanclient.utils.Constants.MONOPOLE;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

public class MonopoleFragment extends MainActivityFragment {

  private RadioButton rawMat;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View fragmentView = inflater.inflate(R.layout.fragment_monopole, container, false);
    // Inflate the layout for this fragment

    RadioGroup firstSelection = fragmentView.findViewById(R.id.selection);

    firstSelection.setOnCheckedChangeListener((group, checkedId) -> {
      if (rawMat != null) {
        rawMat.setBackgroundColor(Color.TRANSPARENT);
      }
      rawMat = group.findViewById(checkedId);
      rawMat.setBackgroundColor(Color.LTGRAY);
    });

    Button sendBtn = fragmentView.findViewById(R.id.send_btn);
    sendBtn.setOnClickListener((View v) -> requestMonopole());

    return fragmentView;
  }

  private void requestMonopole() {
    Monopole mono = new Monopole(rawMat.getText().toString(), null);

    String monopoleMsg = createJSONString(MONOPOLE, mono);
    fragHandler.sendMsgToServer(monopoleMsg);
    fragHandler.closeFragment(this);
  }
}
