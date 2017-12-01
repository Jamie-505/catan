package de.lmu.settlebattle.catanclient;


/**
 * Diese Klasse ist der Listener, wo auf Touch Ereignisse ausschau gehalten wird und die in 3 Kategorien geteilt werden. 
 * TODO: Gibt es hier von Android vor-definierte Methoden, die performanter sind?
 */

public interface GameBoardTouchListener {

/**
 * Callen wenn touchdown passiert
 *
 */

public void onTouchDown ();

/**
 * Wenn touchdown wird diese methode aufgerufen
 *
 * 
 * @param downX int - x value of the down action square
 * @param downY int - y value of the down action square
 * @param upX int - x value of the up action square
 * @param upY int - y value of the up action square
 * @return void
 */

public void onTouchUp (int downX, int downY, int upX, int upY);

/**
 * Rufe die Androidtouchdown zeit auf, rufe die methode auf wenn zeitüberschreitung
 *
 * @param downX int - x value of the down action square
 * @param downY int - y value of the down action square
 * @param upX int - x value of the up action square
 * @param upY int - y value of the up action square
 * @return void
 */

public void onLongTouchUp (int downX, int downY, int upX, int upY);

}
