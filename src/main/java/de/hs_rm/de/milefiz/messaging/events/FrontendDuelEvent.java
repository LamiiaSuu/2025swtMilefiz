package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.MiniGame;

/**
 * Frontend-Event, das ausgelöst wird, wenn zwei Meeples auf demselben Feld
 * aufeinandertreffen und dadurch ein Duell entsteht.
 *
 * Dieses Ereignis informiert das Frontend darüber, dass der Zug eines
 * Spielers zu einer direkten Konfrontation zwischen zwei Meeples geführt hat.
 *
 * Auslöser für dieses Event sind:
 * - ein Spieler landet mit seinem Meeple auf dem Feld eines anderen Spielers
 *
 * Das Event enthält die IDs beider beteiligten Meeples sowie das Zielfeld
 * und die verbleibenden Bewegungen nach der Ausführung des Zuges.
 *
 * @param type           Typ des Events (DUEL)
 * @param firstMeepleId  ID des ersten beteiligten Meeple (der aktive Spieler)
 * @param secondMeepleId ID des zweiten beteiligten Meeple (der gegnerische Spieler)
 * @param targetField    ID des Feldes, auf dem das Duell stattfindet
 * @param remainingMoves Anzahl der verbleibenden Züge nach der Bewegung
 *
 * Author: Maximilian Ressel / Robert Bothfeld
 */
public record FrontendDuelEvent(
        String type,
        UUID duelId,
        UUID playerId,
        UUID firstMeepleId,
        UUID secondMeepleId,
        UUID targetField,
        int remainingMoves,
        int miniGameId,
        String miniGameName,
        String miniGameType
) implements FrontendEvent {

    public FrontendDuelEvent(
            UUID duelId,
            UUID playerId,
            UUID firstMeepleId,
            UUID secondMeepleId,
            UUID targetField,
            int remainingMoves,
            MiniGame game
    ) {
        this(
                EventType.DUEL.name(),
                duelId,
                playerId,
                firstMeepleId,
                secondMeepleId,
                targetField,
                remainingMoves,
                game.getId(),
                game.getName(),
                game.getClass().getSimpleName()
        );
    }
}


