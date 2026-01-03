package de.hs_rm.de.milefiz.game.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.service.DuelService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendDiceGameUpdateEvent;

@Controller
public class MiniGameController {

    private final DuelService duelService;
    private final FrontendMessagingService messaging;
    private final LobbyManager lobbyManager;

    public MiniGameController(
            DuelService duelService,
            FrontendMessagingService messaging,
            LobbyManager lobbyManager
    ) {
        this.duelService = duelService;
        this.messaging = messaging;
        this.lobbyManager = lobbyManager;
    }

    @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/dice/roll")
    public void handleDiceRoll(
            @DestinationVariable UUID lobbyId,
            @DestinationVariable UUID duelId,
            Player player
    ) throws LobbyNotFoundException {

        // Lobby laden 
        Lobby lobby = lobbyManager.getLobby(lobbyId);

        // MiniGame holen (bereits zu diesem Zeitpunkt dem Duell zugewiesen)
        DiceGame game = (DiceGame) duelService.getMiniGame(duelId);

        // würfelt für diesen Spieler
        game.roll(player.getId());

        var event = new FrontendDiceGameUpdateEvent(
                duelId,
                game.getP1(),
                game.getP2(),
                game.getRollP1(),
                game.getRollP2(),
                game.getWinner(),
                game.isFinished()
        );


        messaging.sendEvent(new LobbyMessage(lobby, event));
    }
}
