package de.hs_rm.de.milefiz.game.lobby;

public class LobbyNotFoundException extends Exception {

    public LobbyNotFoundException() {
    }

    public LobbyNotFoundException(String message) {
        super(message);
    }

    public LobbyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
