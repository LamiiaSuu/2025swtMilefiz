package de.hs_rm.de.milefiz.messaging.commands;

import java.util.UUID;

public record RotationCommand(UUID meepleId, double rotation) {
    
}
