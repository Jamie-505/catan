package de.lmu.settlebattle.catanclient.devCards;

import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIALS;

import com.google.gson.annotations.SerializedName;
import de.lmu.settlebattle.catanclient.player.RawMaterialOverview;

public class Invention {

  public Invention(RawMaterialOverview rawMats, Integer playerId) {
    this.rawMats = rawMats;
    this.playerId = playerId;
  }

  @SerializedName(PLAYER)
  private Integer playerId;

  @SerializedName(RAW_MATERIALS)
  private RawMaterialOverview rawMats;

  public RawMaterialOverview getRawMaterials() {
    return rawMats;
  }

  public Integer getPlayerId() {
    return playerId;
  }
}
