package de.lmu.settlebattle.catanclient.devCards;

import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIAL;

import com.google.gson.annotations.SerializedName;

public class Monopole {

  public Monopole(String rawMaterial, Integer playerId) {
    this.rawMaterial = rawMaterial;
    this.playerId = playerId;
  }

  @SerializedName(PLAYER)
  private Integer playerId;

  @SerializedName(RAW_MATERIAL)
  private String rawMaterial;


  public Integer getPlayerId() {
    return playerId;
  }

  public String getRawMaterial() {
    return rawMaterial;
  }
}
