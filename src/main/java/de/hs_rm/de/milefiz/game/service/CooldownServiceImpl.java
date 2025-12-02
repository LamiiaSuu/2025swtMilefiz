package de.hs_rm.de.milefiz.game.service;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.messaging.events.FrontendCooldownFinishedEvent;


@Service
public class CooldownServiceImpl implements CooldownService {

    //Map für Spieler-Cooldowns
    private final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher publisher;

    @Value("${dice.cooldown.seconds}")
    private int defaultSeconds;

    public CooldownServiceImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Fügt einen Cooldown für einen Spieler hinzu oder aktualisiert einen Cooldown für einen Spieler.
     */
    public void addCooldown(UUID playerId) {
        cooldowns.put(playerId, defaultSeconds);
    }

    /**
     * Gibt die verbleibenden Sekunden für einen Spieler zurück.
     * 
     */
    public int getCooldown(UUID playerId) {
        return cooldowns.getOrDefault(playerId, 0);
    }

    /**
     * Wird automatisch jede Sekunde (1000ms) von Spring aufgerufen. In dieser Methode wird die Map 'cooldowns' durchiteriert und für jeden Spieler, sollte mindestens eine Sekunde übrig sein, eine Sekunde abgezogen.
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