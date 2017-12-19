package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

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

    private boolean cancelled;

    private boolean executed;

    @Expose
    @SerializedName(Constants.FELLOW_PLAYER)
    private Integer executedWith;

    private Map<Integer, Boolean> answers;


    public TradeRequest(RawMaterialOverview offer, RawMaterialOverview request) {
        this.id = idCounter++;
        this.offer = offer;
        this.request = request;
        this.cancelled = false;
        this.executed = false;
        answers = new HashMap<>();
        executedWith = null;
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

    public boolean isAccepted() { return answers.values().contains(true); }
    public boolean getAcceptedBy(int fellowPlayerId) { return answers.get(fellowPlayerId); }
    public boolean accept(boolean accept, int fellowPlayerId) {

        if (this.playerId == fellowPlayerId || this.cancelled || this.executed) return false;

        //set fire = true if something changed
        boolean fire = (!this.answers.containsKey(fellowPlayerId) ||
                this.answers.get(fellowPlayerId) != accept) ? true : false;

        if (fire) {
            this.answers.put(fellowPlayerId, accept);
            changes.firePropertyChange("TR Accept", fellowPlayerId, this);
        }

        return true;
    }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public void setOffer(RawMaterialOverview offer) { this.offer = offer; }
    public void setRequest(RawMaterialOverview request) {
        this.request = request;
    }

    public void cancel() {
        boolean fire = (!this.cancelled) ? true:false;
        this.cancelled = true;

        if (fire)
            changes.firePropertyChange("TR Cancel", false, this);
    }
    public boolean isCancelled() { return this.cancelled; }

    public boolean canBeExecutedBy(int fellowPlayerId) {
        return !this.cancelled && !this.executed && this.answers.containsKey(fellowPlayerId) &&
                this.answers.get(fellowPlayerId);
    }
    public void execute(int fellowPlayerId) {
        if (this.playerId == fellowPlayerId)
            throw new IllegalArgumentException("This trade cannot be executed with the same player who offered it!");

        if (!this.canBeExecutedBy(fellowPlayerId))
            throw new IllegalArgumentException("This trade cannot be executed by fellow player " + fellowPlayerId);

        this.executedWith = fellowPlayerId;
        this.executed = true;

        changes.firePropertyChange("TR Execute", false, this);
    }
    public boolean isExecuted() { return this.executed; }
    public Integer getExecutedWith() { return this.executedWith; }

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