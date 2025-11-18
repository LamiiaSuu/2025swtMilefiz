package de.hs_rm.de.milefiz.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class FrontendMessagingServiceImpl implements FrontendMessagingService {

    private final Logger logger = LoggerFactory.getLogger(FrontendMessagingServiceImpl.class);
    private final SimpMessagingTemplate messagingTemplate;

    public FrontendMessagingServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void sendEvent(FrontendEvent ev) {
        messagingTemplate.convertAndSend("/topic/milefiz", ev);
        logger.info("Gesendet: {}", ev);
    }
}
