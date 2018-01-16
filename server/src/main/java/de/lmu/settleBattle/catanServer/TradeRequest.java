package de.lmu.settleBattle.catanServer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import static de.lmu.settleBattle.catanServer.Constants.*;

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
    @SerializedName(TRADE_ID)
    private int id;

    @Expose
    @SerializedName(PLAYER)
    private int playerId;

    @Expose
    @SerializedName(OFFER)
    private RawMaterialOverview offer;

    @Expose
    @SerializedName(REQUEST)
    private RawMaterialOverview request;

    private boolean cancelled;

    private boolean executed;

    @Expose
    @SerializedName(FELLOW_PLAYER)
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
                this.answers.get(fellowPlayerId) != accept);

        if (fire) {
            this.answers.put(fellowPlayerId, accept);
            changes.firePropertyChange(TDR_REQ_ACC, fellowPlayerId, this);
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
        boolean fire = !this.cancelled;
        this.cancelled = true;

        if (fire)
            changes.firePropertyChange(TDR_REQ_CANCEL, false, this);
    }
    public boolean isCancelled() { return this.cancelled; }

    public boolean canBeExecutedBy(int fellowPlayerId) {
        return !this.cancelled && !this.executed && this.answers.containsKey(fellowPlayerId) &&
                this.answers.get(fellowPlayerId);
    }
    public void execute(int fellowPlayerId) throws CatanException {
        if (this.playerId == fellowPlayerId)
            throw new CatanException(String.format("Der Handel %s kann nicht von dir selbst angenommen werden.", this.id));

        if (!this.canBeExecutedBy(fellowPlayerId))
            throw new CatanException(String.format("Spieler %s kann den Handel nicht ausführen. Hat er angenommen?", fellowPlayerId, this.id));

        this.executedWith = fellowPlayerId;
        this.executed = true;

        changes.firePropertyChange(TDR_REQ_EXE, false, this);
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