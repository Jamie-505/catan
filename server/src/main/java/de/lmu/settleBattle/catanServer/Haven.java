package de.lmu.settleBattle.catanServer;
public class Haven {

    private Location[] locations;
    private RawMaterialType harvest;

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */

    public Location[] getLocations() {
        return locations;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */

    public RawMaterialType getHarvest() {
        return harvest;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */
    public RawMaterialType trade  (RawMaterialType [] rm){
        //TODO
        return null;
    }

    public RawMaterialType trade (TradeRequest tr) {
        if (this.isValidTradeRequest(tr)) {
            //TODO
        }
        return null;
    }

    /**
     *<method name: isValidTradeRequest
     *<description: returns true if the trade request is valid, false if not
     *<preconditions: none>
     *<postconditions: none>
     */
    private boolean isValidTradeRequest(TradeRequest tr){

        // a haven can only give one raw material
        if (tr.getRequestCount() != 1)
            return false;

        switch(harvest) {

            case NONE:
                // if this is not a special trading haven then you have to give away 3 raw materials of the
                // same type to receive one arbitrary raw material
                if(tr.getOfferCount() == 3)
                    return true;

            case WOOL: case WOOD: case WEAT: case ORE: case CLAY:
                // if this is a special trading haven then you have to give away 3 raw materials of the
                // same type to receive one raw material of type of the harvest
                // if another raw material type is requested than the harvest type then the
                // trade request is not valid
                if (tr.getOfferCount() == 2 &&
                        tr.getRequest().equals(harvest))
                    return true;
        }

        return false;
    }
}
