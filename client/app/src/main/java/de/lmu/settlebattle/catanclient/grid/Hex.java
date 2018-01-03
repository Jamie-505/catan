package de.lmu.settlebattle.catanclient.grid;


import static de.lmu.settlebattle.catanclient.utils.Constants.LOCATION;
import static de.lmu.settlebattle.catanclient.utils.Constants.NUMBER;
import static de.lmu.settlebattle.catanclient.utils.Constants.TYPE;

import com.google.gson.annotations.SerializedName;
import de.lmu.settlebattle.catanclient.utils.Location;

/**
 * Non-cube hex coordinates (q, r)
 */
public class Hex {

  @SerializedName(LOCATION)
  Location loc;

  @SerializedName(TYPE)
  public String type;

  @SerializedName(NUMBER)
  public int number;

  public Hex(Location loc) {
    this.loc = loc;
  }

  public Hex (int q, int r) {
     this.loc = new Location(q, r);
  }

  public Hex (float q, float r) {
    float x = q;
    float y = -q-r;
    float z = r;

    int rx = Math.round(x);
    int ry = Math.round(y);
    int rz = Math.round(z);

    float x_diff = Math.abs(rx - x);
    float y_diff = Math.abs(ry - y);
    float z_diff = Math.abs(rz - z);

    if (x_diff > y_diff && x_diff > z_diff)
      rx = -ry-rz;
    else if (y_diff > z_diff)
      ry = -rx-rz;

    this.loc = new Location(rx, ry);
  }

  public String toString() {
    return loc.x + ":" + loc.y;
  }

  public int getX() {
    return loc.x;
  }

  public int getY() {
    return loc.y;
  }
}
