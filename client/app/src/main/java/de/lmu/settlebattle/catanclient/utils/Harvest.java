package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIALS;

import com.google.gson.annotations.SerializedName;
import de.lmu.settlebattle.catanclient.player.RawMaterialOverview;

public class Harvest {

  @SerializedName(PLAYER)
  private Integer playerId;

  @SerializedName(RAW_MATERIALS)
  private RawMaterialOverview rawMaterials;

  public Integer getPlayerId() {
    return playerId;
  }

  public RawMaterialOverview getRawMaterials() {
    return rawMaterials;
  }
}
