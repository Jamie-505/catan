package de.lmu.settleBattle;
public enum DevCardType {
    INVENTION,
    KNIGHT,
    ROAD_CONSTRUCTION,
    MONOPOLE,
    VICTORY_POINT;


    private static String KNIGHT_DESC = "";
    private static String ROAD_CONSTRUCTION_DESC = "";
    private static String INVENTION_DESC = "";
    private static String MONOPOLE_DESC = "";
    private static String VICTORY_POINT_DESC = "";

    public String description(DevCardType type){
        switch (type){
            case INVENTION:
                return INVENTION_DESC;
            case KNIGHT:
                return KNIGHT_DESC;
            case ROAD_CONSTRUCTION:
                return ROAD_CONSTRUCTION_DESC;
            case MONOPOLE:
                return MONOPOLE_DESC;
            case VICTORY_POINT:
                return VICTORY_POINT_DESC;
            default: throw new AssertionError( "Unknown card" + this);

        }
    }
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
