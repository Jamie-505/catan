package de.lmu.settlebattle.catanclient.playerCards;



public class CardItem {
  private boolean mZug; // ist der Spieler am Zug
  private String mColor;
  //private int mTextResource; // Additional information
  private String pName; // Name des Spielers
  private int mSiegpunkResource; // Anzahl der Siegpunkte
  private int mEntwicklungResource; // Anzahl der Entwicklungskarten
  private int mResourcenResource; // Anzahl der Ressourcenkarten
  private boolean mRittermacht; // Ist user Rittermacht?
  private boolean mHandelsstrasse; // Ist user Handelsmacht?


  public CardItem(boolean amZug, String farbe, String pName, int siegpunkte, int entwicklungskarten, int resourcenkarten, boolean rittermacht, boolean handelsstrasse) {
    mZug = amZug;
    mColor = farbe;
    this.pName = pName;
    mSiegpunkResource = siegpunkte;
    mEntwicklungResource = entwicklungskarten;
    mResourcenResource = resourcenkarten;
    mRittermacht = rittermacht;
    mHandelsstrasse = handelsstrasse;
  }

  public boolean getZug() {
    return mZug;
  }
  public String getColor() {
    return mColor;
  }
  public String getName() {
    return pName;
  }

  public int getSP() {
    return mSiegpunkResource;
  }

  public int getEK() {
    return mEntwicklungResource;
  }

  public int getRK() {
    return mResourcenResource;
  }
  public boolean getRM() {
    return mRittermacht;
  }

  public boolean getHS() {
    return mHandelsstrasse;
  }
}

