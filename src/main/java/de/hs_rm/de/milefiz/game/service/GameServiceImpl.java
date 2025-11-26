package de.hs_rm.de.milefiz.game.service;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


/**
 * Standard-Implementierung des GameService Interface.
 * 
 * <h3>Verwendete Services:</h3>
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
}
