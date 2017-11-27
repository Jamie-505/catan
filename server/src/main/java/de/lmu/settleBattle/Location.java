package de.lmu.settleBattle;

public class Location {
    public int x;
    public int y;

    public Location (){
        this.x = 0;
        this.y = 0;
    }

    public Location (int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */

    public int getX(){
        return x;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public int getY(){
        return y;
    }
}
