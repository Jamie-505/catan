package de.lmu.settlebattle.catanclient.robber;

import static de.lmu.settlebattle.catanclient.utils.Constants.LOCATION;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.TARGET;

import com.google.gson.annotations.SerializedName;
import de.lmu.settlebattle.catanclient.grid.Location;

public class Robber {

  public Robber(Location location, Integer target) {
    this.location = location;
    this.target = target;
  }

  @SerializedName(LOCATION)
  public Location location;

  @SerializedName(PLAYER)
  public Integer player;

  @SerializedName(TARGET)
  public Integer target;
}
