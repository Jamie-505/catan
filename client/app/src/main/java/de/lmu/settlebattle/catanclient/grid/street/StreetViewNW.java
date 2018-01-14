package de.lmu.settlebattle.catanclient.grid.street;

import android.content.Context;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Location;

public class StreetViewNW extends StreetView {

  public StreetViewNW(Context context, Hex hex) {
    super(context, hex, Orientation.TILTED);
    this.setScaleX(-1);
  }

  @Override
  protected void setLocation(Hex hex) {
    location[0] = new Location(hex.getX() -1, hex.getY()+1);
    location[1] = new Location(hex.getX(), hex.getY());
  }
}
