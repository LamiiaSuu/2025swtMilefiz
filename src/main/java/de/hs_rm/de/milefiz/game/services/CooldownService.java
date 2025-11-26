package de.hs_rm.de.milefiz.game.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;


@Service
public class CooldownService {

    private final GameService gameService;
    private final FrontendMessagingService messagingService;
    
    /* Standard-Cooldown-Dauer aus application.properties */
    @Value("${dice.cooldown.seconds}")
    private long cooldownDurationSeconds;


    
    
    public CooldownService(GameService gameService, FrontendMessagingService messagingService) {
        this.gameService = gameService;
        this.messagingService = messagingService;
    }




}