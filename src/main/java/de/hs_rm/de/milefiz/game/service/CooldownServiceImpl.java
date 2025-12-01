package de.hs_rm.de.milefiz.game.service;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class CooldownServiceImpl implements CooldownService {

    //Map für Spieler-Cooldowns
    private final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();

    /**
     * Fügt einen Cooldown für einen Spieler hinzu oder aktualisiert einen Cooldown für einen Spieler.
     */
    public void addCooldown(UUID playerId, int seconds) {
        cooldowns.put(playerId, seconds);
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
            } else {
                cooldowns.put(uuid, newTime);
            }
        });
    }
}