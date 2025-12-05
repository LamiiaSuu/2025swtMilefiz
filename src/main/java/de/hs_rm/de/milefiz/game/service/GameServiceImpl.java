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
    private final ApplicationEventPublisher publisher;
    private Board testBoard;

    /**
     * Konstruktor für Dependency Injection und Board-Initialisierung.
     * 
     * @param diceService Service für Würfeloperationen
     * @param publisher Event Publisher für Events  
     * @param cooldownService Service für Cooldown-Management
     * @throws IOException wenn Board-Datei nicht gefunden oder gelesen werden kann
     * 
     * @author Leon Schäfer
     * 
     */
    public GameServiceImpl(DiceServiceImpl diceService, ApplicationEventPublisher publisher, CooldownServiceImpl cooldownService) throws IOException {
        final String BOARD_PATH = "boards/dummyBoard.json";
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(BOARD_PATH);
        if(inputStream == null){
            logger.error("BoardFile not found: {}", BOARD_PATH);
            throw new IOException("BoardFile not found: " + BOARD_PATH);
        }

        try{
            BoardDTO testBoardDTO = objectMapper.readValue(inputStream, BoardDTO.class);
            testBoard = BoardMapper.mapToBoard(testBoardDTO);
            logger.info("Test board loaded successfully from: {}", BOARD_PATH);
        }finally{
            inputStream.close();
        }
        
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
