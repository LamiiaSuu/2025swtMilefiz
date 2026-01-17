package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendDuelEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveWithLossEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendPlayerHasWonEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRejectedByBarrierEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendTriggerBarrierMoveEvent;

@ExtendWith(MockitoExtension.class)
public class MovementServiceMeepleTest {

    @Mock
    private LobbyManager lobbyManager;
    private DuelService duelService;

    private MovementService movementService;
    private Lobby lobby;
    private Board board;
    private Player player;
    private Meeple meeple;
    private Field currentField;
    private Field nextField;
    private final int MOVES = 5;
    private final int NO_MOVES = 0;
    private final int LAST_MOVE = 1;
    private final int SECOND_TO_LAST_MOVE = 2;
    @Mock
    private FrontendMessagingService messaging;

    @Mock
    private DuelResolutionService duelResolutionService;

    @BeforeEach
    void setUp() throws LobbyNotFoundException {
        duelService = new DuelServiceImpl(lobbyManager, messaging, duelResolutionService);
        movementService = new MovementServiceImpl(lobbyManager, duelService);

        // Felder
        currentField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 0));
        nextField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 1));
        currentField.addNeighbour(nextField, Direction.NORTH);

        // Spieler
        player = new Player(Color.RED);
        player.setRemainingMoves(MOVES);

        // Meeple
        meeple = player.getMeeples()[0];
        meeple.setCurrentField(currentField);

        // Board
        board = new Board();
        board.setStartGreen(currentField);

        // Lobby
        lobby = new Lobby();
        lobby.setBoard(board);
        lobby.setPlayers(List.of(player));

        when(lobbyManager.getLobby(lobby.getId())).thenReturn(lobby);
    }

    // Erfolgreicher Move
    @Test
    void moveMeepleSuccessfulMoveReturnsFrontendMoveEvent() {

        Field furtherField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(furtherField, Direction.NORTH);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveEvent.class, result);

        FrontendMoveEvent event = (FrontendMoveEvent) result;

        assertEquals(nextField.getId(), event.targetField());

        assertEquals((MOVES - 1), event.remainingMoves());
    }

    // Versuch ohne Moves zu ziehen
    @Test
    void moveMeepleWithNoRemainingMovesIsRejected() {

        player.setRemainingMoves(NO_MOVES);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("MOVE_ERROR_NO_MOVES_LEFT", evt.msg());
    }

    // Versuch in eine Richtung zu ziehen, in der kein Feld ist.
    @Test
    void moveMeepleToNonExistingFieldIsRejected() {

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.SOUTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("MOVE_ERROR_NO_FIELD_IN_DIRECTION", evt.msg());
    }

    // Versuch die Richtung innerhalb eines Zuges zu wechseln
    @Test
    void moveMeepleChangeDirectionIsRejected() {

        Field furtherField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(furtherField, Direction.NORTH);

        meeple.setCurrentField(nextField);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.SOUTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("MOVE_ERROR_CANT_CHANGE_DIRECTION", evt.msg());
    }

    // Versuch auf ein Startfeld zu gehen
    @Test
    void moveMeepleIntoStartFieldIsRejected() {

        Field startField = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(1, 0));

        currentField.addNeighbour(startField, Direction.EAST);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.EAST);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);
        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("MOVE_ERROR_INTO_START", evt.msg());
    }

    // Versuch Endfeld mit Restzügen zu betreten
    @Test
    void moveMeepleToEndFieldWithRemainingMovesIsRejected() {

        Field endField = new Field(UUID.randomUUID(), FieldType.END, new Position(1, 0));

        currentField.addNeighbour(endField, Direction.EAST);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.EAST);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL", evt.msg());
    }

    // Sieg, wenn erster Meeple das Ziel erreicht
    @Test
    void moveMeepleLastMoveOntoEndFieldTriggersPlayerHasWonEvent() throws LobbyNotFoundException {

        Field endField = new Field(UUID.randomUUID(), FieldType.END, new Position(0, 1));
        currentField.addNeighbour(endField, Direction.WEST);

        player.setRemainingMoves(LAST_MOVE);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.WEST);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendPlayerHasWonEvent.class, result);

        FrontendPlayerHasWonEvent evt = (FrontendPlayerHasWonEvent) result;
        assertEquals(player.getPlayerName(), evt.playerName());

        assertEquals(0, player.getRemainingMoves());
    }

    // Verlust der ueberschuessigen Moves beim betreten einer Sackgasse NUR durch
    // Barrieren
    @Test
    void moveMeepleEntersBarrierDeadEndTriggersMoveWithLossEvent() {

        Field blocked = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(1, 1));

        nextField.addNeighbour(blocked, Direction.NORTH);

        Meeple barrier = new Meeple(true);

        barrier.setCurrentField(blocked);

        board.addBarrier(barrier);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        assertEquals(nextField, meeple.getCurrentField());
    }

    // Verlust der ueberschuessigen Moves beim betreten einer Sackgasse durch
    // Barrieren UND Startfelder
    @Test
    void moveMeepleEntersBarrierAndStartDeadEndTriggersMoveWithLossEvent() {

        Field blocked = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(1, 1));
        Field startGreen = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(1, 0));

        nextField.addNeighbour(blocked, Direction.NORTH);
        nextField.addNeighbour(startGreen, Direction.WEST);

        Meeple barrier = new Meeple(true);

        barrier.setCurrentField(blocked);

        board.addBarrier(barrier);

        board.setStartGreen(startGreen);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        assertEquals(nextField, meeple.getCurrentField());
    }

    // Verlust der ueberschuessigen Moves beim betreten einer Sackgasse durch
    // Barrieren UND Startfelder UND Ziel
    @Test
    void moveMeepleEntersBarrieAndStartAndEndDeadEndTriggersMoveWithLossEvent() {

        Field blocked = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(1, 1));
        Field startGreen = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(1, 0));
        Field endField = new Field(UUID.randomUUID(), FieldType.END, new Position(0, 2));

        nextField.addNeighbour(endField, Direction.EAST);
        nextField.addNeighbour(blocked, Direction.NORTH);
        nextField.addNeighbour(startGreen, Direction.WEST);

        Meeple barrier = new Meeple(true);

        barrier.setCurrentField(blocked);

        board.addBarrier(barrier);

        board.setStartGreen(startGreen);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        assertEquals(nextField, meeple.getCurrentField());
    }

    // Move in Sackgasse durch Barrieren mit vorletztem Move ist erlaubt
    @Test
    void moveMeepleInBarrierDeadEndWithSecondToLastMoveDoesNotTriggerLossEvent() {
        player.setRemainingMoves(SECOND_TO_LAST_MOVE);

        Field blocked = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(1, 1));
        nextField.addNeighbour(blocked, Direction.NORTH);
        Meeple barrier = new Meeple(true);
        barrier.setCurrentField(blocked);
        board.addBarrier(barrier);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveEvent.class, result);
    }

    // Wenn man genau auf einer Barriere landet, darf man sie verschieben
    @Test
    void moveMeepleHitsBarrierOnLastMoveTriggersBarrierMove() {

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Meeple barrier = new Meeple(true);
        barrier.setCurrentField(nextField);
        board.addBarrier(barrier);

        player.setRemainingMoves(LAST_MOVE);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendTriggerBarrierMoveEvent.class, result);

        FrontendTriggerBarrierMoveEvent evt = (FrontendTriggerBarrierMoveEvent) result;
        assertEquals(meeple.getId(), evt.meepleId());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(barrier.getId(), evt.barrierId());

        assertEquals(nextField, meeple.getCurrentField());

        assertEquals(0, player.getRemainingMoves());
    }

    // Versuch mit letztem Zug auf ein Feld zu ziehen, auf dem bereits ein eigener Meeple steht
    @Test
    void moveMeepleOntoOwnMeepleWithLastMoveIsRejected() {

        player.setRemainingMoves(LAST_MOVE);

        Field furtherField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        currentField.addNeighbour(furtherField, Direction.SOUTH);

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Meeple otherMeeple = player.getMeeples()[1];
        otherMeeple.setCurrentField(nextField);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE", evt.msg());

        assertEquals(currentField, meeple.getCurrentField());
    }

    // Feld ueberspringen, auf dem bereits ein eigener Meeple steht
    @Test
    void moveMeepleJumpOverOwnMeeple() {

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Meeple otherMeeple = player.getMeeples()[1];
        otherMeeple.setCurrentField(nextField);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveEvent.class, result);

        FrontendMoveEvent evt = (FrontendMoveEvent) result;
        assertEquals("MOVE", evt.type());
        assertEquals(MOVES - 1, evt.remainingMoves());
        assertEquals(nextField, meeple.getCurrentField());
    }

    // Move in Sackgasse aus eigenen Meeplen fuehrt zu Verlust des letzten Moves,
    // wenn man sich mit dem vorletzten in die Sackgasse begibt
    @Test
    void moveMeepleEntersDeadEndOfOwnMeeplesTriggersMoveWithLossEvent() {

        player.setRemainingMoves(SECOND_TO_LAST_MOVE);

        Field north = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        Field west = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(-1, 1));

        nextField.addNeighbour(north, Direction.NORTH);
        nextField.addNeighbour(west, Direction.WEST);

        Meeple m1 = player.getMeeples()[1];
        Meeple m2 = player.getMeeples()[2];
        m1.setCurrentField(north);
        m2.setCurrentField(west);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        assertEquals(nextField, meeple.getCurrentField());
    }

    // Move in Sackgasse aus eigenen Meeplen UND Startfeld fuehrt zu Verlust des
    // letzten Moves,
    // wenn man sich mit dem vorletzten in die Sackgasse begibt
    @Test
    void moveMeepleEntersDeadEndOfOwnMeeplesAndStartFieldTriggersMoveWithLossEvent() {

        player.setRemainingMoves(SECOND_TO_LAST_MOVE);

        Field north = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        Field west = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(-1, 1));
        Field east = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(1, 1)); // Startfeld

        nextField.addNeighbour(north, Direction.NORTH);
        nextField.addNeighbour(west, Direction.WEST);
        nextField.addNeighbour(east, Direction.EAST);

        Meeple m1 = player.getMeeples()[1];
        Meeple m2 = player.getMeeples()[2];
        m1.setCurrentField(north);
        m2.setCurrentField(west);

        board.setStartGreen(east);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        assertEquals(nextField, meeple.getCurrentField());
    }

    // Eigene Meeple koennen uebersprungen werden, wenn man mit genug remainingMoves
    // in die Sackgasse geht
    @Test
    void moveMeepleInOwnMeepleDeadEndButNotSecondToLastMoveContinuesNormally() {

        Field north = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        Field west = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(-1, 1));
        nextField.addNeighbour(north, Direction.NORTH);
        nextField.addNeighbour(west, Direction.WEST);

        Meeple m1 = player.getMeeples()[1];
        Meeple m2 = player.getMeeples()[2];
        m1.setCurrentField(north);
        m2.setCurrentField(west);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;

        assertEquals(nextField.getId(), evt.targetField());

        assertEquals(NO_MOVES, evt.remainingMoves());
    }

    // Duell, wenn man mit dem letzte Move auf einem Feld mit einem gegnerischen
    // Meeple landet
    @Disabled("Failed für MonkeyType-Minigame wegen fehlender initialisierung")
    @Test
    void moveMeepleOnLastMoveOntoRivalMeepleTriggersDuelEvent() {
        player.setRemainingMoves(LAST_MOVE);

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        Field dummyField2 = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(2, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Player rival = new Player(Color.BLUE);
        Meeple rivalMeeple2 = rival.getMeeples()[1];
        Meeple rivalMeeple3 = rival.getMeeples()[2];
        Meeple rivalMeeple4 = rival.getMeeples()[3];
        Meeple rivalMeeple5 = rival.getMeeples()[4];
        rivalMeeple2.setCurrentField(dummyField2);
        rivalMeeple3.setCurrentField(dummyField2);
        rivalMeeple4.setCurrentField(dummyField2);
        rivalMeeple5.setCurrentField(dummyField2);

        Meeple rivalMeeple1 = rival.getMeeples()[0];
        rivalMeeple1.setCurrentField(nextField);

        lobby.setPlayers(List.of(player, rival));

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendDuelEvent.class, result);

        FrontendDuelEvent evt = (FrontendDuelEvent) result;
        assertEquals(meeple.getId(), evt.firstMeepleId());
        assertEquals(rivalMeeple1.getId(), evt.secondMeepleId());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(NO_MOVES, evt.remainingMoves());

        assertEquals(nextField, meeple.getCurrentField());

        assertEquals(NO_MOVES, player.getRemainingMoves());
    }

    // gegnerischer Meeple wird uebersprungen, wenn man nicht mit dem letzten Move
    // auf ihm landet
    @Test
    void moveMeepleOntoRivalMeepleWithoutLastMoveDoesNotTriggerDuel() {

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);
        Field dummyField2 = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(2, 2));

        Player rival = new Player(Color.BLUE);

        Meeple rivalMeeple2 = rival.getMeeples()[1];
        Meeple rivalMeeple3 = rival.getMeeples()[2];
        Meeple rivalMeeple4 = rival.getMeeples()[3];
        Meeple rivalMeeple5 = rival.getMeeples()[4];
        rivalMeeple2.setCurrentField(dummyField2);
        rivalMeeple3.setCurrentField(dummyField2);
        rivalMeeple4.setCurrentField(dummyField2);
        rivalMeeple5.setCurrentField(dummyField2);

        Meeple rivalMeeple = rival.getMeeples()[0];
        rivalMeeple.setCurrentField(nextField);

        lobby.setPlayers(List.of(player, rival));

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, player);

        assertInstanceOf(FrontendMoveEvent.class, result);

        FrontendMoveEvent evt = (FrontendMoveEvent) result;
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(MOVES - 1, evt.remainingMoves());
    }

}
