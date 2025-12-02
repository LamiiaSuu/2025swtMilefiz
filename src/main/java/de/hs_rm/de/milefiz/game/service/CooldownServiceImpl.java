package de.hs_rm.de.milefiz.game.service;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.messaging.events.FrontendCooldownFinishedEvent;

/**
 * 
 * Service-Implementierung zur Verwaltung von zeitbasierten Cooldowns pro Spieler.
 * <p>
 * Der Service speichert aktive Cooldowns in einer thread-sicheren Map und reduziert diese
 * jede Sekunde über die mit {@link Scheduled} annotierte {@link #tick()}-Methode.
 * Sobald ein Cooldown eines Spielers abläuft, wird ein {@link FrontendCooldownFinishedEvent}
 * über den {@link ApplicationEventPublisher} veröffentlicht.
 * </p>
 * @author Robert Bothfeld
 */

@Service
public class CooldownServiceImpl implements CooldownService {

    /** Thread-sichere Map mit verbleibenden Cooldown-Sekunden pro Spieler. */
    private final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();

    /** Publisher zum Auslösen von Spring-Events, wenn ein Cooldown endet. */
    private final ApplicationEventPublisher publisher;

    /** Standard-Cooldown-Dauer in Sekunden, über application.properties gesetzt. */
    @Value("${dice.cooldown.seconds}")
    private int defaultSeconds;

    /**
     * Erstellt einen neuen CooldownService.
     *
     * @param publisher Event-Publisher, über den Cooldown-Ende-Events ausgelöst werden
     */
    public CooldownServiceImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Fügt einem Spieler einen Cooldown hinzu oder setzt einen bereits vorhandenen zurück.
     * <p>
     * Der Cooldown-Wert wird auf den in der Konfiguration definierten Standardwert gesetzt.
     * </p>
     *
     * @param playerId die UUID des Spielers, der einen Cooldown erhalten soll
     */
    public void addCooldown(UUID playerId) {
        cooldowns.put(playerId, defaultSeconds);
    }

    /**
     * Gibt die verbleibenden Cooldown-Sekunden eines Spielers zurück.
     *
     * @param playerId die UUID des Spielers
     * @return verbleibende Zeit in Sekunden oder {@code 0}, falls kein aktiver Cooldown existiert
     */
    public int getCooldown(UUID playerId) {
        return cooldowns.getOrDefault(playerId, 0);
    }

    /**
     * Reduziert jede Sekunde alle Cooldowns um 1.
     * <p>
     * Diese Methode wird durch Spring automatisch im festen Intervall aufgerufen.
     * Sobald ein Cooldown 0 erreicht oder unterschreitet, wird er aus der Map entfernt
     * und ein {@link FrontendCooldownFinishedEvent} veröffentlicht.
     * </p>
     */
    @Override
    @Scheduled(fixedRate = 1000) 
    public void tick() {
        cooldowns.forEach((uuid, seconds) -> {
            int newTime = seconds - 1;

            if (newTime <= 0) {
                cooldowns.remove(uuid); // Cooldown fertig -> spieler raus aus der Map


                publisher.publishEvent(new FrontendCooldownFinishedEvent(uuid, null));
            } else {
                cooldowns.put(uuid, newTime);
            }
        });
    }
}