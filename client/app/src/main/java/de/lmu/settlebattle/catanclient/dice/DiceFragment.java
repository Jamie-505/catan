package de.lmu.settlebattle.catanclient.dice;

import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_THROW;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;


public class DiceFragment extends MainActivityFragment {

  private Dice dice;
  private Gson gson = new Gson();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    if (getArguments() != null) {
      dice = gson.fromJson(getArguments().getString(DICE_THROW), Dice.class);
    }
    // Inflate the layout for this fragment
    View fragmentView = inflater.inflate(R.layout.fragment_dice_result, container, false);

    TextView diceSumTxt = (TextView) fragmentView.findViewById(R.id.diceSumTxt);
    diceSumTxt.setText(String.valueOf(dice.getSum()));

    ImageView dice1Img = (ImageView) fragmentView.findViewById(R.id.dice1Image);
    ImageView dice2Img = (ImageView) fragmentView.findViewById(R.id.dice2Image);
    String dice1 = "dice" + dice.getDice()[0];
    String dice2 = "dice" + dice.getDice()[1];
    int resId1 = getResources().getIdentifier(dice1, "drawable", getActivity().getPackageName());
    int resId2 = getResources().getIdentifier(dice2, "drawable", getActivity().getPackageName());
    dice1Img.setImageResource(resId1);
    dice2Img.setImageResource(resId2);

    Button okBtn = (Button) fragmentView.findViewById(R.id.diceOkBtn);
    okBtn.setOnClickListener((View v) -> getActivity().onBackPressed());

    return fragmentView;
  }
}
