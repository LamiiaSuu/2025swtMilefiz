package de.hs_rm.de.milefiz.game.service;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;


/**
 * Standard-Implementierung des GameService Interface.
 * 
 * <h2>Verwendete Services:</h2>
 * <ul>
 * <li>{@link DiceServiceImpl} - Für Würfelaktionen</li>
 * </ul>
 * 
 * @author Leon Schäfer
 */
@Service
public class GameServiceImpl implements GameService {

    private final DiceServiceImpl diceService;
    private final ApplicationEventPublisher publisher;
    private Board testBoard;

    public GameServiceImpl(DiceServiceImpl diceService, ApplicationEventPublisher publisher) {
        this.diceService = diceService;
        this.publisher = publisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int rollDice() {

        return diceService.roll();
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
