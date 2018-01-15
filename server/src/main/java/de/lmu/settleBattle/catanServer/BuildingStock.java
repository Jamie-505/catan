package de.lmu.settleBattle.catanServer;

public class BuildingStock {
    private int cityCount;
    private int settlementCount;
    private int roadCount;

    public BuildingStock() {
        this.cityCount = 4;
        this.settlementCount = 5;
        this.roadCount = 15;
    }

    public int getCount(BuildingType type) {
        int count = 0;
        switch (type) {
            case SETTLEMENT:
                count = settlementCount;
                break;
            case ROAD:
                count = roadCount;
                break;
            case CITY:
                count = cityCount;
                break;
        }
        return count;
    }

    public void increaseSettlement() {
        this.settlementCount += 1;
    }

    public boolean decrease(BuildingType type) {
        if (getCount(type) < 1)
            return false;

        switch (type) {
            case CITY:
                cityCount--;
                break;
            case SETTLEMENT:
                settlementCount --;
                break;
            case ROAD:
                roadCount--;
                break;
        }
        return true;
    }
}
