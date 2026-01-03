package de.hs_rm.de.milefiz.game.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.service.DuelService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendDiceGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;

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

    // Würfeln der Spieler
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
        
        if (!game.isFinished()) {
            return;
        }

        var duel = duelService.getDuel(duelId);

        UUID winner = game.getWinner();

        UUID p1 = duel.getPlayer1();
        UUID p2 = duel.getPlayer2();

        Meeple m1 = lobby.getMeepleById(duel.getFirstMeeple());
        Meeple m2 = lobby.getMeepleById(duel.getSecondMeeple());

        // Hilfsmethode: Startfeld des Spielers ermitteln
        Field start1 = lobby.getBoard().getStartField(
                lobby.getPlayer(p1).getColor()
        );

        Field start2 = lobby.getBoard().getStartField(
                lobby.getPlayer(p2).getColor()
        );

        // --- Spieler 1 verliert?
        if (winner == null || !winner.equals(p1)) {
            m1.clearLastField();
            messaging.sendEvent(new LobbyMessage(
                    lobby,
                    new FrontendMoveEvent(
                            p1,
                            m1.getId(),
                            start1.getId(),
                            lobby.getPlayer(p1).getRemainingMoves(),
                            lobby.getPlayer(p1).hasMoved()
                    )
            ));

            m1.setCurrentField(start1);
        }

        // --- Spieler 2 verliert?
        if (winner == null || !winner.equals(p2)) {
            m2.clearLastField();
            messaging.sendEvent(new LobbyMessage(
                    lobby,
                    new FrontendMoveEvent(
                            p2,
                            m2.getId(),
                            start2.getId(),
                            lobby.getPlayer(p2).getRemainingMoves(),
                            lobby.getPlayer(p2).hasMoved()
                    )
            ));

            m2.setCurrentField(start2);
        }
    }
}
