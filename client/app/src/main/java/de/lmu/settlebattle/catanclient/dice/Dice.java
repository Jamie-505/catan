package de.lmu.settlebattle.catanclient.dice;

import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_THROW;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;

public class Dice {

  @SerializedName(PLAYER)
  private int player;

  @SerializedName(DICE_THROW)
  private int[] dice;

  public int[] getDice() {
    return dice;
  }

  public int getSum() {
    return Arrays.stream(dice).sum();
  }
}
