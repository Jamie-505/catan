package de.lmu.settlebattle.catanclient.grid;

import static de.lmu.settlebattle.catanclient.utils.Constants.OWNER;
import static de.lmu.settlebattle.catanclient.utils.Constants.LOCATION;
import static de.lmu.settlebattle.catanclient.utils.Constants.TYPE;
import static de.lmu.settlebattle.catanclient.utils.Constants.VIEW_ID;

import com.google.gson.annotations.SerializedName;
import de.lmu.settlebattle.catanclient.utils.Constants;

public class Construction {

  public enum ConstructionType {
    @SerializedName(Constants.CITY)
    CITY,

    @SerializedName(Constants.SETTLEMENT)
    SETTLEMENT,

    @SerializedName(Constants.STREET)
    STREET,

    @SerializedName("")
    NONE
  }

  public Construction(int owner, ConstructionType type, Location[] locations, String viewId) {
    this.owner = owner;
    this.type = type;
    this.locations = locations;
    this.viewId = viewId;
  }

  @SerializedName(OWNER)
  public int owner;

  @SerializedName(TYPE)
  public ConstructionType type;

  @SerializedName(LOCATION)
  public Location[] locations;

  @SerializedName(VIEW_ID)
  public String viewId;
}
