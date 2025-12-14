package de.hs_rm.de.milefiz.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;

@Service
public class FrontendMessagingServiceImpl implements FrontendMessagingService {

    private final Logger logger = LoggerFactory.getLogger(FrontendMessagingServiceImpl.class);
    private final SimpMessagingTemplate messagingTemplate;

    public FrontendMessagingServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sendet ein Event an alle Clients die die Lobby abonniert haben.
     *
     * @param lobbyEvent beinhaltet Lobby (Empfänger) und FrontendEvent
     * (Payload). Verfügbare Eventtypen, z.B.:
     * <ul>
     * <li>{@link de.hs_rm.de.milefiz.messaging.events.FrontendJumpEvent}</li>
     * <li>{@link de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent}</li>
     * <li>{@link de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent}</li>
     * <li>{@link de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceRejectedEvent}</li>
     * </ul>
     */
    @Override
    @EventListener
    public void sendEvent(LobbyMessage lobbyEvent) {
        Lobby lobby = lobbyEvent.lobby();
        FrontendEvent event = lobbyEvent.event();
        messagingTemplate.convertAndSend("/topic/milefiz/lobby/" + lobby.getId().toString(), event);
        logger.info("Gesendet: {}", event);
    }
}
