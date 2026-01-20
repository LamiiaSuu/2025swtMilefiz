package de.hs_rm.de.milefiz.messaging.events;

import java.util.List;
import java.util.UUID;

/**
 * FrontendEvent zum Updaten des Colorbrain Minigames
 */
public record FrontendColorbrainGameUpdateEvent(
        String type,
        UUID duelId,
        UUID player1,
        UUID player2,
        String player1Pick,
        String player2Pick,
        List<String> selectedColors,
        UUID winner,
        boolean finished

) implements FrontendEvent {

    public FrontendColorbrainGameUpdateEvent(
            UUID duelId,
            UUID player1,
            UUID player2,
            String player1Pick,
            String player2Pick,
            List<String> selectedColors,
            UUID winner,
            boolean finished) {
        this(EventType.COLORBRAIN_GAME_UPDATE.name(), duelId, player1, player2, player1Pick, player2Pick, selectedColors, winner, finished);
    }
}
