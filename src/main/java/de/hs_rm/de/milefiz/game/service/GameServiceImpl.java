package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendDuelEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.game.model.BoardDTO;
import de.hs_rm.de.milefiz.game.model.BoardMapper;

/**
 * Standard-Implementierung des GameService Interface.
 * 
 * <h2>Verwendete Services:</h2>
 * <ul>
 * <li>{@link DiceServiceImpl} - Für Würfelaktionen</li>
 * <li>{@link CooldownServiceImpl} - Für serverseitiges Cooldown-Management</li>
 * </ul>
 * 
 * @author Leon Schäfer
 */
@Service
public class GameServiceImpl implements GameService {

    private final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    private final DiceServiceImpl diceService;
    private final CooldownServiceImpl cooldownService;
    private final MovementService movementService;
    private final ApplicationEventPublisher publisher;
    private Board testBoard;

    /**
     * Konstruktor für Dependency Injection und Board-Initialisierung.
     * 
     * @param diceService     Service für Würfeloperationen
     * @param publisher       Event Publisher für Events
     * @param cooldownService Service für Cooldown-Management
     * @throws IOException wenn Board-Datei nicht gefunden oder gelesen werden kann
     * 
     * @author Leon Schäfer
     * 
     */
    public GameServiceImpl(DiceServiceImpl diceService, ApplicationEventPublisher publisher,
            CooldownServiceImpl cooldownService, MovementService movementService)
            throws IOException {
        final String BOARD_PATH = "boards/dummyBoard.json";
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(BOARD_PATH);
        if (inputStream == null) {
            logger.error("BoardFile not found: {}", BOARD_PATH);
            throw new IOException("BoardFile not found: " + BOARD_PATH);
        }

        try {
            BoardDTO testBoardDTO = objectMapper.readValue(inputStream,
                    BoardDTO.class);
            testBoard = BoardMapper.mapToBoard(testBoardDTO);
            logger.info("Test board loaded successfully from: {}", BOARD_PATH);
        } finally {
            inputStream.close();
        }

        this.diceService = diceService;
        this.cooldownService = cooldownService;
        this.movementService = movementService;
        this.publisher = publisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int rollDice() {

        return diceService.roll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRollDiceCooldown(UUID playerId) {

        return cooldownService.getCooldown(playerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRollDiceCooldown(UUID playerId) {
        cooldownService.addCooldown(playerId);
    }

    @Override
    public Board getTestBoard() {
        return testBoard;
    }

    @Override
    public void setTestBoard(Board testBoard) {
        this.testBoard = testBoard;
    }

    /**
     * Verarbeitet einen Bewegungsbefehl
     * ({@link de.hs_rm.de.milefiz.messaging.commands.MovementCommand})
     * innerhalb einer bestimmten Lobby und delegiert die Spiellogik an den
     * {@link de.hs_rm.de.milefiz.game.service.MovementService}.
     *
     * Diese Methode dient als zentrale Schnittstelle des GameService für
     * Figurenbewegungen (Meeples). Sie protokolliert den empfangenen
     * Befehl, übergibt ihn an den MovementService und gibt das dort erzeugte
     * Ergebnis-Event an den Aufrufer zurück.
     *
     * Ablauf:
     * - Logging des Bewegungsbefehls mit Lobby, Spieler, Meeple-ID, Richtung und
     * Session-ID
     * - Delegation an
     * {@link de.hs_rm.de.milefiz.game.service.MovementService#moveMeeple(UUID, MovementCommand, Principal, SimpMessageHeaderAccessor)}
     * - Rückgabe des {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent},
     * das das Ergebnis beschreibt
     *
     * Diese Methode enthält selbst keine Spiellogik, sondern fungiert als
     * Vermittler zwischen Controllerebene (z. B.
     * {@link de.hs_rm.de.milefiz.web.FrontendReceiverController})
     * und der tatsächlichen Spiellogik im MovementService.
     *
     * @param lobbyId   die eindeutige ID der Lobby, in der die Bewegung ausgeführt
     *                  wird
     * @param moveCmd   der Bewegungsbefehl mit Meeple-ID und Bewegungsrichtung
     * @param principal der Spieler (bzw. dessen Benutzerkontext), der den Zug
     *                  ausgelöst hat
     * @param sha       WebSocket-Header mit Sitzungsinformationen (z. B.
     *                  Session-ID)
     * @return ein {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent}, das
     *         das Ergebnis der Bewegung enthält
     *
     *         Author: Maximilian Ressel
     */
    @Override
    public FrontendEvent moveMeeple(UUID lobbyId, MovementCommand moveCmd, Principal principal,
            SimpMessageHeaderAccessor sha) {

        logger.info("Processing movement command in lobby {} from player '{}': meeple {} moving {} (sessionId={})",
                lobbyId,
                principal != null ? principal.getName() : "anonymous",
                moveCmd.meepleId(),
                moveCmd.direction(),
                sha.getSessionId());

        return movementService.moveMeeple(lobbyId, moveCmd, principal, sha);
    }

    /**
     * Verarbeitet einen Barrierenbewegungsbefehl
     * ({@link de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand})
     * innerhalb einer bestimmten Lobby und delegiert die Spiellogik an den
     * {@link de.hs_rm.de.milefiz.game.service.MovementService}.
     *
     * Diese Methode wird aufgerufen, wenn ein Spieler im Frontend eine Barriere
     * verschieben möchte (typischerweise nachdem ein Meeple direkt auf einer
     * Barriere gelandet ist). Sie führt Logging durch und übergibt die Anfrage an
     * den MovementService, der die Spielregeln prüft und die Bewegung ggf.
     * ausführt.
     *
     * Ablauf:
     * - Logging des Befehls mit Lobby, Spieler, Barrieren-ID, Ziel-Feld-ID und
     * Session-ID
     * - Delegation an
     * {@link de.hs_rm.de.milefiz.game.service.MovementService#moveBarrier(UUID, MoveBarrierCommand, Principal, SimpMessageHeaderAccessor)}
     * - Rückgabe des {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent},
     * das das Ergebnis beschreibt
     *
     * Wie
     * {@link #moveMeeple(UUID, MovementCommand, Principal, SimpMessageHeaderAccessor)}
     * enthält
     * auch diese Methode keine Spiellogik, sondern dient der Weiterleitung an die
     * zuständige
     * Spiellogik-Komponente.
     *
     * @param lobbyId     die eindeutige ID der Lobby, in der die Barriere
     *                    verschoben wird
     * @param moveBarrCmd der Barrierenbewegungsbefehl mit Barrieren-ID und
     *                    Ziel-Feld-ID
     * @param principal   der Spieler (bzw. dessen Benutzerkontext), der die Aktion
     *                    ausgelöst hat
     * @param sha         WebSocket-Header mit Sitzungsinformationen (z. B.
     *                    Session-ID)
     * @return ein {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent}, das
     *         das Ergebnis der Barrierenbewegung enthält
     *
     *         Author: Maximilian Ressel
     */
    @Override
    public FrontendEvent moveBarrier(UUID lobbyId, MoveBarrierCommand moveBarrCmd, Principal principal,
            SimpMessageHeaderAccessor sha) {

        logger.info(
                "Processing MoveBarrierCommand in lobby {} from player '{}': barrier {} moving to field {} (sessionId={})",
                lobbyId,
                principal != null ? principal.getName() : "anonymous",
                moveBarrCmd.barrierId(),
                moveBarrCmd.targetFieldId(),
                sha.getSessionId());

        return movementService.moveBarrier(lobbyId, moveBarrCmd, principal, sha);
    }
}
