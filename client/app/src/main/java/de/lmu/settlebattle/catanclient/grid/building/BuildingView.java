package de.lmu.settlebattle.catanclient.grid.building;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageView;
import de.lmu.settlebattle.catanclient.grid.Construction.ConstructionType;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Location;
import java.util.Arrays;

public abstract class BuildingView extends AppCompatImageView{

  public Location[] location = new Location[3];
  public ConstructionType type;

  public BuildingView(Context context, Hex hex) {
    super(context);
    this.setBackgroundColor(Color.TRANSPARENT);
    this.setElevation(10);
    this.type = ConstructionType.SETTLEMENT;
    setLocation(hex);
    this.setTag(createTag(location));
  }

  public static String createTag(Location[] locations) {
    Arrays.sort(locations);
    String tag = "";
    for (Location l : locations) {
      tag = tag + l.getX() + l.getY();
    }
    return tag;
  }

  public static String createTag(Location l) {
    return String.valueOf(l.getX()) + String.valueOf(l.getY());
  }

  protected abstract void setLocation(Hex hex);

  public void setType(ConstructionType type) {
    this.type = type;
  }
}
