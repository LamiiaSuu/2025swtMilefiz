package de.hs_rm.de.milefiz.game.lobby;

public class LobbyJoinException extends Exception {

    public LobbyJoinException() {
    }

    public LobbyJoinException(String message) {
        super(message);
    }

    public LobbyJoinException(String message, Throwable cause) {
        super(message, cause);
    }

}
