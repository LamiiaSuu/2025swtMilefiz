package de.hs_rm.de.milefiz.game.lobby;

public class PlayerHasNoPermissionException extends RuntimeException {

    public PlayerHasNoPermissionException() {
    }

    public PlayerHasNoPermissionException(String message) {
        super(message);
    }

    public PlayerHasNoPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
