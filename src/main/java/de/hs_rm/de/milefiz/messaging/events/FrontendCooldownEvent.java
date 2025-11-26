package de.hs_rm.de.milefiz.messaging.events;

/**
 * Cooldown-Event für das Frontend.
 * type kann sein: COOLDOWN_STARTED, COOLDOWN_UPDATE, COOLDOWN_READY
 */
public record FrontendCooldownEvent(String type, long remainingMs) implements FrontendEvent {

    public FrontendCooldownEvent(String type, long remainingMs) {
        this.type = type;
        this.remainingMs = remainingMs;
    }
}
