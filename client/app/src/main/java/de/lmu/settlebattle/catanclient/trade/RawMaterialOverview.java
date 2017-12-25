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

  @SerializedName(WHEAT)
  private int wheatCount;

  @SerializedName(ORE)
  private int oreCount;

  //region Constructors
  public RawMaterialOverview() {
    this.clayCount = this.oreCount = this.woodCount =
        this.woolCount = this.wheatCount = 0;
  }

  public RawMaterialOverview(int initAmount) {
    this.clayCount = this.oreCount = this.woodCount =
        this.woolCount = this.wheatCount = initAmount;
  }

  public RawMaterialOverview(int clay, int ore, int wheat, int wood, int wool) {
    this.clayCount = clay;
    this.oreCount = ore;
    this.woodCount = wood;
    this.woolCount = wool;
    this.wheatCount = wheat;
  }

  public RawMaterialOverview(int[] qnts) {
    this.clayCount = qnts[0];
    this.oreCount = qnts[1];
    this.wheatCount = qnts[2];
    this.woodCount = qnts[3];
    this.woolCount = qnts[4];
  }

  public RawMaterialOverview(String type, int initAmount) {
    this();
    switch (type) {
      case WHEAT:
        wheatCount = initAmount;
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

  public int[] getQnts() {
    return new int[] { this.clayCount, this.oreCount, this.wheatCount, this.woodCount, this.woolCount };
  }
}
