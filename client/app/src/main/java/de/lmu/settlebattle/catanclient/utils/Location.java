package de.lmu.settlebattle.catanclient.utils;

import com.google.gson.annotations.SerializedName;

public class Location {

  public Location(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @SerializedName("x")
  public int x;

  @SerializedName("y")
  public int y;
}
