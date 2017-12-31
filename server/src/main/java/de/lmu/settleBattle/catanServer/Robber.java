package de.lmu.settleBattle.catanServer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Random;

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

    /**
     * <method name: robPlayer>
     * <description: the player choose a card from one of the other players who have sLocs where the robber moved >
     * <preconditions: number 7 is rolled>
     * <postconditions: player gets a card from another players>
     */
    public boolean robPlayer(Player current, Player target) {
        List<RawMaterialType> types = target.getRawMaterialTypes();
        if (types.size() == 0) return false;

        Random random = new Random();
        RawMaterialType type = types.get(random.nextInt(types.size()));

        RawMaterialOverview overview = new RawMaterialOverview(type, 1);
        target.decreaseRawMaterials(overview);
        current.increaseRawMaterials(overview);

        return true;
    }

    public void robPlayer(Player player) {

        int toWithdraw = player.hasToExtractCards() ? player.getRawMaterialCount() / 2 : 0;
        for (int i = toWithdraw; i > 0; i--) {
            player.rawMaterialDeck.withdrawRandomCard();
        }
    }

    public boolean robPlayer(Player player, RawMaterialOverview overview) throws IllegalArgumentException {
        if (player == null || !player.canAfford(overview)) return false;

        player.decreaseRawMaterials(overview);
        return true;
    }
}
