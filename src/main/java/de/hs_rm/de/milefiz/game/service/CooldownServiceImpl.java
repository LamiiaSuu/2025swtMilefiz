package de.hs_rm.de.milefiz.game.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


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

    public void tick() {

    }
}