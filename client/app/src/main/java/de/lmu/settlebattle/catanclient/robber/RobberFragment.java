package de.lmu.settlebattle.catanclient.robber;

import static de.lmu.settlebattle.catanclient.utils.Constants.OWNER;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.lmu.settlebattle.catanclient.MainActivity;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.player.Player;
import java.util.ArrayList;

public class RobberFragment extends MainActivityFragment {

  private Player[] targets;
  private MainActivity mainActivity;
  private View fragmentView;
  private ArrayList<Integer> targetIds;

  public RobberFragment() {

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    fragmentView = inflater.inflate(R.layout.fragment_robber, container, false);
    // Inflate the layout for this fragment
    mainActivity = (MainActivity) getActivity();

    if (getArguments() != null) {
      targetIds = getArguments().getIntegerArrayList(OWNER);
      if (targetIds != null) {
        targets = new Player[targetIds.size()];
      }
    }
    return fragmentView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    setPlayers(targetIds);
  }


  private void setPlayers(ArrayList<Integer> ids) {
    for (Integer id : ids) {
      targets[ids.indexOf(id)] = mainActivity.storage.getOpponent(id);
    }
    try {

      for (int i = 0; i < targets.length; i++) {
        Player target = targets[i];
        if (target != null) {
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
          playerName.setText(target.name);
          ImageButton playerStatus = fragmentView.findViewById(resIdSta);
          int colorId = getResources().getIdentifier(
              target.color.toLowerCase(), "color", mainActivity.getPackageName()
          );
          playerStatus.setBackgroundColor(getResources().getColor(colorId));
          playerStatus.setOnClickListener((View v) -> {
            mainActivity.sendRobberMsg(target.id);
            mainActivity.onBackPressed();
          });
          playerStatus.setClickable(true);
          }
        }
      } catch (Exception e) {
      if (isAdded()) {
        mainActivity = (MainActivity) getActivity();
        mainActivity.onBackPressed();
        mainActivity.displayMessage("Hoppla, da ist wohl was schief gelaufen");
      }
    }
  }
}
