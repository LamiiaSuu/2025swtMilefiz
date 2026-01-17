package de.hs_rm.de.milefiz.messaging.events;

import java.util.UUID;

/**
 * Frontend-Event für Updates des Mini-Spiels "MathGame"/"Kopfrechnen".
 *
 * Dieses Event wird immer dann gesendet, wenn der aktuelle Term angefragt wird
 * oder das Mini-Spiel abgeschlossen wird.
 * 
 * Im Frontend werden der aktuelle Term, beziehungsweise nach Abschluss der Minispiels die richtige Lösung und
 * die Eingabe des anderen Spielers agezeigt.
 *
 * @param type Typ des Events (MATH_GAME_UPDATE)
 * @param duelId ID des Duells, zu dem dieses Mini-Spiel gehört
 * @param player1 Spieler 1
 * @param player2 Spieler 2
 * @param p1Value Eingabewert des Spieler 1 oder null
 * @param p2Value Eingabewert des Spieler 2 oder null
 * @param termRepresentation Representation des Terms als String
 * @param termValue Lösung des Terms oder null
 * @param winner Gewinner des Mini-Spiels (oder null, solange unentschieden oder noch nicht abgeschlossen)
 * @param finished Ob das Mini-Spiel abgeschlossen ist
 */
public record FrontendMathGameUpdateEvent(
        String type,
        UUID duelId,
        UUID player1,
        UUID player2,
        Integer p1Value,
        Integer p2Value,
        String termRepresentation,
        Integer termValue,
        UUID winner,
        boolean finished
) implements FrontendEvent {

    public FrontendMathGameUpdateEvent(
            UUID duelId,
            UUID player1,
            UUID player2,
            Integer p1Value,
            Integer p2Value,
            String termRepresentation,
            Integer termValue,
            UUID winner,
            boolean finished
    ) {
        this(EventType.MATH_GAME_UPDATE.name(), duelId, player1, player2, p1Value, p2Value, termRepresentation, termValue, winner, finished);
    }
}
