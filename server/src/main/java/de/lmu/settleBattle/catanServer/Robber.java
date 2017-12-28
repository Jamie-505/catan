package de.lmu.settleBattle.catanServer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Robber extends JSONStringBuilder {

    @Expose
    @SerializedName(Constants.PLACE)
    private Location location;

    public Robber() {
        this.location = new Location(0,0);
    }

    public Robber(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public boolean move(Location newLoc) {
        boolean ret = false;

        if (isValidNewLocation(newLoc)) {
            this.location = newLoc;
            ret = true;
        }

        return ret;
    }

    public boolean isValidNewLocation(Location loc) {
        return (!loc.isWaterField() && !this.getLocation().equals(loc));
    }
}
