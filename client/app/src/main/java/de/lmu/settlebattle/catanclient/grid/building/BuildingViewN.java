package de.lmu.settlebattle.catanclient.grid.building;

import android.content.Context;
import android.util.Log;
import android.view.View;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.utils.Location;

public class BuildingViewN extends BuildingView {

  public BuildingViewN(Context context, Hex hex) {
    super(context, hex);
  }

  @Override
  protected void setLocation(Hex hex) {
    location[0] = new Location(hex.getX() - 1, hex.getY() + 1);
    location[1] = new Location(hex.getX(), hex.getY()+1);
    location[2] = new Location(hex.getX(), hex.getY());
  }

}