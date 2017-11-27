package de.lmu.settleBattle;
public enum DevCardType {
    INVENTION,
    KNIGHT,
    ROAD_CONSTRUCTION,
    MONOPOLE,
    VICTORY_POINT;


    //this can hold the card description
    public String toString(DevCardType dct){
        switch (this){
            case INVENTION:
                return "Invention";
            case KNIGHT:
                return "Knight";
            case ROAD_CONSTRUCTION:
                return "Road Construction";
            case MONOPOLE:
                return "Monopole";
            case VICTORY_POINT:
                return "Victory Points";
            default: throw new AssertionError( "Unknown card" + this);

        }
    }
}
