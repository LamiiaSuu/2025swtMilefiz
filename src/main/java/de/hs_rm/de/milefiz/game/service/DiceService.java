package de.hs_rm.de.milefiz.game.service;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.stereotype.Service;

/**
 * Würfel
 */
@Service
public class DiceService {
    private final Random random = new SecureRandom(); // SecureRandom ist ncht vorhersehbar (nutzt System-Entropie z.B.
                                                      // Mausbewegung)
                                                      // Kein Random, da dieser einem linearen Algorithmus folgt ->
                                                      // Vorhersehbar

    /**
     * 
     * @return eine Zahl zwischen 1 und 6
     */
    public int roll() {
        return random.nextInt(6) + 1;
    }
}
