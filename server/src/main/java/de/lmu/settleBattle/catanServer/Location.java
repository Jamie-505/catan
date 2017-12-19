package de.lmu.settleBattle.catanServer;
import com.google.gson.annotations.Expose;

public class Location extends JSONStringBuilder {

    @Expose
    public int x;

    @Expose
    public int y;

    public Location (){
        this.x = 0;
        this.y = 0;
    }

    public Location (int x, int y){
        if (!isValidLoc(x,y))
            throw new IllegalArgumentException("The location coordinates do not have the required values.");

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

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public int getSum() {
        return this.getX() + this.getY();
    }

    public boolean isValidLoc(int x, int y) {
        boolean ret = true;

        if (Math.abs(x) > 3 || Math.abs(y) > 3)
            ret = false;

        if (Math.abs(x+y) > 3)
            ret = false;

        return ret;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Location) {
            Location loc = (Location) object;
            return loc.x == this.x && loc.y == this.y;
        }
        return false;
    }

    public boolean compare(Location loc){
        return (this.x == loc.x && this.y == loc.y);
    }
}
