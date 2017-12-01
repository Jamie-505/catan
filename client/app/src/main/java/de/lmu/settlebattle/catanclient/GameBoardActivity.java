//package de.lmu.settlebattle.catanclient;
//
//
//import java.util.Random;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//
///**
// * Hier kommt alles zusammen in eine funktionierende View
// */
//
//public class GameBoardActivity extends Activity
//   implements GameBoardTouchListener
//{
//
//    static public final int NumSquaresOnGridSide = 5;
//    static public final int NumSquaresOnViewSide = 5;
//    static public final int NumRedBlueTypes = 7;
//
//
//    static private Random mRandomObject = new Random (System.currentTimeMillis ());
//
//
//
//   private int [][] pGrid;
//
//
//
//public int [][] getGrid ()
//{
//   //if (pGrid == null) {}
//   return pGrid;
//} // end getGrid
//
///**
// * Wert für Grid Prop
// *
// * @param newValue int [][]
// */
//
//public void setGrid (int [][] newValue)
//{
//   pGrid = newValue;
//}
//
//
///* Property GridView */
///**
// * Hier ist die Variable für die GridView
// */
//
//   private GameBoardView pGridView;
//
///**
// * Hol dir GridView
// *
// * @return GameBoardView
// */
//
//public GameBoardView getGridView ()
//{
//   //if (pGridView == null) {}
//   return pGridView;
//} // end getGridView
//
///**
// * Wert für Grid View Prob setzen
// *
// * @param newValue GameBoardView
// */
//
//public void setGridView (GameBoardView newValue)
//{
//   pGridView = newValue;
//} // end
//
//
///**
// */
//// Methoden
//
///**
// * onCreate
// */
//
//@Override public void onCreate(Bundle savedInstanceState) {
//	System.out.println("hello_world gameBoardActivityOnCreate");
//    super.onCreate(savedInstanceState);
//
//    setContentView(R.layout.activity_main); //
//
//    setupMyGrid (NumSquaresOnGridSide);
//
//    GameBoardView gv = (GameBoardView) findViewById (R.id.boardview);
//    if (gv != null) {
//       setGridView (gv);
//
//       gv.setNumSquaresAlongCanvas (NumSquaresOnGridSide);
//       gv.setNumSquaresAlongSide (NumSquaresOnViewSide);
//       gv.updateGrid (getGrid ());
//       gv.setTouchListener (this);
//    }
//
//}
//
//
///* TODO: Einzelne Klassen für die jeweiligen Tile-Kategorien definieren
// *
// *
// *
// */
//
//
///**
// * Baue eine 2D Grid und assoziere die passende Bitmap
// *
// * @param n int - grid size ist N x N quadrate
// * @return int [] []
// */
//
//
//
//int [] [] randomGridArray (int n){
//   // Feld aufbauen mit 3 Grundtypen
//    int [][] grid = new int [n][n];
//    for (int i = 0; i < n; i++)
//    for (int j = 0; j < n; j++) {
//      grid [i][j] = 0;
//    }
//
//   int half_n = n / 2;
//
//   for (int i = 0; i < n; i++) {
//			int invalid_fields_in_this_row = Math.abs(i - half_n);
//			int valid_fields_in_this_row = n - invalid_fields_in_this_row;
//	   for (int j = 0; j < valid_fields_in_this_row; j++) {
//		   int index = randomInt(1, NumRedBlueTypes - 1);    // wie viele Karten gibt es und welche Bitmap nutze ich?
//		   grid[i][j] = index;
//
//	   }
//   }
//   return grid;}
//
//
///*
//   int [] [] randomGridArray (int n) {
//   // Feld aufbauen mit 3 Grundtypen
//   int [][] grid = new int [n] [n];
//   for (int i = 0; i < n; i++)
//   for (int j = 0; j < n; j++) {
//      int index = randomInt (0, NumRedBlueTypes-1);    // wie viele Karten gibt es und welche Bitmap nutze ich?
//      grid [i][j] = index;
//   }
//   System.out.println("hello_world randomGridArray");
//   return grid;
//}
//*/
///**
// * Gib irgendwas zwischen minVal zu maxVal.
// *
// */
//
///*
//public int randomInt (int minVal, int maxVal) {
//    Random r = mRandomObject;
//      int range = maxVal - minVal;
//      int offset = (int) Math.round (r.nextFloat () * range);
//      return minVal + offset;
//}
// */
//
//
//public int randomInt (int minVal, int maxVal) {
//    Random r = mRandomObject;
//      int range = maxVal - minVal;
//      int offset = (int) Math.round (r.nextFloat () * range);
//      return minVal + offset;
//}
//
///**
// * Baue ein 2 Dimensionales Array aus tiles
// * Aktuell ist die verteilung noch random
// * TODO: X/Y Implementierung, wie sollen die positionen allokalisiert werden?
// * Grid wird gespeichert im setGrid.
// *
// *
// * @param n int - grid size is N x N squares
// * @param maxValue
// * @return void
// */
//
//public void setupMyGrid (int n)
//{
//	Log.d("Dario", "hello");
//   int [][] grid = randomGridArray (n);
//	 // int [][] grid = finalCatanGrid (n);
//   setGrid (grid);
//}
//
///**
// */
//// GameBoardTouchListener methods
//
///**
// * Ab hier geht es los mit TouchListener
// * Bedenke: Die x/y location wird es mit touchlistener ende mitgeteilt, nicht gleich zu fanfand
// */
//
//public void onTouchDown () {
//}
//
///**
// * Touch up = touchlistenerende
// *
// * @param downX int - x wert von getippten square
// * @param downY int - y wert von getippten square
// * @param upX int - x ^ (wenn keine veränderung)
// * @param upY int - y ^
// * @return void
// */
//
//public void onTouchUp (int downX, int downY, int upX, int upY) {
//   GameBoardView gv = getGridView ();
//   if (gv == null) return;
//
//   boolean isSelected = gv.isSelected (upX, upY);
//   gv.clearSelections ();
//   if (!isSelected) gv.toggleSelection (upX, upY);
//   gv.invalidate ();
//
//   if (AppConfig.DEBUG)
//      Log.d (Constants.LOG_NAME, "onTouchUp x: " + upX + " y: " + upY + " selected: " + isSelected);
//
//}
//
///**
// * Diese Methode wird ausgeführt, wenn es einen Long Touch gibt auf dem Square. Aktuell gibt dieser nur eine neuen Wert für die Klasse zurück.
// *
// *
// * Values are between 0 and NumSquaresAlongCanvas-1.
// *
// *
// */
//
//public void onLongTouchUp (int downX, int downY, int upX, int upY) {
//   GameBoardView gv = getGridView ();
//   if (gv == null) return;
//
//   int oldValue = gv.gridValue (upX, upY);
//   int newValue = oldValue + 1;
//   if (newValue >= NumRedBlueTypes) newValue = 0;
//   gv.setGridValue (upX, upY, newValue);
//   gv.invalidate ();
//
//   if (AppConfig.DEBUG)
//      Log.d (Constants.LOG_NAME, "onLongTouchUp x: " + upX + " y: " + upY + " old value: " + oldValue);
//
//}
//
//}
