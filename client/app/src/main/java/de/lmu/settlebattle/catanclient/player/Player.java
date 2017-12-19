package de.lmu.settlebattle.catanclient.player;

import static de.lmu.settlebattle.catanclient.utils.Constants.ARMY;
import static de.lmu.settlebattle.catanclient.utils.Constants.DEV_CARDS;
import static de.lmu.settlebattle.catanclient.utils.Constants.LONGEST_RD;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_COLOR;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_NAME;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_STATE;
import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIALS;
import static de.lmu.settlebattle.catanclient.utils.Constants.VICTORY_PTS;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Player {
  public int id;

  @SerializedName(PLAYER_NAME)
  public String name;

  @SerializedName(PLAYER_STATE)
  public String status;

  @SerializedName(PLAYER_COLOR)
  public String color;

  @SerializedName(ARMY)
  public int army;

  @SerializedName(LONGEST_RD)
  public boolean longestRd;

  @SerializedName(VICTORY_PTS)
  public int victoryPts;

  @SerializedName(RAW_MATERIALS)
  public Map<String, Integer> rawMaterials;

  @SerializedName(DEV_CARDS)
  public Map<String, Integer> devCards;
}
