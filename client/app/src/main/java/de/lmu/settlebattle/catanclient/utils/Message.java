package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Map;

public class Message {

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

  class Board {}

  class Building {
    @SerializedName(OWNER)
    int owner;

    @SerializedName(TYPE)
    String type;

    @SerializedName(PLACE)
    private ArrayList[] locations;
  }

  class Error {
    @SerializedName(MESSAGE)
    String Message;
  }
}
