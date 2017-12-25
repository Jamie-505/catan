package de.lmu.settlebattle.catanclient.trade;

import static de.lmu.settlebattle.catanclient.utils.Constants.ACCEPT;
import static de.lmu.settlebattle.catanclient.utils.Constants.OFFER;
import static de.lmu.settlebattle.catanclient.utils.Constants.OPPONENT;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.REQUEST;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ID;

import com.google.gson.annotations.SerializedName;

public class Trade {

  public Trade(RawMaterialOverview offer, RawMaterialOverview request) {
    this.offer = offer;
    this.request = request;
  }

  @SerializedName(ACCEPT)
  public Boolean accept;

  @SerializedName(TRD_ID)
  public int id;

  @SerializedName(OPPONENT)
  public Integer opponent;

  @SerializedName(PLAYER)
  public Integer player;

  @SerializedName(OFFER)
  public RawMaterialOverview offer;

  @SerializedName(REQUEST)
  public RawMaterialOverview request;
}
