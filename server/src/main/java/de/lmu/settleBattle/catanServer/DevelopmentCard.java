package de.lmu.settleBattle.catanServer;

public class DevelopmentCard  {

    private DevCardType type;
    private String infoText;

    public DevelopmentCard(){
        type = null;
        infoText = "";
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */

    public DevCardType getType() {
        return type;
    }

    /**
     *<method name: none>
     *<description: none>
     *<preconditions: none>
     *<postconditions: none>
     */

    public String getInfoText() {
        return infoText;
    }

    /**
     * determines costs to buy a development card
     * @return RawMaterialOverview representing costs
     */
    public static RawMaterialOverview getCosts() {
        return new RawMaterialOverview(0,0,1,1,1);
    }
}
