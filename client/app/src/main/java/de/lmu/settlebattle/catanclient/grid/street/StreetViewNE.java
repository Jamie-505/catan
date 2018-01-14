package de.lmu.settlebattle.catanclient.grid.street;

import android.content.Context;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Location;

public class StreetViewNE extends StreetView {

  public StreetViewNE(Context context, Hex hex) {
    super(context, hex, Orientation.TILTED);
  }

  @Override
  protected void setLocation(Hex hex) {
    location[0] = new Location(hex.getX(), hex.getY()+1);
    location[1] = new Location(hex.getX(), hex.getY());
  }
}
