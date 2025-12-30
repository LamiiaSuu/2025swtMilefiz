package de.hs_rm.de.milefiz.game.service;

public class BoardValidateException extends RuntimeException {

    public BoardValidateException() {
    }

    public BoardValidateException(String message) {
        super(message);
    }

    public BoardValidateException(String message, Throwable cause) {
        super(message, cause);
    }
}
