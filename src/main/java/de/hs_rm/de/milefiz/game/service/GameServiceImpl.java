package de.hs_rm.de.milefiz.game.service;


import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;


/**
 * Standard-Implementierung des GameService Interface.
 * 
 * <h3>Verwendete Services:</h3>
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

    public GameServiceImpl(DiceServiceImpl diceService, ApplicationEventPublisher publisher, CooldownServiceImpl cooldownService) {
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

    @Override
    public int getRollDiceCooldown(UUID playerId) {

        return cooldownService.getCooldown(playerId);
    }

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
