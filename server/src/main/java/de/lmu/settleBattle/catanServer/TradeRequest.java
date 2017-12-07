package de.lmu.settleBattle.catanServer;
public class TradeRequest {
    private static int idCounter = 0;

    private int id;
    private RawMaterialType offer;
    private int offerCount;
    private RawMaterialType request;
    private int requestCount;

    private boolean accepted;
    private int acceptedBy;

    public TradeRequest(RawMaterialType offer, RawMaterialType request) {
        this.id = idCounter++;
        this.offer = offer;
        this.request = request;
        this.offerCount = 1;
        this.requestCount = 1;
        this.acceptedBy = -1;
        this.accepted = false;
    }

    public TradeRequest(RawMaterialType offer, int offerCount,
                        RawMaterialType request, int requestCount){
        this(offer, request);
        this.requestCount = requestCount;
        this.offerCount = offerCount;
    }

    public int getId() {
        return this.id;
    }

    public RawMaterialType getOffer() {
        return this.offer;
    }
    public int getOfferCount() {
        return this.offerCount;
    }
    public int getRequestCount() {
        return this.requestCount;
    }
    public RawMaterialType getRequest() {
        return this.request;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public int getAcceptedBy() {
        return this.acceptedBy;
    }

    public void setOffer(RawMaterialType offer) {
        this.offer = offer;
    }

    public void setOfferCount(int offerCount) {
        this.offerCount = offerCount;
    }

    public void setRequest(RawMaterialType request) {
        this.request = request;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public void accept(int playerId) {
        this.accepted = true;
        this.acceptedBy = playerId;
    }
}