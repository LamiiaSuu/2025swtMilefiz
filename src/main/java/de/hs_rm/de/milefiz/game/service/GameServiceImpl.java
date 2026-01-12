package de.hs_rm.de.milefiz.game.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.dto.BoardDTO;
import de.hs_rm.de.milefiz.game.model.mapper.BoardMapper;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;

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
    private final PlantingService plantingService;
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
            CooldownServiceImpl cooldownService, MovementService movementService, PlantingService plantingService)
            throws IOException {

        this.plantingService = plantingService;

        final String BOARD_PATH = "boards/standardBoard.json";
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(BOARD_PATH);
        if (inputStream == null) {
            logger.error("BoardFile not found: {}", BOARD_PATH);
            throw new IOException("BoardFile not found: " + BOARD_PATH);
        }

        try {
            BoardDTO testBoardDTO = objectMapper.readValue(inputStream, BoardDTO.class);
            if (!testBoardDTO.hasTrees()) {
                logger.info("Test board has no trees, Generating local trees...");
                testBoardDTO = this.plantingService.plantTrees(testBoardDTO, 0.1f); // TODO: test
            }
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
        return new Board(testBoard);
    }

    @Override
    public void setTestBoard(Board testBoard) {
        this.testBoard = testBoard;
    }

    /**
     * Verarbeitet ein Meeple-Bewegungskommando und delegiert die
     * Ausführung an den {@link MovementService}.
     *
     * @param lobbyId die eindeutige ID der Lobby, in der der Zug ausgeführt wird
     * @param moveCmd das Bewegungskommando mit Meeple-ID und Bewegungsrichtung
     * @param player  der Spieler, der den Zug ausführt
     *
     * @return ein {@link FrontendEvent}, das den Ausgang des Zuges beschreibt
     *
     * @see MovementService#moveMeeple(UUID, MovementCommand, Player)
     *
     * @author Maximilian Ressel
     */
    @Override
    public FrontendEvent moveMeeple(UUID lobbyId, MovementCommand moveCmd, Player player) {

        logger.info("Processing movement command in lobby {} from player '{}': meeple {} moving {} (sessionId={})",
                lobbyId,
                player != null ? player.getName() : "anonymous",
                moveCmd.meepleId(),
                moveCmd.direction());

        return movementService.moveMeeple(lobbyId, moveCmd, player);
    }

    /**
     * Verarbeitet ein Kommando zur Verschiebung einer Barriere und
     * delegiert die Spiellogik an den {@link MovementService}.
     *
     * @param lobbyId     die eindeutige ID der Lobby, in der die Barriere bewegt
     *                    wird
     * @param moveBarrCmd das Kommando mit Barrieren-ID und Ziel-Feld-ID
     * @param player      der Spieler, der die Aktion ausführt
     *
     * @return ein {@link FrontendEvent}, das den Ausgang der Barrierenbewegung
     *         beschreibt
     *
     * @see MovementService#moveBarrier(UUID, MoveBarrierCommand, Player)
     *
     * @author Maximilian Ressel
     */
    @Override
    public FrontendEvent moveBarrier(UUID lobbyId, MoveBarrierCommand moveBarrCmd, Player player) {

        logger.info(
                "Processing MoveBarrierCommand in lobby {} from player '{}': barrier {} moving to field {} (sessionId={})",
                lobbyId,
                player != null ? player.getName() : "anonymous",
                moveBarrCmd.barrierId(),
                moveBarrCmd.targetFieldId());

        return movementService.moveBarrier(lobbyId, moveBarrCmd, player);
    }
}
