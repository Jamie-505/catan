package de.lmu.settlebattle.catanclient.grid;

import static de.lmu.settlebattle.catanclient.utils.Constants.BOARD;
import static de.lmu.settlebattle.catanclient.utils.Constants.CLAY;
import static de.lmu.settlebattle.catanclient.utils.Constants.FIELDS;
import static de.lmu.settlebattle.catanclient.utils.Constants.ORE;
import static de.lmu.settlebattle.catanclient.utils.Constants.WHEAT;
import static de.lmu.settlebattle.catanclient.utils.Constants.WOOD;
import static de.lmu.settlebattle.catanclient.utils.Constants.WOOL;

import android.util.Log;
import com.google.gson.annotations.SerializedName;
import de.lmu.settlebattle.catanclient.R;
import java.util.HashMap;

public class Board {

  private HashMap<String, Integer> typeImgMap = new HashMap<String, Integer>();

  public Board(Hex[] fields) {
    this.fields = fields;
    typeImgMap.put(WHEAT, R.drawable.wheat_field);
    typeImgMap.put(ORE, R.drawable.ore_field);
    typeImgMap.put(WOOL, R.drawable.sheep_field);
    typeImgMap.put(WOOD, R.drawable.woods_field);
    typeImgMap.put(CLAY, R.drawable.clay_field);
    typeImgMap.put("DESERT", R.drawable.desert_field);
    setImages(fields);
  }

  private void setImages(Hex[] fields) {
    for (Hex field : fields) {
      try {
        int y = field.loc.y;
        int x = field.loc.x;
        if (y >= 0) {
          hexMap[y + 3][x + 3] = typeImgMap.get(field.type);
        } else {
          hexMap[y + 3][x + y + 3] = typeImgMap.get(field.type);
        }
      } catch (NullPointerException e) {
        Log.i(BOARD, "Water fields have no location");
      }
    }
  }

  public Object[][] hexMap = new Integer[][] {
      {R.drawable.hafen_0_m3, R.drawable.hafen_1_m3, R.drawable.hafen_2_m3, R.drawable.hafen_3_m3},
      {R.drawable.hafen_m1_m2,R.drawable.clay_field, R.drawable.clay_field, R.drawable.ore_field, R.drawable.hafen_3_m2},
      {R.drawable.hafen_m2_m1, R.drawable.tile_layers, R.drawable.tile_layers, R.drawable.tile_layers, R.drawable.sheep_field, R.drawable.hafen_3_m1},
      {R.drawable.hafen_m3_0, R.drawable.tile_layers, R.drawable.tile_layers, R.drawable.desert_field, R.drawable.wheat_field, R.drawable.ore_field, R.drawable.hafen_3_0},
      {R.drawable.hafen_m3_1, R.drawable.ore_field, R.drawable.sheep_field, R.drawable.ore_field, R.drawable.woods_field, R.drawable.hafen_2_1},
      {R.drawable.hafen_m3_2, R.drawable.clay_field, R.drawable.woods_field, R.drawable.wheat_field, R.drawable.hafen_1_2},
      {R.drawable.hafen_m3_3, R.drawable.hafen_m2_3, R.drawable.hafen_m1_3, R.drawable.hafen_0_3}
  };

  @SerializedName(FIELDS)
  public Hex[] fields;

}
