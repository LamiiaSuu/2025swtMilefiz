package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;

public interface MovementService {
    FrontendEvent moveMeeple(UUID lobbyId, MovementCommand moveCmd, Player player);

    FrontendEvent moveBarrier(UUID lobbyId, MoveBarrierCommand moveCmd, Player player);
}
