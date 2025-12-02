package de.hs_rm.de.milefiz.game.lobby;

public class PlayerNotFoundException extends Exception {
    
    public PlayerNotFoundException() {
    }

    public PlayerNotFoundException(String message) {
        super(message);
    }

    public PlayerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
