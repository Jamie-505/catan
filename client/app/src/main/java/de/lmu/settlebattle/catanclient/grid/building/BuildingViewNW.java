package de.lmu.settlebattle.catanclient.grid.building;

import android.content.Context;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Location;

public class BuildingViewNW extends BuildingView {

  public BuildingViewNW(Context context, Hex hex) {
    super(context, hex);
  }

  @Override
  protected void setLocation(Hex hex) {
    location[0] = new Location(hex.getX() - 1, hex.getY());
    location[1] = new Location(hex.getX() - 1, hex.getY() + 1);
    location[2] = new Location(hex.getX(), hex.getY());
  }

}
