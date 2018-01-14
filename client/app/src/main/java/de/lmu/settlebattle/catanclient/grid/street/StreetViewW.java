package de.lmu.settlebattle.catanclient.grid.street;

import android.content.Context;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Location;

public class StreetViewW extends StreetView {

  public StreetViewW(Context context, Hex hex) {
    super(context, hex, Orientation.VERTICAL);
  }

  @Override
  protected void setLocation(Hex hex) {
    location[0] = new Location(hex.getX() -1, hex.getY());
    location[1] = new Location(hex.getX(), hex.getY());
  }
}
