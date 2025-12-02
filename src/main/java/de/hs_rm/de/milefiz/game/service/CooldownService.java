package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;


/**
 * Service-Schnittstelle zur Verwaltung zeitbasierter Cooldowns pro Spieler.
 * <p>
 * Implementierungen dieses Interfaces erlauben das Setzen und Abfragen von Cooldowns
 * sowie das regelmäßige Aktualisieren (Herunterzählen) der Cooldown-Werte.
 * </p>
 */
public interface CooldownService {

    /**
     * Gibt die verbleibenden Cooldown-Sekunden eines Spielers zurück.
     *
     * @param playerId die UUID des Spielers
     * @return verbleibende Sekunden; {@code 0}, wenn kein aktiver Cooldown existiert
     */
    public int getCooldown(UUID playerId);

    /**
     * Fügt einem Spieler einen Cooldown hinzu oder setzt einen bestehenden zurück.
     *
     * @param playerId die UUID des Spielers, für den der Cooldown gesetzt werden soll
     */
    public void addCooldown(UUID playerId);

    /**
     * Aktualisiert alle aktiven Cooldowns, typischerweise indem ihre verbleibende
     * Zeit um eine Sekunde reduziert wird.
     * <p>
     * Diese Methode ist dafür gedacht, in festen Intervallen (z. B. via {@code @Scheduled})
     * aufgerufen zu werden.
     * </p>
     */
    public void tick();
}
 