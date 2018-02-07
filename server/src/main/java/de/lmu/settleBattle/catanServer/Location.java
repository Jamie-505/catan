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

    public Location (int x, int y) throws CatanException {
        if (!isValidLoc(x,y))
            throw new CatanException(String.format("Die Koordinaten (%s, %s) sind nicht gültig.", x,y), true);

        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public boolean isValidLoc(int x, int y) {
        boolean ret = true;

        if (Math.abs(x) > 3 || Math.abs(y) > 3)
            ret = false;

        if (Math.abs(x+y) > 3)
            ret = false;

        return ret;
    }

    public boolean isWaterField() {
        return Math.abs(x) == 3 || Math.abs(y) == 3 || Math.abs(x+y) == 3;
    }

    public boolean isDesert() { return x==0 && y ==0; }

    public static int getWaterAndDesertCount(Location[] locs) {
        if (locs == null || locs.length == 0) return 0;

        int count = 0;
        for (Location loc : locs) {
            if (loc.isWaterField() || loc.isDesert())
                count++;
        }

        return count;
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

    @Override
    public String toString() {
        return "( x:" + x + ", y:" + y + " )";
    }
}
