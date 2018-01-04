package de.lmu.settlebattle.catanclient.utils;

import com.google.gson.annotations.SerializedName;
import java.util.Comparator;

public class Location implements Comparable<Location> {

  public Location(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @SerializedName("x")
  public int x;

  public int getX() {
    return x;
  }

  @SerializedName("y")
  public int y;

  public int getY() {
    return y;
  }

  public String toString() {
    return "x: " + x + ", y: " + y;
  }

  @Override
  public int compareTo(Location other) {
    return Comparator
        .comparing(Location::getX)
        .thenComparing(Location::getY)
        .compare(this, other);
  }
}
