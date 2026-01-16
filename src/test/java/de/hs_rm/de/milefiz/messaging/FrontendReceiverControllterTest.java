package de.hs_rm.de.milefiz.messaging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.lobby.PlayerHasNoPermissionException;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.mapper.LobbyMapper;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.commands.*;
import de.hs_rm.de.milefiz.messaging.events.*;

@ExtendWith(MockitoExtension.class)
class FrontendReceiverControllerTest {

    private LobbyManager lobbyManager;
    private GameService gameService;
    private SimpMessagingTemplate messagingTemplate;
    private FrontendMessagingServiceImpl messagingService;
    private LobbyMapper lobbyMapper;

    private FrontendReceiverController controller;

    @BeforeEach
    void setup() {
        lobbyManager = mock(LobbyManager.class);
        gameService = mock(GameService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        messagingService = mock(FrontendMessagingServiceImpl.class);
        lobbyMapper = mock(LobbyMapper.class);

        controller = new FrontendReceiverController(
                lobbyManager,
                gameService,
                messagingTemplate,
                messagingService,
                lobbyMapper
        );
    }

    /* ---------------- ROTATE ---------------- */

    @Test
    void handleRotate_returnsRotateEvent() {
        UUID lobbyId = UUID.randomUUID();
        UUID meepleId = UUID.randomUUID();
        Player player = mock(Player.class);

        RotationCommand cmd = new RotationCommand(meepleId, 45f);

        FrontendEvent event = controller.handleRotate(lobbyId, cmd, player);

        assertInstanceOf(FrontendRotateEvent.class, event);
    }

    /* ---------------- MOVE ---------------- */

    @Test
    void handleMove_delegatesToGameService() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        MovementCommand cmd = mock(MovementCommand.class);

        FrontendEvent response = mock(FrontendEvent.class);
        when(gameService.moveMeeple(lobbyId, cmd, player)).thenReturn(response);

        FrontendEvent result = controller.handleMove(lobbyId, cmd, player);

        assertSame(response, result);
    }

    /* ---------------- BARRIER MOVE ---------------- */

    @Test
    void handleBarrierMove_delegatesToGameService() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        MoveBarrierCommand cmd = mock(MoveBarrierCommand.class);

        FrontendEvent response = mock(FrontendEvent.class);
        when(gameService.moveBarrier(lobbyId, cmd, player)).thenReturn(response);

        FrontendEvent result = controller.handleBarrierMove(lobbyId, cmd, player);

