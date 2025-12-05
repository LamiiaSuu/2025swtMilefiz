package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.Principal;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
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

    private final DiceServiceImpl diceService;
    private final CooldownServiceImpl cooldownService;
    private final MovementService movementService;
    private final ApplicationEventPublisher publisher;
    private final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    private Board testBoard;

    public GameServiceImpl(DiceServiceImpl diceService, ApplicationEventPublisher publisher,
            CooldownServiceImpl cooldownService, MovementService movementService)
            throws StreamReadException, DatabindException, IOException {
        ObjectMapper objectMapper;
        objectMapper = new ObjectMapper();
        BoardDTO testBoardDTO = objectMapper.readValue(new File("src/main/resources/static/boards/dummyBoard.json"),
                BoardDTO.class);
        testBoard = BoardMapper.mapToBoard(testBoardDTO);

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
}
