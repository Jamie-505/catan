package de.lmu.settleBattle.catanServer;
public enum BuildingType {

    ROAD,
    SETTLEMENT,
    CITY;

    public String toString(BuildingType bt){
        switch (this){
            case ROAD:
                return "Road";
            case SETTLEMENT:
                return "Settlement";
            case CITY:
                return "City";
            default: throw new AssertionError( "Unknown building type" + this);

        }
    }
}
