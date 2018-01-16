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
        try {
            this.location = new Location(0, 0);
        } catch (CatanException ex) {
            ex.printStackTrace();
        }
    }

    public Robber(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public void move(Location newLoc) throws CatanException {
        if (!isValidNewLocation(newLoc))
            throw new CatanException(String.format("Der Räuber kann nicht nach %s versetzt werden", newLoc.toString()), true);

        this.location = newLoc;
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
    public boolean robPlayer(Player current, Player target) throws CatanException {
        List<RawMaterialType> types = target.getRawMaterialTypes();
        if (types.size() == 0) return false;

        Random random = new Random();
        RawMaterialType type = types.get(random.nextInt(types.size()));

        RawMaterialOverview overview = new RawMaterialOverview(type, 1);

        target.decreaseRawMaterials(overview);

        //do not send status update when increasing here because the player has
        //still status "Räuber versetzen" and client should not react to this
        current.increaseRawMaterials(overview, false);

        return true;
    }

    public void robPlayer(Player player) throws CatanException {

        int toWithdraw = player.hasToExtractCards() ? player.getRawMaterialCount() / 2 : 0;
        for (int i = toWithdraw; i > 0; i--) {
            player.rawMaterialDeck.withdrawRandomCard();
        }
    }

    /**
     * player should have specified which cards he wants to extract if he has more
     * than 7 raw materials, then robber takes care of this
     * @param player player who extracts cards
     * @param overview cards to extract
     * @throws CatanException if player has not enought raw materials
     */
    public void robPlayer(Player player, RawMaterialOverview overview) throws CatanException {
        if (player == null || !player.canAfford(overview))
            throw new CatanException("Du besitzt die abgegebenen Rohstoffe nicht.", true);

        //do not send status update because then the player would have the status
        //EXTRACT_CARDS_DUE_TO_ROBBER again and the game would not continue
        player.decreaseRawMaterials(overview, false);
    }
}
