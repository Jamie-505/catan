package de.lmu.settleBattle.catanServer;

public class CatanException extends Exception {

    private boolean sendToClient;
    private Object errorObject;

    public CatanException() {
        super();
        this.sendToClient = false;
        this.errorObject = null;
    }

    public CatanException(boolean sendToClient) {
        super();
        this.sendToClient = sendToClient;
    }

    public CatanException(String message) {
        super(message);
    }

    public CatanException(String message, boolean sendToClient) {
        super(message);
        this.sendToClient = sendToClient;
    }

    public CatanException(String message, boolean sendToClient, Object errorObject) {
        this(message, sendToClient);
        this.errorObject = errorObject;
    }

    public boolean sendToClient() {
        return this.sendToClient;
    }

    public Object getErrorObject() { return this.errorObject; }

    public static void throwNotYourTurnException(int id) throws CatanException{
        throw new CatanException(String.format("Spieler %s ist am Zug", id), true);
    }
}

