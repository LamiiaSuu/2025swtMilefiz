package de.hs_rm.de.milefiz.game.service;


import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;
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
    private final ApplicationEventPublisher publisher;
    private Board testBoard;

    public GameServiceImpl(DiceServiceImpl diceService, ApplicationEventPublisher publisher, CooldownServiceImpl cooldownService) throws StreamReadException, DatabindException, IOException {
        ObjectMapper objectMapper;
        objectMapper = new ObjectMapper();
        BoardDTO testBoardDTO = objectMapper.readValue(new File("src/main/resources/static/boards/dummyBoard.json"), BoardDTO.class);
        testBoard = BoardMapper.mapToBoard(testBoardDTO);
        
        this.diceService = diceService;
        this.cooldownService = cooldownService;
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
}
