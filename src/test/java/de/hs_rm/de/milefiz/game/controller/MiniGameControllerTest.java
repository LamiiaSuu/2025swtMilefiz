package de.hs_rm.de.milefiz.game.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.*;
import de.hs_rm.de.milefiz.game.model.minigames.BalloonGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.EinarmigerBanditGame;
import de.hs_rm.de.milefiz.game.service.DuelService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;

@ExtendWith(MockitoExtension.class)
class MiniGameControllerTest {

    @Mock
    private DuelService duelService;

    @Mock
    private FrontendMessagingService messaging;

    @Mock
    private LobbyManager lobbyManager;

    @InjectMocks
    private MiniGameController controller;

    private UUID lobbyId;
    private UUID duelId;
    private UUID playerId;

    private Player player;
    private Lobby lobby;

    @BeforeEach
    void setUp() {
        lobbyId = UUID.randomUUID();
        duelId = UUID.randomUUID();
        playerId = UUID.randomUUID();

        player = mock(Player.class);

        lobby = mock(Lobby.class);
    }

    // DiceGame

    @Test
    void handleDiceRoll_gameNotFinished_broadcastOnly() throws Exception {
        when(player.getId()).thenReturn(playerId);
        DiceGame game = mock(DiceGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(false);

        controller.handleDiceRoll(lobbyId, duelId, player);

        verify(game).roll(playerId);
        verify(messaging).sendEvent(any(LobbyMessage.class));
        verify(duelService, never()).getDuel(any());
    }

    @Test
    void handleDiceRoll_gameFinished_triggersLoserHandling() throws Exception {
        when(player.getId()).thenReturn(playerId);
        DiceGame game = mock(DiceGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(true);

        Duel duel = mock(Duel.class);
        when(duelService.getDuel(duelId)).thenReturn(duel);

        stubDuelAndLobbyForLoserHandling(duel);

        controller.handleDiceRoll(lobbyId, duelId, player);

        verify(game).roll(playerId);
        verify(messaging, atLeastOnce()).sendEvent(any(LobbyMessage.class));
    }

    // Einarmiger Bandit

    @Test
    void handleSlotStop_gameNotFinished_broadcastOnly() throws Exception {
        when(player.getId()).thenReturn(playerId);
        EinarmigerBanditGame game = mock(EinarmigerBanditGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(false);

        controller.handleSlotStop(lobbyId, duelId, player);

        verify(game).stop(playerId);
        verify(messaging).sendEvent(any(LobbyMessage.class));
        verify(duelService, never()).getDuel(any());
    }

    @Test
    void handleSlotStop_gameFinished_triggersLoserHandling() throws Exception {
        when(player.getId()).thenReturn(playerId);
        EinarmigerBanditGame game = mock(EinarmigerBanditGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(true);

        Duel duel = mock(Duel.class);
        when(duelService.getDuel(duelId)).thenReturn(duel);

        stubDuelAndLobbyForLoserHandling(duel);

        controller.handleSlotStop(lobbyId, duelId, player);

        verify(game).stop(playerId);
        verify(messaging, atLeastOnce()).sendEvent(any(LobbyMessage.class));
    }

    // BalloonGame

    @Test
    void handleBalloonClick_phaseChanged_broadcastsUpdate() throws Exception {
        when(player.getId()).thenReturn(playerId);
        BalloonGame game = mock(BalloonGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.processClick(playerId)).thenReturn(true);
        when(game.isFinished()).thenReturn(false);

        controller.handleBalloonClick(lobbyId, duelId, player);

        verify(game).processClick(playerId);
        verify(messaging).sendEvent(any(LobbyMessage.class));
    }

    @Test
    void handleBalloonClick_noPhaseChange_noBroadcast() throws Exception {
        when(player.getId()).thenReturn(playerId);
        BalloonGame game = mock(BalloonGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.processClick(playerId)).thenReturn(false);
        when(game.isFinished()).thenReturn(false);

        controller.handleBalloonClick(lobbyId, duelId, player);

        verify(game).processClick(playerId);
        verify(messaging, never()).sendEvent(any());
    }

    @Test
    void handleBalloonClick_finished_triggersLoserHandling() throws Exception {
        when(player.getId()).thenReturn(playerId);
        BalloonGame game = mock(BalloonGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.processClick(playerId)).thenReturn(true);
        when(game.isFinished()).thenReturn(true);

        Duel duel = mock(Duel.class);
        when(duelService.getDuel(duelId)).thenReturn(duel);

        stubDuelAndLobbyForLoserHandling(duel);

        controller.handleBalloonClick(lobbyId, duelId, player);

        verify(messaging, atLeastOnce()).sendEvent(any(LobbyMessage.class));
    }

    // broadcastBalloonUpdate (öffentlich)

    @Test
    void broadcastBalloonUpdate_sendsEvent() {
        BalloonGame game = mock(BalloonGame.class);

        controller.broadcastBalloonUpdate(lobby, duelId, game);

        verify(messaging).sendEvent(any(LobbyMessage.class));
    }


    // Test-Helper

    private void stubDuelAndLobbyForLoserHandling(Duel duel) {
        when(player.getId()).thenReturn(playerId);
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        when(duel.getPlayer1()).thenReturn(p1);
        when(duel.getPlayer2()).thenReturn(p2);
        when(duel.getFirstMeeple()).thenReturn(UUID.randomUUID());
        when(duel.getSecondMeeple()).thenReturn(UUID.randomUUID());

        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);

        when(lobby.getPlayer(p1)).thenReturn(player1);
        when(lobby.getPlayer(p2)).thenReturn(player2);

        Board board = mock(Board.class);
        when(lobby.getBoard()).thenReturn(board);

        Field f1 = mock(Field.class);
        Field f2 = mock(Field.class);

        when(board.getStartField(any())).thenReturn(f1, f2);

        Meeple m1 = mock(Meeple.class);
        Meeple m2 = mock(Meeple.class);

        when(lobby.getMeepleById(any())).thenReturn(m1, m2);
    }
}
