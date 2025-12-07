package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.FieldType;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.Position;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveBarrierEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveBarrierRejectedEvent;

@ExtendWith(MockitoExtension.class)
public class MovementServiceBarrierTest {
    @Mock
    private LobbyManager lobbyManager;

    @Mock
    private Principal principal;

    @Mock
    private SimpMessageHeaderAccessor sha;

    private MovementService movementService;
    private Lobby lobby;
    private Board board;
    private Player player;
    private Meeple barrier;
    private Field startField;
    private Field meepleField;
    private Field barrierField;
    private Field freeField;

    private Field endField;

    @BeforeEach
    void setUp() throws LobbyNotFoundException {
        movementService = new MovementServiceImpl(lobbyManager);

        // Felder
        startField = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(0, 0));
        meepleField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 1));
        barrierField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        freeField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 3));
        endField = new Field(UUID.randomUUID(), FieldType.END, new Position(0, 4));

        startField.addNeighbour(meepleField, Direction.NORTH);
        meepleField.addNeighbour(barrierField, Direction.NORTH);
        barrierField.addNeighbour(freeField, Direction.NORTH);
        freeField.addNeighbour(endField, Direction.NORTH);

        // Board
        board = new Board();
        board.setStartGreen(startField);
        board.setStartYellow(startField);
        board.setStartBlue(startField);
        board.setStartRed(startField);

        // Spieler
        player = new Player(Color.RED);

        // Meeple
        Meeple playerMeeple = player.getMeeples()[0];
        playerMeeple.setCurrentField(meepleField);

        // Barriere
        barrier = new Meeple(true);
        barrier.setCurrentField(barrierField);
        board.addBarrier(barrier);

        // Lobby
        lobby = new Lobby();
        lobby.setBoard(board);
        lobby.setPlayers(List.of(player));

        when(lobbyManager.getLobby(lobby.getId())).thenReturn(lobby);
        when(principal.getName()).thenReturn("player1");
        when(sha.getSessionId()).thenReturn("session1");
    }

    // Barriere erfolgreich auf freies Feld setzen
    @Test
    void moveBarrierToValidFieldSucceeds() {
        MoveBarrierCommand cmd = new MoveBarrierCommand(barrier.getId(), freeField.getId());

        FrontendEvent result = movementService.moveBarrier(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveBarrierEvent.class, result);

        FrontendMoveBarrierEvent evt = (FrontendMoveBarrierEvent) result;
        assertEquals(barrier.getId(), evt.id());
        assertEquals(barrier.getCurrentField().getId(), evt.targetField());
        assertEquals(freeField.getId(), evt.targetField());
    }

    // Versuch Barriere auf ein Startfeld zu setzen
    @Test
    void moveBarrierOntoStartFieldIsRejected() {
        MoveBarrierCommand cmd = new MoveBarrierCommand(barrier.getId(), startField.getId());

        FrontendEvent result = movementService.moveBarrier(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveBarrierRejectedEvent.class, result);

        FrontendMoveBarrierRejectedEvent evt = (FrontendMoveBarrierRejectedEvent) result;
        assertEquals("BARRIER_MOVE_ERROR", evt.type());
        assertEquals("Cant place a barrier on Start or End", evt.msg());
    }

    // Versuch Barriere auf Endfeld zu setzen
    @Test
    void moveBarrierOntoEndFieldIsRejected() {
        MoveBarrierCommand cmd = new MoveBarrierCommand(barrier.getId(), endField.getId());

        FrontendEvent result = movementService.moveBarrier(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveBarrierRejectedEvent.class, result);

        FrontendMoveBarrierRejectedEvent evt = (FrontendMoveBarrierRejectedEvent) result;
        assertEquals("BARRIER_MOVE_ERROR", evt.type());
        assertEquals("Cant place a barrier on Start or End", evt.msg());
    }

    // Versuch eine Barriere auf Feld mit Meeple zu setzen
    @Test
    void moveBarrierOntoOccupiedFieldIsRejected() {
        // Das Feld ist bereits durch einen Meeple des Spielers besetzt
        MoveBarrierCommand cmd = new MoveBarrierCommand(barrier.getId(), meepleField.getId());

        FrontendEvent result = movementService.moveBarrier(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveBarrierRejectedEvent.class, result);

        FrontendMoveBarrierRejectedEvent evt = (FrontendMoveBarrierRejectedEvent) result;
        assertEquals("BARRIER_MOVE_ERROR", evt.type());
        assertEquals("Cant place a barrier on an occupied Field", evt.msg());
    }

    // Versuch Barriere auf Feld mit Barriere zu setzen
    @Test
    void moveBarrierOntoFieldOccupiedByAnotherBarrierIsRejected() {

        Meeple secondBarrier = new Meeple(true);
        secondBarrier.setCurrentField(freeField);
        board.addBarrier(secondBarrier);

        MoveBarrierCommand cmd = new MoveBarrierCommand(barrier.getId(), freeField.getId());

        FrontendEvent result = movementService.moveBarrier(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveBarrierRejectedEvent.class, result);

        FrontendMoveBarrierRejectedEvent evt = (FrontendMoveBarrierRejectedEvent) result;
        assertEquals("BARRIER_MOVE_ERROR", evt.type());
        assertEquals("Cant place a barrier on an occupied Field", evt.msg());
    }

}
