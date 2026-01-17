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
import de.hs_rm.de.milefiz.game.model.minigames.ColorbrainGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.SlotMachineGame;
import de.hs_rm.de.milefiz.game.model.minigames.RockPaperScissorsGame;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.QuizGame;
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
    private UUID player2Id;

    private Player player;
    private Player player2;
    private Lobby lobby;

    @BeforeEach
    void setUp() {
        lobbyId = UUID.randomUUID();
        duelId = UUID.randomUUID();
        playerId = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        player = mock(Player.class);
        player2 = mock(Player.class);

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
    // SlotMachine
    // -------------------------------------------------

    @Test
    void handleSlotStop_gameNotFinished_broadcastOnly() throws Exception {
        when(player.getId()).thenReturn(playerId);
        when(player2.getId()).thenReturn(player2Id);
        SlotMachineGame game = mock(SlotMachineGame.class);

        when(game.getPlayer1()).thenReturn(player);
        when(game.getPlayer2()).thenReturn(player2);

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
        when(player2.getId()).thenReturn(player2Id);
        SlotMachineGame game = mock(SlotMachineGame.class);
        Duel duel = mock(Duel.class);

        when(game.getPlayer1()).thenReturn(player);
        when(game.getPlayer2()).thenReturn(player2);

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

    // -------------------------------------------------
    // QuizGame
    // -------------------------------------------------

    @Test
    void handleQuestionRequest_broadcastsQuizUpdate() throws Exception {
        QuizGame game = mock(QuizGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);

        controller.handleQuestionRequest(lobbyId, duelId, player);

        verify(messaging).sendEvent(any(LobbyMessage.class));
        verifyNoInteractions(duelResolutionService);
    }

    @Test
    void handleAnswerRequest_notFinished_onlyBroadcast() throws Exception {
        QuizGame game = mock(QuizGame.class);

        when(player.getId()).thenReturn(playerId);
        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(false);

        controller.handleAnswerRequest(lobbyId, duelId, player, 2);

        verify(game).checkAnswer(playerId, 2);
        verify(messaging).sendEvent(any(LobbyMessage.class));
        verifyNoInteractions(duelResolutionService);
    }

    @Test
    void handleAnswerRequest_finished_delegatesToResolutionService() throws Exception {
        QuizGame game = mock(QuizGame.class);
        Duel duel = mock(Duel.class);

        when(player.getId()).thenReturn(playerId);
        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);

        // Controller checkt nach checkAnswer() nochmal isFinished()
        when(game.isFinished()).thenReturn(true);

        when(duelService.getDuel(duelId)).thenReturn(duel);

        controller.handleAnswerRequest(lobbyId, duelId, player, 1);

        verify(game).checkAnswer(playerId, 1);
        verify(messaging).sendEvent(any(LobbyMessage.class));
        verify(duelResolutionService).sendLoserHome(lobby, duel, game);
    }

    @Test
    void broadcastQuizUpdate_sendsEvent() {
        QuizGame game = mock(QuizGame.class);

        controller.broadcastQuizUpdate(lobby, duelId, game);

        verify(messaging).sendEvent(any(LobbyMessage.class));
    }

    // -------------------------------------------------
    // RockPaperScissorsGame
    // -------------------------------------------------

    @Test
    void handleChooseMove_notFinished_onlyBroadcast() throws Exception {
        RockPaperScissorsGame game = mock(RockPaperScissorsGame.class);

        when(player.getId()).thenReturn(playerId);
        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(false);

        controller.handleChooseMove(lobbyId, duelId, "ROCK", player);

        verify(game).choose(playerId, "ROCK");
        verify(messaging).sendEvent(any(LobbyMessage.class));
        verifyNoInteractions(duelResolutionService);
    }

    @Test
    void handleChooseMove_finished_delegatesToResolutionService() throws Exception {
        RockPaperScissorsGame game = mock(RockPaperScissorsGame.class);
        Duel duel = mock(Duel.class);

        when(player.getId()).thenReturn(playerId);
        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);
        when(game.isFinished()).thenReturn(true);
        when(duelService.getDuel(duelId)).thenReturn(duel);

        controller.handleChooseMove(lobbyId, duelId, "PAPER", player);

        verify(game).choose(playerId, "PAPER");
        verify(messaging).sendEvent(any(LobbyMessage.class));
        verify(duelResolutionService).sendLoserHome(lobby, duel, game);
    }

    // -------------------------------------------------
    // ColorbrainGame
    // -------------------------------------------------

    @Test
    void handleColorbrainClick_payloadNull_doesNothing() throws Exception {
        ColorbrainGame game = mock(ColorbrainGame.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);

        controller.handleColorbrainClick(lobbyId, duelId, player, null);

        verify(game, never()).handlePlayerClick(any(), any());
        verify(messaging, never()).sendEvent(any());
    }

    @Test
    void handleColorbrainClick_payloadWithQuotes_isCleanedAndHandled() throws Exception {
        ColorbrainGame game = mock(ColorbrainGame.class);

        when(player.getId()).thenReturn(playerId);
        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(duelService.getMiniGame(duelId)).thenReturn(game);

        controller.handleColorbrainClick(lobbyId, duelId, player, "\"red\"");

        verify(game).handlePlayerClick(playerId, ColorbrainGame.ColorbrainColor.RED);
        verify(messaging).sendEvent(any(LobbyMessage.class));
    }

    @Test
    void broadcastColorbrainUpdate_sendsEvent() {
        ColorbrainGame game = mock(ColorbrainGame.class);

        controller.broadcastColorbrainUpdate(lobby, duelId, game);

        verify(messaging).sendEvent(any(LobbyMessage.class));
    }
}