        assertSame(response, result);
    }

    /* ---------------- ROLL DICE ---------------- */

    @Test
    void handleRollDice_success() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        RollDiceCommand cmd = mock(RollDiceCommand.class);

        when(player.getId()).thenReturn(UUID.randomUUID());
        when(player.getRemainingMoves()).thenReturn(0);
        when(gameService.getRollDiceCooldown(any())).thenReturn(0);
        when(gameService.rollDice()).thenReturn(5);

        FrontendEvent event = controller.handleRollDice(lobbyId, cmd, player);

        assertInstanceOf(FrontendRollDiceEvent.class, event);
    }

    @Test
    void handleRollDice_rejectedDueToCooldown() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        RollDiceCommand cmd = mock(RollDiceCommand.class);

        when(player.getId()).thenReturn(UUID.randomUUID());
        when(gameService.getRollDiceCooldown(any())).thenReturn(3);

        FrontendEvent event = controller.handleRollDice(lobbyId, cmd, player);

        assertInstanceOf(FrontendRollDiceRejectedEvent.class, event);
    }

    /* ---------------- SAVE ENERGY ---------------- */

    @Test
    void handleSaveEnergy_success() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        EnergyCommand cmd = mock(EnergyCommand.class);

        when(player.getId()).thenReturn(UUID.randomUUID());
        when(player.hasMoved()).thenReturn(false);
        when(player.hasFullEnergy()).thenReturn(false);
        when(player.getEnergy()).thenReturn(2);
        when(player.getMaxEnergy()).thenReturn(10);

        FrontendEvent event = controller.handleSaveEnergy(lobbyId, cmd, player);

        assertInstanceOf(FrontendSaveEnergyEvent.class, event);
    }

    @Test
    void handleSaveEnergy_rejected() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        EnergyCommand cmd = mock(EnergyCommand.class);

        when(player.hasMoved()).thenReturn(true);

        FrontendEvent event = controller.handleSaveEnergy(lobbyId, cmd, player);

        assertInstanceOf(FrontendSaveEnergyRejectedEvent.class, event);
    }

    /* ---------------- CONSUME ENERGY ---------------- */

    @Test
    void handleConsumeEnergy_success() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        EnergyCommand cmd = mock(EnergyCommand.class);

        when(player.getId()).thenReturn(UUID.randomUUID());
        when(player.hasFullEnergy()).thenReturn(true);
        when(player.getEnergy()).thenReturn(5);

        FrontendEvent event = controller.handleConsumeEnergy(lobbyId, cmd, player);

        assertInstanceOf(FrontendConsumeEnergyEvent.class, event);
    }

    @Test
    void handleConsumeEnergy_rejected() {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        EnergyCommand cmd = mock(EnergyCommand.class);

        when(player.hasFullEnergy()).thenReturn(false);

        FrontendEvent event = controller.handleConsumeEnergy(lobbyId, cmd, player);

        assertInstanceOf(FrontendConsumeEnergyRejectedEvent.class, event);
    }

    /* ---------------- START GAME ---------------- */

    @Test
    void handleStartGame_success() throws Exception {
        UUID lobbyId = UUID.randomUUID();
        Player leader = mock(Player.class);
        Lobby lobby = mock(Lobby.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(lobby.getLeader()).thenReturn(leader);

        FrontendEvent event = controller.handleStartGame(lobbyId, leader);

        assertInstanceOf(FrontendGameStartEvent.class, event);
        verify(lobby).setGameStarted(true);
    }

    @Test
    void handleStartGame_notLeader_throws() throws Exception {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        Player leader = mock(Player.class);
        Lobby lobby = mock(Lobby.class);

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(lobby.getLeader()).thenReturn(leader);

        assertThrows(PlayerHasNoPermissionException.class,
                () -> controller.handleStartGame(lobbyId, player));
    }

    /* ---------------- UPDATE LOBBY ---------------- */

    @Test
    void handleLobbyUpdate_success() throws Exception {
        UUID lobbyId = UUID.randomUUID();
        Player leader = mock(Player.class);
        Lobby lobby = mock(Lobby.class);
        UpdateLobbySettingsCommand cmd =
                new UpdateLobbySettingsCommand("NewName", 6);

        when(leader.isLeader()).thenReturn(true);
        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(lobbyMapper.toDTO(lobby)).thenReturn(null);

        FrontendEvent event =
                controller.handleLobbyUpdate(lobbyId, cmd, leader);

        assertInstanceOf(FrontendLobbyUpdateEvent.class, event);
    }

    @Test
    void handleLobbyUpdate_lobbyNotFound() throws Exception {
        UUID lobbyId = UUID.randomUUID();
        Player leader = mock(Player.class);

        when(leader.isLeader()).thenReturn(true);
        when(lobbyManager.getLobby(lobbyId))
                .thenThrow(new LobbyNotFoundException("not found"));

        FrontendEvent event =
                controller.handleLobbyUpdate(
                        lobbyId,
                        new UpdateLobbySettingsCommand("x", 4),
                        leader);

        assertInstanceOf(FrontendLobbyUpdateEvent.class, event);
    }

    /* ---------------- UPDATE PLAYER NAME ---------------- */

    @Test
    void handlePlayerNameChange_success() throws Exception {
        UUID lobbyId = UUID.randomUUID();
        Player player = mock(Player.class);
        Lobby lobby = mock(Lobby.class);

        UpdatePlayerNameCommand cmd =
                new UpdatePlayerNameCommand("NewName");

        when(lobbyManager.getLobby(lobbyId)).thenReturn(lobby);
        when(lobbyMapper.toDTO(lobby)).thenReturn(null);

        FrontendEvent event =
                controller.handlePlayerNameChange(lobbyId, cmd, player);

        assertInstanceOf(FrontendLobbyUpdateEvent.class, event);
        verify(player).setPlayerName("NewName");
    }
}
