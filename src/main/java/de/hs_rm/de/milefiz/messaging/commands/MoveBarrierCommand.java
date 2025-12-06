package de.hs_rm.de.milefiz.messaging.commands;

import java.util.UUID;

public record MoveBarrierCommand (UUID barrierId, UUID targetFieldId) {}
