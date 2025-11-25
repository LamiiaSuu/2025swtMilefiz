package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;

public record FrontendMoveRejectedEvent(
        String sessionId,
        UUID meepleId,
        String reason
) implements FrontendEvent { }
