package de.hs_rm.de.milefiz.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.services.GameService;
import de.hs_rm.de.milefiz.messaging.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.messaging.MovementCommand;

/**
 * Controller zur Verarbeitung von Bewegungsanfragen über WebSocket/STOMP.
 * Bewegung findet immer nur in "ein Feld" Schritten statt
 *
 * Diese Klasse empfängt Nachrichten vom Frontend über das Topic /app/move,
 * interpretiert den Bewegungsbefehl ({@link MovementCommand}) und führt die entsprechende
 * Spiellogik aus.
 * 
 * Nach erfolgreicher Bewegung wird ein {@link FrontendMoveEvent} an das Topic
 * /topic/move</b> gesendet, um alle Clients über die neue Meeple-Position zu informieren.
 * 
 * Im Fehlerfall (z. B. ungültige Richtung, Sperrfeld, Rückschritt) wird ein
 * {@link FrontendMoveRejectedEvent} gesendet.
 *
 * @author Maximilian Ressel
 * @version 1.0
 */
@Controller
public class MeepleMovementController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Behandelt eingehende Bewegungsbefehle vom Frontend.
     *
     * Diese Methode wird automatisch ausgelöst, wenn ein Client über
     * /app/move einen {@link MovementCommand} sendet. Der Befehl enthält
     * die Meeple-ID und eine Bewegungsrichtung ({@link Direction}).
     * 
     *   - Ermittelt den {@link Player} anhand der WebSocket-Session-ID
     *   - Prüft, ob der Spieler und die Figur ({@link Meeple}) existieren, ggf. Dummy-Spieler anlegen (Testmodus)
     *   - Bestimmt das Ziel-Feld basierend auf der angegebenen {@link Direction}
     *   - Validiert die Bewegung (z. B. keine Barriere, keine Rückkehr auf letztes Feld, kein besetztes Feld).
     *   - Aktualisiert Spielfeld- und Meeple-Zustände (Occupant, CurrentField, LastField)
     *   - Sendet ein {@link FrontendMoveEvent} an alle verbundenen Clients
     * 
     * Im Falle einer ungültigen Bewegung wird stattdessen ein
     * {@link FrontendMoveRejectedEvent} gesendet, z. B. bei:
     * 
     *  - ungültiger Richtung (kein Nachbarfeld vorhanden)
     *  - Rückkehr auf das letzte Feld ("CANNOT_CHANGE_DIRECTION")
     *
     * @param sessionId  die STOMP/WebSocket-Session-ID des Clients
     * @param moveCmd    der empfangene Bewegungsbefehl mit Meeple-ID und Richtung
     */
    @MessageMapping("/move")
    public void handleMove(@Header("simpSessionId") String sessionId, MovementCommand moveCmd) {

        Player player = gameService.getPlayerBySession(sessionId);

        // nur zum testen
        if (player == null) {
            System.out.println("No player found for session " + sessionId + ", creating dummy player...");
            player = gameService.createDummyPlayer(sessionId);
            player.getMeeples()[0].setId(moveCmd.meepleId());
            player.getMeeples()[0].setCurrentField(gameService.getTestStartField());
        }
        //

        Meeple meeple = player.getMeepleWithId(moveCmd.meepleId());
        Field currentField = meeple.getCurrentField();
        Field lastField = meeple.getLastField();
        Direction direction = moveCmd.direction();

        // Ziel-Feld anhand der Bewegungsrichtung bestimmen
        Field nextField = switch (direction) {
            case NORTH -> currentField.getNorth();
            case EAST -> currentField.getEast();
            case SOUTH -> currentField.getSouth();
            case WEST -> currentField.getWest();
        };

        if (nextField == null) {
            System.out.println("invalid direction!");
            return;
        }

        if (nextField.isBarrier()) {
            // TODO player loses all unspent steps
            System.out.println("reached blockade, cant go any further!");
            return;
        }

        if (nextField.getOccupant() != null) {
            // TODO duel starts
            System.out.println("oh oh, looks like its time to duel!");
            return;
        }

        //Rückwärtsbewegung nicht erlaubt
        if (nextField.equals(lastField)) {
            System.out.println("cannot change direction!");
            FrontendMoveRejectedEvent rejectEvent = new FrontendMoveRejectedEvent(
                    sessionId,
                    meeple.getId(),
                    "CANNOT_CHANGE_DIRECTION");

            messagingTemplate.convertAndSend("/topic/move", rejectEvent);
            return;
        }

        // Spielfeld-Zustand aktualisieren
        currentField.setOccupant(null);
        nextField.setOccupant(meeple);
        meeple.setLastField(currentField);
        meeple.setCurrentField(nextField);

        // Erfolgreiche Bewegung an Clients senden
        FrontendMoveEvent frontendMoveEvent = new FrontendMoveEvent(
                sessionId,
                meeple.getId(),
                nextField.getId());

        messagingTemplate.convertAndSend("/topic/move", frontendMoveEvent);

    }
}
