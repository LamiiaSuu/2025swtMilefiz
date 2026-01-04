package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event für Updates des Mini-Spiels "DiceGame".
 *
 * Dieses Event wird immer dann gesendet, wenn:
 * <ul>
 *     <li>ein Spieler würfelt</li>
 *     <li>oder das Mini-Spiel abgeschlossen wird</li>
 * </ul>
 *
 * Es enthält alle relevanten Informationen, damit das Frontend:
 * <ul>
 *     <li>die gewürfelten Werte anzeigen</li>
 *     <li>den aktuellen Status des Mini-Spiels darstellen</li>
 *     <li>den Gewinner markieren, sobald das Spiel beendet ist</li>
 * </ul>
 *
 * @param type     Typ des Events (DICE_GAME_UPDATE)
 * @param duelId   ID des Duells, zu dem dieses Mini-Spiel gehört
 * @param p1       Spieler 1
 * @param p2       Spieler 2
 * @param rollP1   Würfelergebnis von Spieler 1 (kann null sein, wenn noch nicht gewürfelt)
 * @param rollP2   Würfelergebnis von Spieler 2 (kann null sein, wenn noch nicht gewürfelt)
 * @param winner   Gewinner des Mini-Spiels (null, solange unentschieden oder noch nicht abgeschlossen)
 * @param finished Ob das Mini-Spiel abgeschlossen ist
 */
public record FrontendDiceGameUpdateEvent(
        String type,
        UUID duelId,
        UUID p1,
        UUID p2,
        Integer rollP1,
        Integer rollP2,
        UUID winner,
        boolean finished
) implements FrontendEvent {

    public FrontendDiceGameUpdateEvent(
            UUID duelId,
            UUID p1,
            UUID p2,
            Integer rollP1,
            Integer rollP2,
            UUID winner,
            boolean finished
    ) {
        this(EventType.DICE_GAME_UPDATE.name(), duelId, p1, p2, rollP1, rollP2, winner, finished);
    }
}
