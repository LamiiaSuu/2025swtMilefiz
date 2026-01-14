package de.hs_rm.de.milefiz.game.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.*;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;

@Service
public class DuelResolutionService {

    private final FrontendMessagingService messaging;

    public DuelResolutionService(FrontendMessagingService messaging) {
        this.messaging = messaging;
    }

    
    /**
     * Setzt nach einem beendeten Duell die Loser-Meeples
     * zurück auf ihr jeweiliges Startfeld. Das können beide sein.
     *
     * <p>
     * Regeln:
     * <ul>
     * <li>Gewinner bleibt stehen</li>
     * <li>Verlierer gehen zurück in die Basis</li>
     * <li>Bei Unentschieden verlieren beide</li>
     * </ul>
     *
     * <p>
     * Zusätzlich wird ein {@link FrontendMoveEvent}
     * gesendet, damit das Update im Frontend animiert wird.
     *
     * @param lobby  aktuelle Lobby
     * @param duelId ID des Duells
     * @param game   beendetes Mini-Game
     */
    public void sendLoserHome(Lobby lobby, Duel duel, MiniGame game) {

        UUID winner = game.getWinner();

        UUID p1 = duel.getPlayer1();
        UUID p2 = duel.getPlayer2();

        Meeple m1 = lobby.getMeepleById(duel.getFirstMeeple());
        Meeple m2 = lobby.getMeepleById(duel.getSecondMeeple());

        Field start1 = lobby.getBoard().getStartField(
                lobby.getPlayer(p1).getColor());

        Field start2 = lobby.getBoard().getStartField(
                lobby.getPlayer(p2).getColor());

        handleLoser(lobby, p1, m1, start1, winner);
        handleLoser(lobby, p2, m2, start2, winner);
    }

    private void handleLoser(
            Lobby lobby,
            UUID playerId,
            Meeple meeple,
            Field start,
            UUID winner
    ) {
        if (winner != null && winner.equals(playerId)) {
            return; // Gewinner bleibt stehen
        }

        Player player = lobby.getPlayer(playerId);

        if (Objects.equals(player.getActiveMeeple(), meeple)) {
            player.setRemainingMoves(0);
            player.setActiveMeeple(null);
        }

        messaging.sendEvent(new LobbyMessage(
                lobby,
                new FrontendMoveEvent(
                        playerId,
                        meeple.getId(),
                        start.getId(),
                        player.getRemainingMoves(),
                        player.hasMoved()
                )
        ));

        meeple.setCurrentField(start);
        meeple.clearLastField();
    }
}
