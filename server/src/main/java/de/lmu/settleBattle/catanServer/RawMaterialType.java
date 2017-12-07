package de.lmu.settleBattle.catanServer;
public enum RawMaterialType {

    CLAY,
    ORE,
    WOOD,
    WOOL,
    WEAT,
    NONE;

    public String toString(RawMaterialType bt){
        switch (this){
            case CLAY:
                return "Clay";
            case ORE:
                return "Ore";
            case WOOD:
                return "Wood";
            case WOOL:
                return "Wool";
            case WEAT:
                return "Weat";
            case NONE:
                return "None";
            default: throw new AssertionError( "Unknown building type" + this);

        }
    }
}
