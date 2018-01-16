package de.lmu.settlebattle.catanclient.player;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;
import com.google.gson.annotations.SerializedName;

public class DevCardOverview {

  @SerializedName(INVENTION)
  private int invention;

  @SerializedName(KNIGHT)
  private  int knight;

  @SerializedName(MONOPOL)
  private int monopol;

  @SerializedName(RD_CONSTR)
  private int rdConstr;

  @SerializedName(VICTORY_PT)
  private int victPt;

  @SerializedName(UNKNOWN)
  private int unknown;

  public DevCardOverview(int[] qnts) {
    this.invention = qnts[0];
    this.monopol = qnts[1];
    this.rdConstr = qnts[2];
    this.victPt = qnts[4];
    this.knight = qnts[5];
  }

  public int[] getQnts() {
    return new int[] { invention, monopol, rdConstr, victPt, knight, unknown };
  }

  public int getTotalAmnt() {
    int size = 0;
    for (int q : getQnts()) {
      size += q;
    }
    return size;
  }
}
