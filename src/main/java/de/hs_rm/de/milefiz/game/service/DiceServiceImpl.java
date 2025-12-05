package de.hs_rm.de.milefiz.game.service;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.stereotype.Service;
/**
 * Standard-Implementierung des {@link DiceService} für Würfelwürfe.
 * 
 * <p>Diese Implementierung verwendet {@link SecureRandom} zur Generierung 
 * kryptographisch sicherer, unvorhersagbarer Zufallszahlen. Dies verhindert 
 * potenzielle Manipulation oder Vorhersage von Würfelergebnissen durch 
 * Analyse der verwendeten Zufallsalgorithmen.</p>
 * 
 * @author Leon Schäfer
 * 
 */
@Service
public class DiceServiceImpl implements DiceService{

    private final Random random = new SecureRandom(); // SecureRandom ist ncht vorhersehbar (nutzt System-Entropie z.B.
                                                      // Mausbewegung)
                                                      // Kein Random, da dieser einem linearen Algorithmus folgt ->
                                                      // Vorhersehbar

    /**
     * Führt einen Würfelwurf aus und generiert eine Zufallszahl zwischen 1 und 6.
     * 
     * @return eine kryptographisch sichere Zufallszahl zwischen 1 und 6 (inklusive)
     */
    @Override
    public int roll() {
        return random.nextInt(6) + 1;
    }
    
}
