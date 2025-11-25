package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

public record FrontendMoveRejectedEvent(
        String sessionId,
        UUID meepleId,
        String reason
) implements FrontendEvent { }
