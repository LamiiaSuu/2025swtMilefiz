package de.hs_rm.de.milefiz.game.service;

/**
 * Service Interface für Würfel-Operationen im Milefiz-Spiel.
 * 
 * <p>Definiert die Grundfunktionalität für das Würfeln in Spielsituationen.
 * Implementierungen sollten zufällige, nicht vorhersagbare Ergebnisse liefern.</p>
 * 
 * @author Leon Schäfer
 * 
 * @see DiceServiceImpl
 */
public interface DiceService {

    /**
     * Führt einen Würfelwurf aus.
     * 
     * <p>Generiert eine zufällige Zahl zwischen 1 und 6 (inklusive), 
     * entsprechend einem Standard-Sechsseiten-Würfel.</p>
     * 
     * @return eine zufällige Ganzzahl zwischen 1 und 6 (beide Grenzen inklusive)
     */
    public int roll();
}
