package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Message {

  public class Player {
    public int id;

    @SerializedName(PLAYER_NAME)
    public String name;

    @SerializedName(PLAYER_STATE)
    public String status;

    @SerializedName(PLAYER_COLOR)
    String color;
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
