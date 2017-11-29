package de.lmu.settleBattle.catanServer;

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
    public void decrease(DevCardType type, int i) {
        if (i < 0) return;

        switch(type) {
            case INVENTION:
                invention = invention < i ? 0 : invention - i;
                break;
            case KNIGHT:
                knight = knight < i ? 0 : knight - i;
                break;
            case ROAD_CONSTRUCTION:
                roadConstruction = roadConstruction < i ? 0 : roadConstruction - i;
                break;
            case MONOPOLE:
                monopole = monopole < i ? 0 : monopole - i;
                break;
            case VICTORY_POINT:
                victoryPoint = victoryPoint < i ? 0 : victoryPoint - i;
                break;
        }
    }
    //endregion

    public int getTotalCount() {
        return invention+knight+monopole+roadConstruction+victoryPoint;
    }

    @Override
    public String toJSONString_Unknown(){
        JSONObject json = new JSONObject();
        json.put(Constants.UNKNOWN, getTotalCount());
        return json.toString();
    }
}
