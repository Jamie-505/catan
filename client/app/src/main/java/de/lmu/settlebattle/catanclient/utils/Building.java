package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.OWNER;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLACE;
import static de.lmu.settlebattle.catanclient.utils.Constants.TYPE;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

class Building {
  @SerializedName(OWNER)
  int owner;

  @SerializedName(TYPE)
  String type;

  @SerializedName(PLACE)
  private ArrayList[] locations;
}
