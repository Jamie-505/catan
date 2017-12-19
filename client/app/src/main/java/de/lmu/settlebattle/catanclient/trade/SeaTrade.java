package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.OFFER;
import static de.lmu.settlebattle.catanclient.utils.Constants.REQUEST;

import com.google.gson.annotations.SerializedName;

public class SeaTrade {

  public SeaTrade(RawMaterialOverview offer, RawMaterialOverview request) {
    this.offer = offer;
    this.request = request;
  }

  @SerializedName(OFFER)
  private RawMaterialOverview offer;

  @SerializedName(REQUEST)
  private RawMaterialOverview request;
}
