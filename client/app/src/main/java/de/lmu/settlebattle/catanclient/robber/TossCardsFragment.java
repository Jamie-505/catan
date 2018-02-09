package de.lmu.settlebattle.catanclient.robber;

import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIALS;
import static de.lmu.settlebattle.catanclient.utils.Constants.TOSS_CARDS;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.gson.Gson;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.NumberPicker;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.RawMaterialOverview;
import java.util.Locale;


public class TossCardsFragment extends MainActivityFragment {

  private Button okBtn;
  private Gson gson = new Gson();
  private int numPickCnt;
  private int tossCardCnt;
  private NumberPicker[] numPicks = new NumberPicker[5];
  private RawMaterialOverview rawMat;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    if (getArguments() != null) {
      rawMat = gson.fromJson(getArguments().getString(RAW_MATERIALS),
              RawMaterialOverview.class);
    }
    // Inflate the layout for this fragment
    View fragmentView = inflater.inflate(R.layout.fragment_toss_cards, container, false);

    // toss half of the cards (floored)
    tossCardCnt = rawMat.getTotalAmnt()/2;

    NumberPicker woodCnt = fragmentView.findViewById(R.id.woodCnt);
    woodCnt.setMax(rawMat.getWoodCount());
    woodCnt.setUnit(1);
    numPicks[0] = woodCnt;

    NumberPicker clayCnt = fragmentView.findViewById(R.id.clayCnt);
    clayCnt.setMax(rawMat.getClayCount());
    clayCnt.setUnit(1);
    numPicks[1] = clayCnt;

    NumberPicker woolCnt = fragmentView.findViewById(R.id.woolCnt);
    woolCnt.setMax(rawMat.getWoolCount());
    woolCnt.setUnit(1);
    numPicks[2] = woolCnt;

    NumberPicker wheatCnt = fragmentView.findViewById(R.id.wheatCnt);
    wheatCnt.setMax(rawMat.getWheatCount());
    wheatCnt.setUnit(1);
    numPicks[3] = wheatCnt;

    NumberPicker oreCnt = fragmentView.findViewById(R.id.oreCnt);
    oreCnt.setMax(rawMat.getOreCount());
    oreCnt.setUnit(1);
    numPicks[4] = oreCnt;

    okBtn = fragmentView.findViewById(R.id.okBtn);
    okBtn.setOnClickListener((View v) -> {
      RawMaterialOverview tossedCards = createTossCardObj();
      fragHandler.sendMsgToServer(createJSONString(TOSS_CARDS, tossedCards));
      fragHandler.closeFragment(this);
    });

    return fragmentView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    for (NumberPicker np : numPicks) {
      np.setLimitExceededListener((limit, exceededValue) -> {
        String message = String.format(Locale.GERMAN,
            "Du kannst den Wert nicht auf %d setzen", exceededValue);
        fragHandler.displayFragMsg(message);
      });
      np.setValueChangedListener((value, action) -> {
        if (action == ActionEnum.DECREMENT) {
          numPickCnt--;
        } else if (action == ActionEnum.INCREMENT) {
          numPickCnt++;
        }
        if (numPickCnt == tossCardCnt) {
          okBtn.setEnabled(true);
          okBtn.setAlpha(1f);
          okBtn.setClickable(true);
        } else {
          okBtn.setEnabled(false);
          okBtn.setAlpha(.8f);
          okBtn.setClickable(false);
        }
      });
    }
  }

  private RawMaterialOverview createTossCardObj() {
    int[] values = new int[5];
    int i = 0;
    for (NumberPicker np : numPicks) {
      values[i++] = np.getValue();
    }
    return new RawMaterialOverview(values);
  }
}
