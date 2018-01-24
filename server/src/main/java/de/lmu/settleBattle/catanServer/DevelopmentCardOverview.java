package de.lmu.settleBattle.catanServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import static de.lmu.settleBattle.catanServer.Constants.*;

public class DevelopmentCardOverview extends JSONStringBuilder {

    @Expose
    @SerializedName(Constants.KNIGHT)
    private int knight;

    @Expose
    @SerializedName(Constants.ROAD_CONSTRUCTION)
    private int roadConstruction;

    @Expose
    @SerializedName(Constants.MONOPOLE)
    private int monopole;

    @Expose
    @SerializedName(Constants.INVENTION)
    private int invention;

    @Expose
    @SerializedName(Constants.VICTORY_PT)
    private int victoryPoint;

    //region Constructors



    public DevelopmentCardOverview(){
        this.knight = 14;
        this.invention = 2;
        this.monopole = 2;
        this.roadConstruction = 2;
        this.victoryPoint = 5;
    }

    public DevelopmentCardOverview(int initCount) {
        this.knight = this.invention = this.monopole =
                this.roadConstruction = this.victoryPoint = initCount;
    }

    public DevelopmentCardOverview(int knightCount, int roadConstructionCount, int monopoleCount,
                                   int inventionCount, int victoryPointCount) {
        this.knight = knightCount;
        this.invention = inventionCount;
        this.monopole = monopoleCount;
        this.roadConstruction = roadConstructionCount;
        this.victoryPoint = victoryPointCount;
    }
    //endregion

    //region increase
    public void increase(DevCardType type, int i) throws CatanException {
        //the method increase must not decrease an amount
        if (i < 0) throw new CatanException(String.format("i ist negativ und somit ungültig. (Wert: %s)", i), false);

        switch(type) {
            case INVENTION:
                invention += i;
                break;
            case KNIGHT:
                knight += i;
                break;
            case ROAD_CONSTRUCTION:
                roadConstruction += i;
                break;
            case MONOPOLE:
                monopole += i;
                break;
            case VICTORY_POINT:
                victoryPoint += i;
                break;
        }
    }
    //endregion

    //region decrease
    public void decrease(DevCardType type, int i)  throws CatanException {
        if (i < 0) throw new CatanException(String.format("i ist negativ und somit ungültig. (Wert: %s)", i), false);

        switch(type) {
            case INVENTION:
                if (i > invention) throw new CatanException(String.format(INVALID_DEV_CARDS_DECREASE, i, invention), false);
                invention -= i;
                break;
            case KNIGHT:
                if (i > knight) throw new CatanException(String.format(INVALID_DEV_CARDS_DECREASE, i, knight), false);
                knight -= i;
                break;
            case ROAD_CONSTRUCTION:
                if (i > roadConstruction) throw new CatanException(String.format(INVALID_DEV_CARDS_DECREASE, i, roadConstruction), false);
                roadConstruction -= i;
                break;
            case MONOPOLE:
                if (i > monopole) throw new CatanException(String.format(INVALID_DEV_CARDS_DECREASE, i, monopole), false);
                monopole -= i;
                break;
            case VICTORY_POINT:
                if (i > victoryPoint) throw new CatanException(String.format(INVALID_DEV_CARDS_DECREASE, i, victoryPoint), false);
                victoryPoint -= i;
                break;
        }
    }
    //endregion

    public boolean hasMonopoleCard() {
        return this.monopole > 0;
    }

    public DevCardType withdrawRandomCard() throws CatanException {

        List<DevCardType> cards = this.getWithdrawableTypes();

        if (cards.size() < 1) throw new CatanException("Es sind keine Entwicklungskarten mehr vorhanden.", true);

        Random random = new Random();
        DevCardType card = cards.get(random.nextInt(cards.size()));
        decrease(card, 1);
        return card;
    }

    public boolean hasInventionCard() {
        return this.invention > 0;
    }

    public int getTotalCount() {
        return invention+knight+monopole+roadConstruction+victoryPoint;
    }

    public boolean hasKnightCard() {
        return this.knight > 0;
    }

    public boolean hasRoadConstructionCard() {
        return this.roadConstruction > 0;
    }

    public List<DevCardType> getWithdrawableTypes() {
        List<DevCardType> types = new ArrayList<>();

        if (knight > 0) types.add(DevCardType.KNIGHT);
        if (invention > 0) types.add(DevCardType.INVENTION);
        if (monopole > 0) types.add(DevCardType.MONOPOLE);
        if (roadConstruction > 0) types.add(DevCardType.ROAD_CONSTRUCTION);
        if (victoryPoint > 0) types.add(DevCardType.VICTORY_POINT);

        return types;
    }

    @Override
    public String toJSONString_Unknown(){
        JSONObject json = new JSONObject();
        json.put(Constants.UNKNOWN, getTotalCount());
        return json.toString();
    }

    /**
     * determines costs to buy a development card
     * @return RawMaterialOverview representing costs
     */
    public static RawMaterialOverview getCosts() {
        return new RawMaterialOverview(0,1,0,1,1);
    }

}

