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


        public void tick() {

        }
}