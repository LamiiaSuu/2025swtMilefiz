package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;
import de.hs_rm.de.milefiz.game.model.Direction;

public record MovementCommand(UUID meepleId, Direction direction) {
    
}