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

    public void move(Location location) {
        this.location = location;
    }

    public void robPlayer(Player robber, Player victim) {
        //TODO
    }

    public void robAll() {
        //TODO
    }
}
