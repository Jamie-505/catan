package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

public class TradeRequest extends JSONStringBuilder {
    private static int idCounter = 0;

    @Expose
    @SerializedName(Constants.TRADE_ID)
    private int id;

    @Expose
    @SerializedName(Constants.PLAYER)
    private int playerId;

    @Expose
    @SerializedName(Constants.OFFER)
    private RawMaterialOverview offer;

    @Expose
    @SerializedName(Constants.REQUEST)
    private RawMaterialOverview request;

    @Expose
    @SerializedName(Constants.ACCEPT)
    private boolean accepted;

    @Expose
    @SerializedName(Constants.FELLOW_PLAYER)
    private int acceptedBy;

    private boolean cancelled;

    private boolean executed;

    public TradeRequest(RawMaterialOverview offer, RawMaterialOverview request) {
        this.id = idCounter++;
        this.offer = offer;
        this.request = request;
        this.acceptedBy = -1;
        this.accepted = false;
        this.cancelled = false;
        this.executed = false;
    }

    public int getId() {
        return this.id;
    }

    public RawMaterialOverview getOffer() {
        return this.offer;
    }
    public RawMaterialOverview getRequest() {
        return this.request;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getAcceptedBy() {
        return this.acceptedBy;
    }

    public void setOffer(RawMaterialOverview offer) { this.offer = offer; }
    public void setRequest(RawMaterialOverview request) {
        this.request = request;
    }

    public void accept(int playerId) {
        this.accepted = true;
        this.acceptedBy = playerId;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() { return this.cancelled; }

    public void execute() {
        if (!this.accepted)
            throw new IllegalArgumentException("Cannot execute trade if no one has accepted it yet.");

        this.executed = true;
    }

    public boolean isExecuted() { return this.executed; }
}