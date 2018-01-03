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
    typeImgMap.put(WHEAT, R.drawable.wheat_highlighted);
    typeImgMap.put(ORE, R.drawable.iron_highlighted);
    typeImgMap.put(WOOL, R.drawable.sheep_highlighted);
    typeImgMap.put(WOOD, R.drawable.woods_highlighted);
    typeImgMap.put(CLAY, R.drawable.clay_highlighted);
    typeImgMap.put("DESERT", R.drawable.desert_highlighted);
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
      // TODO: put in havens
//      {0, R.drawable.water, 0, R.drawable.water},
//      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.wheat_highlighted, 0},
//      {0, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.water},
//      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, 0},
//      {0, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.water},
//      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, 0},
//      {0, R.drawable.water, 0, R.drawable.water},
      {R.drawable.water, R.drawable.water, R.drawable.water, R.drawable.water},
      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.water},
      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.water},
      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.water},
      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.water},
      {R.drawable.water, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.desert_highlighted, R.drawable.water},
      {R.drawable.water, R.drawable.water, R.drawable.water, R.drawable.water},
  };

  @SerializedName(FIELDS)
  public Hex[] fields;

}
