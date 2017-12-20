package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TradeRequest extends JSONStringBuilder {
    private static int idCounter = 0;

    //region property change listener
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
    //endregion

    //region members and constructors
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
    private int executedWith;

    public TradeRequest(RawMaterialOverview offer, RawMaterialOverview request) {
        this.id = idCounter++;
        this.offer = offer;
        this.request = request;
        this.acceptedBy = -1;
        this.accepted = false;
        this.cancelled = false;
        this.executed = false;
    }
    //endregion

    //region properties
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

    public boolean accept(int playerId) {

        if (this.cancelled || this.executed) return false;

        boolean fire = (!this.accepted || this.acceptedBy!=playerId) ? true:false;

        this.accepted = true;
        this.acceptedBy = playerId;

        if (fire)
            changes.firePropertyChange("TR Accept", false, this);

        return true;
    }

    public void cancel() {
        boolean fire = (!this.cancelled) ? true:false;
        this.cancelled = true;

        if (fire)
            changes.firePropertyChange("TR Cancel", false, this);
    }
    public boolean isCancelled() { return this.cancelled; }

    public boolean canBeExecuted(int fellowPlayerId) {
        boolean ret = true;

        if (this.cancelled || this.executed ||
                this.acceptedBy != fellowPlayerId || !this.accepted) ret = false;

        return ret;
    }

    public void execute(int fellowPlayerId) {
        if (!this.accepted)
            throw new IllegalArgumentException("Cannot execute trade if no one has accepted it yet.");

        if (executed || cancelled)
            throw new IllegalArgumentException("Already executed or cancelled");

        this.acceptedBy = fellowPlayerId;
        this.executed = true;
        this.executedWith = fellowPlayerId;

        changes.firePropertyChange("TR Execute", false, this);
    }

    public boolean isExecuted() { return this.executed; }

    public boolean isXTo1(int offerCount) {
        if (this.getOffer().hasOnly(offerCount)) {
            if (this.getRequest().hasOnly(1)) {
                return true;
            }
        }

        return false;
    }
    //endregion
}