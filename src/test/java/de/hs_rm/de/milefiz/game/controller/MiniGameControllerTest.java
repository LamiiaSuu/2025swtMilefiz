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
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.minigames.BalloonGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.EinarmigerBanditGame;
import de.hs_rm.de.milefiz.game.service.DuelResolutionService;
import de.hs_rm.de.milefiz.game.service.DuelService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;

@ExtendWith(MockitoExtension.class)
class MiniGameControllerTest {

    @Mock
    private DuelService duelService;

    @Mock
    private DuelResolutionService duelResolutionService;

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

    // -------------------------------------------------
    // DiceGame
    // -------------------------------------------------

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
        verifyNoInteractions(duelResolutionService);
    }

    @Test
    void handleDiceRoll_gameFinished_delegatesToResolutionService() throws Exception {
        when(player.getId()).thenReturn(playerId);
        DiceGame game = mock(DiceGame.class);
        Duel duel = mock(Duel.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(true);
        when(duelService.getDuel(duelId)).thenReturn(duel);

        controller.handleDiceRoll(lobbyId, duelId, player);

        verify(game).roll(playerId);
        verify(duelResolutionService)
                .sendLoserHome(lobby, duel, game);
    }

    // -------------------------------------------------
    // Einarmiger Bandit
    // -------------------------------------------------

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
        verifyNoInteractions(duelResolutionService);
    }

    @Test
    void handleSlotStop_gameFinished_delegatesToResolutionService() throws Exception {
        when(player.getId()).thenReturn(playerId);
        EinarmigerBanditGame game = mock(EinarmigerBanditGame.class);
        Duel duel = mock(Duel.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(true);
        when(duelService.getDuel(duelId)).thenReturn(duel);

        controller.handleSlotStop(lobbyId, duelId, player);

        verify(game).stop(playerId);
        verify(duelResolutionService)
                .sendLoserHome(lobby, duel, game);
    }

    // -------------------------------------------------
    // BalloonGame
    // -------------------------------------------------

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
        verifyNoInteractions(duelResolutionService);
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
        verifyNoInteractions(duelResolutionService);
    }

    @Test
    void handleBalloonClick_gameFinished_delegatesToResolutionService() throws Exception {
        when(player.getId()).thenReturn(playerId);
        BalloonGame game = mock(BalloonGame.class);
        Duel duel = mock(Duel.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.processClick(playerId)).thenReturn(true);
        when(game.isFinished()).thenReturn(true);
        when(duelService.getDuel(duelId)).thenReturn(duel);

        controller.handleBalloonClick(lobbyId, duelId, player);

        verify(duelResolutionService)
                .sendLoserHome(lobby, duel, game);
    }

    // -------------------------------------------------
    // broadcastBalloonUpdate
    // -------------------------------------------------

    @Test
    void broadcastBalloonUpdate_sendsEvent() {
        BalloonGame game = mock(BalloonGame.class);

        controller.broadcastBalloonUpdate(lobby, duelId, game);

        verify(messaging).sendEvent(any(LobbyMessage.class));
    }
}
