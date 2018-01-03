package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.OWNER;
import static de.lmu.settlebattle.catanclient.utils.Constants.LOCATION;
import static de.lmu.settlebattle.catanclient.utils.Constants.TYPE;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

class Building {
  @SerializedName(OWNER)
  int owner;

  @SerializedName(TYPE)
  String type;

  @SerializedName(LOCATION)
  private ArrayList[] locations;
}
