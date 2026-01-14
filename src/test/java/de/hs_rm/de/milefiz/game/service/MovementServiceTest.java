package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.*;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.*;

@ExtendWith(MockitoExtension.class)
public class MovementServiceTest {

    @Mock
    private LobbyManager lobbyManager;

    @Mock
    private DuelService duelService;

    private MovementService movementService;

    private Lobby lobby;
    private Board board;
    private Player player;
    private Meeple meeple;
    private Field start;
    private Field next;

    @BeforeEach
    void setUp() throws LobbyNotFoundException {
        movementService = new MovementServiceImpl(lobbyManager, duelService);

        start = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 0));
        next = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 1));
        start.addNeighbour(next, Direction.NORTH);

        board = new Board();
        board.setStartGreen(start);

        player = new Player(Color.RED);
        player.setRemainingMoves(2);

        meeple = player.getMeeples()[0];
        meeple.setCurrentField(start);

        lobby = new Lobby();
        lobby.setBoard(board);
        lobby.setPlayers(List.of(player));

        when(lobbyManager.getLobby(lobby.getId())).thenReturn(lobby);
    }

    @Test
    void moveMeepleSwitchingMeepleMidTurnTriggersCheatEvent() {
        Meeple other = player.getMeeples()[1];
        other.setCurrentField(start);

        player.setActiveMeeple(meeple);
        player.setMoved(true);

        MovementCommand cmd = new MovementCommand(other.getId(), Direction.NORTH);
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendCheatedEvent.class, result);
    }

    @Test
    void moveMeepleNoMovesLeftResetsMovedFlag() {
        player.setRemainingMoves(0);
        player.setMoved(true);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);
        movementService.moveMeeple(lobby.getId(), cmd, player);

        assertFalse(player.hasMoved());
    }

    @Test
    void moveMeepleOntoRivalMeepleAlreadyInDuelIsRejected() {
        player.setRemainingMoves(1);

        Player rival = new Player(Color.BLUE);
        Meeple rivalMeeple = rival.getMeeples()[0];
        rivalMeeple.setCurrentField(next);

        lobby.setPlayers(List.of(player, rival));

        when(duelService.isMeepleInDuel(rivalMeeple.getId())).thenReturn(true);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);
        assertEquals("MEEPLE_IN_DUEL",
                ((FrontendMoveRejectedEvent) result).msg());
    }

    @Test
    void moveBarrierWithUnknownTargetFieldThrowsException() {
        Meeple barrier = new Meeple(true);
        barrier.setCurrentField(start);
        board.addBarrier(barrier);

        MoveBarrierCommand cmd =
                new MoveBarrierCommand(barrier.getId(), UUID.randomUUID());

        assertThrows(NullPointerException.class,
                () -> movementService.moveBarrier(lobby.getId(), cmd, player));
    }
}
