package de.lmu.settleBattle.catanServer;

import java.util.Random;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

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

    //region decrease
    public void increase(DevCardType type, int i) {
        //the method increase must not decrease an amount
        if (i < 0) return;

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
    public void decrease(DevCardType type, int i)  throws Exception {
        if (i < 0) throw new Exception();

        switch(type) {
            case INVENTION:
                if (i > invention) throw new Exception();
                invention -= i;
                break;
            case KNIGHT:
                if (i > knight) throw new Exception();
                knight -= i;
                break;
            case ROAD_CONSTRUCTION:
                if (i > roadConstruction) throw new Exception();
                roadConstruction -= i;
                break;
            case MONOPOLE:
                if (i > monopole) throw new Exception();
                monopole -= i;
                break;
            case VICTORY_POINT:
                if (i > victoryPoint) throw new Exception();
                victoryPoint -= i;
                break;
        }
    }
    //endregion

    public boolean hasMonopoleCard() {
        return this.monopole > 0;
    }

    public DevCardType withdrawRandomCard(){

        DevCardType[] cards = DevCardType.values();
        Random random = new Random();
        DevCardType card = cards[random.nextInt(cards.length)];
        try {
            decrease(card, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return card;

    }

    public boolean hasInventionCard() {
        return this.invention > 0;
    }

    public int getTotalCount() {
        return invention+knight+monopole+roadConstruction+victoryPoint;
    }

    public boolean hasRoadConstructionCard() {
        return this.roadConstruction > 0;
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

