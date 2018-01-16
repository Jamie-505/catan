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

    public void decrease(BuildingType type) throws CatanException {
        if (getCount(type) < 1) throw new CatanException(String.format("Es ist keine %s mehr vorhanden.", type.toString()),
                true);

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
    }
}
