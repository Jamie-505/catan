package de.lmu.settlebattle.catanclient.grid.street;

import static de.lmu.settlebattle.catanclient.grid.building.BuildingView.createTag;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageView;
import de.lmu.settlebattle.catanclient.grid.Construction.ConstructionType;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Location;

public abstract class StreetView extends AppCompatImageView {

  public Location[] location = new Location[2];
  public ConstructionType type;
  private Orientation orientation;

  public StreetView(Context context, Hex hex, Orientation orientation) {
    super(context);
    this.setBackgroundColor(Color.TRANSPARENT);
    this.setElevation(5);
    this.setScaleType(ScaleType.FIT_CENTER);
    this.type = ConstructionType.STREET;
    setLocation(hex);
    this.setTag(createTag(location));
    this.orientation = orientation;
  }

  protected abstract void setLocation(Hex hex);

  public Orientation getOrientation() {
    return orientation;
  }

  public enum Orientation {
    VERTICAL, TILTED
  }
}
