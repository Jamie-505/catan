package de.lmu.settleBattle;

public class DevelopmentCardDeck {
    private static int KNIGHT_CNT = 14;
    private static int ROAD_CONSTRUCTION_CNT = 2;
    private static int INVENTION_CNT = 2;
    private static int MONOPOLE_CNT = 2;
    private static int VICTORY_POINT_CNT = 5;


    public int knight;
    public int roadConstruction;
    public int invention;
    public int monopole;
    public int victoryPoins;

    public DevelopmentCardDeck(){
        this.knight = KNIGHT_CNT;
        this.invention = INVENTION_CNT;
        this.monopole = MONOPOLE_CNT;
        this.roadConstruction = ROAD_CONSTRUCTION_CNT;
        this.victoryPoins = VICTORY_POINT_CNT;

    }


    public void increase(DevCardType type, int i) {
        //the method increase must not decrease an amount
        if (i < 0) return;

        switch(type) {
            case INVENTION:
                invention += i;
                break;
            case KNIGHT:
                knight += i;
                break;
            case ROAD_CONSTRUCTION:
                roadConstruction += i;
                break;
            case MONOPOLE:
                monopole += i;
                break;
            case VICTORY_POINT:
                victoryPoins += i;
                break;
        }
    }

    public void decrease(DevCardType type, int i) {
        if (i < 0) return;

        switch(type) {
            case INVENTION:
                invention -= i;
                break;
            case KNIGHT:
                knight -= i;
                break;
            case ROAD_CONSTRUCTION:
                roadConstruction -= i;
                break;
            case MONOPOLE:
                monopole -= i;
                break;
            case VICTORY_POINT:
                victoryPoins -= i;
                break;
        }
    }
}
