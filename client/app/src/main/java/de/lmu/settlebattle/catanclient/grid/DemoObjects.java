package de.lmu.settlebattle.catanclient.grid;

import de.lmu.settlebattle.catanclient.R;

/**
 * Demo contents.
 */
public class DemoObjects {
    //int size = mRadius*2+1;
    //squareMap = new Integer[size][size];
    public static Object[][] squareMap = new Integer[][] {
        {0, 0, 0, 0},
        {0, R.drawable.woods_highlighted, R.drawable.clay_highlighted, R.drawable.iron_highlighted, 0},
        {0, R.drawable.wheat_highlighted, R.drawable.iron_highlighted, R.drawable.woods_highlighted, R.drawable.sheep_highlighted, 0},
        {0, R.drawable.woods_highlighted, R.drawable.clay_highlighted, R.drawable.desert_highlighted, R.drawable.wheat_highlighted, R.drawable.iron_highlighted, 0},
        {0, R.drawable.iron_highlighted, R.drawable.sheep_highlighted, R.drawable.iron_highlighted, R.drawable.woods_highlighted, 0},
        {0, R.drawable.clay_highlighted, R.drawable.woods_highlighted, R.drawable.wheat_highlighted, 0},
        {0, 0, 0, 0}
    };
}
