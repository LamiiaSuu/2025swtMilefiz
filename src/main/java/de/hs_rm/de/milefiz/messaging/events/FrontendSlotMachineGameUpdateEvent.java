package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Color;

public record FrontendSlotMachineGameUpdateEvent(
        String type,
        UUID duelId,
        UUID player1Id,
        UUID player2Id,
        Color resultPlayer1,
        Color resultPlayer2,
        Color resultComp,
        UUID winner,
        boolean jackpot,
        int jackpotEnergy,
        boolean finished) implements FrontendEvent {

    public FrontendSlotMachineGameUpdateEvent(
            UUID duelId,
            UUID player1Id,
            UUID player2Id,
            Color resultPlayer1,
            Color resultPlayer2,
            Color resultComp,
            UUID winner,
            boolean jackpot,
            int jackpotEnergy,
            boolean finished) {
        this(EventType.SLOT_MACHINE_GAME_UPDATE.name(), duelId, player1Id, player2Id, resultPlayer1, resultPlayer2,
                resultComp, winner,
                jackpot, jackpotEnergy,
                finished);
    }
}
