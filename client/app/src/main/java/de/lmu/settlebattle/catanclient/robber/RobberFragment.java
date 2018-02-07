package de.lmu.settlebattle.catanclient.robber;

import static de.lmu.settlebattle.catanclient.utils.Constants.LOCATION;
import static de.lmu.settlebattle.catanclient.utils.Constants.OWNER;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER_TO;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.MainActivity;
import de.lmu.settlebattle.catanclient.MainActivityFragment;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.grid.Location;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;
import java.util.ArrayList;

public class RobberFragment extends MainActivityFragment {

  private Gson gson = new Gson();
  private Player[] targets;
  private MainActivity mainActivity;
  private View fragmentView;
  private ArrayList<Integer> targetIds;
  private Location robberLoc;

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
      robberLoc = gson.fromJson(getArguments().getString(LOCATION), Location.class);
    }
    return fragmentView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    setPlayers(targetIds);
  }


  private void setPlayers(ArrayList<Integer> ids) {
    for (Integer id : ids) {
      targets[ids.indexOf(id)] = Storage.getPlayer(id);
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
            fragHandler.sendRobberMsgToServer(target.id);
            fragHandler.closeFragment(this);
          });
          playerStatus.setClickable(true);
          }
        }
      } catch (Exception e) {
      if (isAdded()) {
        fragHandler.displayFragMsg("Hoppla, da ist wohl was schief gelaufen");
        fragHandler.popBackstack(this);
      }
    }
  }
}
