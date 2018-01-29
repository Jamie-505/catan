package de.lmu.settlebattle.catanclient.player;

import static de.lmu.settlebattle.catanclient.utils.Constants.CLAY;
import static de.lmu.settlebattle.catanclient.utils.Constants.ORE;
import static de.lmu.settlebattle.catanclient.utils.Constants.UNKNOWN;
import static de.lmu.settlebattle.catanclient.utils.Constants.WHEAT;
import static de.lmu.settlebattle.catanclient.utils.Constants.WOOD;
import static de.lmu.settlebattle.catanclient.utils.Constants.WOOL;

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

  @SerializedName(UNKNOWN)
  private int unknownCnt;

  public RawMaterialOverview(int[] qnts) {
    this.woodCount = qnts[0];
    this.clayCount = qnts[1];
    this.woolCount = qnts[2];
    this.wheatCount = qnts[3];
    this.oreCount = qnts[4];
  }

  public RawMaterialOverview(String type, int initAmount) {
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

  public RawMaterialOverview(String... types) {
    for (String type : types) {
      switch (type) {
        case WOOD:
          this.woodCount++;
          break;
        case CLAY:
          this.clayCount++;
          break;
        case WOOL:
          this.woolCount++;
          break;
        case WHEAT:
          this.wheatCount++;
          break;
        case ORE:
          this.oreCount++;
          break;
        case UNKNOWN:
          this.unknownCnt++;
          break;
      }
    }
  }

  public int getClayCount() { return clayCount; }

  public int getOreCount() { return oreCount; }

  public int getWheatCount() { return wheatCount; }

  public int getWoodCount() { return woodCount; }

  public int getWoolCount() { return woolCount; }

  public int[] getQnts() {
    return new int[] { woodCount, clayCount, woolCount, wheatCount, oreCount, unknownCnt};
  }

  public int getTotalAmnt() {
    int size = 0;
    for (int q : getQnts()) {
      size += q;
    }
    return size;
  }

  @Override
  public String toString() {
    String retVal = "";
    int[] qnts = getQnts();
    for (int i = 0; i < qnts.length; i++) {
      if (qnts[i] > 0) {
        switch (i) {
          case 0:
            retVal += qnts[i] + " Holz, ";
            break;
          case 1:
            retVal += qnts[i] + " Lehm, ";
            break;
          case 2:
            retVal += qnts[i] + " Wolle, ";
            break;
          case 3:
            retVal += qnts[i] + " Getreide, ";
            break;
          case 4:
            retVal += qnts[i] + " Erz, ";
            break;
          case 5:
            retVal += qnts[i] + " unbekannte Rohstoffe, ";
            break;
        }
      }
    }
    if (retVal.isEmpty()) {
      retVal = "keine Rohstoffe";
    } else {
      // remove trailing white space and ,
      retVal = retVal.trim();
      retVal = retVal.substring(0, retVal.length()-1);
    }
    return retVal;
  }
}
