package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Color;

public record FrontendEinarmigerBanditGameUpdateEvent(
        String type,
        UUID duelId,
        UUID p1,
        UUID p2,
        Color resultP1,
        Color resultP2,
        Color resultComp,
        UUID winner,
        boolean jackpot,
        int jackpotEnergy,
        boolean finished) implements FrontendEvent {

    public FrontendEinarmigerBanditGameUpdateEvent(
            UUID duelId,
            UUID p1,
            UUID p2,
            Color resultP1,
            Color resultP2,
            Color resultComp,
            UUID winner,
            boolean jackpot,
            int jackpotEnergy,
            boolean finished) {
        this(EventType.EINARMIGER_BANDIT_GAME_UPDATE.name(), duelId, p1, p2, resultP1, resultP2, resultComp, winner,
                jackpot, jackpotEnergy,
                finished);
    }
}
