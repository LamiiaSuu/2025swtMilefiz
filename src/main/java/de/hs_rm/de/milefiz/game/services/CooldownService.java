package de.hs_rm.de.milefiz.game.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CooldownService {

    /* Standard-Cooldown-Dauer aus application.properties */
    @Value("${dice.cooldown.seconds}")
    private long cooldownDurationSeconds;
    private long cooldownDurationMs;
    
    
    public CooldownService() {
        this.cooldownDurationMs = cooldownDurationSeconds*1000;
    }

    

}