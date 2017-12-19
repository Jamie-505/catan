package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;
import com.google.gson.annotations.SerializedName;

public class RawMaterialOverview {

  @SerializedName(WOOD)
  private int woodCount;

  @SerializedName(CLAY)
  private int clayCount;

  @SerializedName(WOOL)
  private int woolCount;

  @SerializedName(WEAT)
  private int weatCount;

  @SerializedName(ORE)
  private int oreCount;

  //region Constructors
  public RawMaterialOverview() {
    this.clayCount = this.oreCount = this.woodCount =
        this.woolCount = this.weatCount = 0;
  }

  public RawMaterialOverview(int initAmount) {
    this.clayCount = this.oreCount = this.woodCount =
        this.woolCount = this.weatCount = initAmount;
  }

  public RawMaterialOverview(int clay, int ore, int wood, int wool, int weat) {
    this.clayCount = clay;
    this.oreCount = ore;
    this.woodCount = wood;
    this.woolCount = wool;
    this.weatCount = weat;
  }

  public RawMaterialOverview(String type, int initAmount) {
    this();
    switch (type) {
      case WEAT:
        weatCount = initAmount;
        break;
      case CLAY:
        clayCount = initAmount;
        break;
      case ORE:
        oreCount = initAmount;
        break;
      case WOOL:
        woolCount = initAmount;
        break;
      case WOOD:
        woodCount = initAmount;
        break;
    }
  }
}
