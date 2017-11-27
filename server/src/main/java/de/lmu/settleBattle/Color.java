package de.lmu.settleBattle;

public enum Color {

    ORANGE,
    BLUE,
    WHITE,
    RED;

    public String toString(Color color){
        switch (this){
            case ORANGE:
                return "Orange";
            case BLUE:
                return "Blue";
            case WHITE:
                return "White";
            case RED:
                return "Red";
            default: throw new AssertionError( "Unknown color" + this);

        }
    }
}
