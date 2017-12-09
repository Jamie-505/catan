package de.lmu.settleBattle.catanServer;public enum DevCardType {
    INVENTION,
    KNIGHT,
    ROAD_CONSTRUCTION,
    MONOPOLE,
    VICTORY_POINT;



    public String description(DevCardType type) {
        switch (type) {
            case INVENTION:
                return Constants.INVENTION_DESC;
            case KNIGHT:
                return Constants.KNIGHT_DESC;
            case ROAD_CONSTRUCTION:
                return Constants.ROAD_CONSTRUCTION_DESC;
            case MONOPOLE:
                return Constants.MONOPOLE_DESC;
            case VICTORY_POINT:
                return Constants.VICTORY_POINT_DESC;
            default:
                throw new AssertionError("Unknown card" + this);

        }
    }

    @Override
    public String toString() {
        switch (this) {
            case INVENTION:
                return Constants.INVENTION;
            case KNIGHT:
                return Constants.KNIGHT;
            case ROAD_CONSTRUCTION:
                return Constants.ROAD_CONSTRUCTION;
            case MONOPOLE:
                return Constants.MONOPOLE;
            case VICTORY_POINT:
                return Constants.VICTORY_PT;
            default:
                throw new AssertionError("Unknown card" + this);

        }
    }
}
