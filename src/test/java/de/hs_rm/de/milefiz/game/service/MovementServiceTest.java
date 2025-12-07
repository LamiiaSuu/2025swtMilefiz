package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendDuelEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMeepleReachedEndEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveWithLossEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRejectedByBarrierEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendTriggerBarrierMoveEvent;

@ExtendWith(MockitoExtension.class)
public class MovementServiceTest {

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
    private Meeple meeple;
    private Field currentField;
    private Field nextField;
    private final int MOVES = 5;
    private final int NO_MOVES = 0;
    private final int LAST_MOVE = 1;
    private final int SECOND_TO_LAST_MOVE = 2;

    @BeforeEach
    void setUp() throws LobbyNotFoundException {
        movementService = new MovementServiceImpl(lobbyManager);

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
        when(principal.getName()).thenReturn("player1");
        when(sha.getSessionId()).thenReturn("session1");
    }

    // Erfolgreicher Move
    // TODO alle grenzfaelle hinzufuegen
    @Test
    void moveMeepleSuccessfulMoveReturnsFrontendMoveEvent() {

        Field furtherField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(furtherField, Direction.NORTH);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

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

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("no moves left", evt.msg());
    }

    // Versuch in eine Richtung zu ziehen, in der kein Feld ist.
    @Test
    void moveMeepleToNonExistingFieldIsRejected() {

        // kein Feld in Richtung South
        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.SOUTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("No Field in this Direction", evt.msg());
    }

    // Versuch die Richtung innerhalb eines Zuges zu wechseln
    @Test
    void moveMeepleChangeDirectionIsRejected() {

        // Meeple zieht aufs naechste Feld
        meeple.setCurrentField(nextField);

        // Jetzt versucht er, zurueck nach Sueden zu gehen (auf lastField)
        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.SOUTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("Cant change direction!", evt.msg());
    }

    // Versuch auf ein Startfeld zu gehen
    @Test
    void moveMeepleIntoStartFieldIsRejected() {

        Field startField = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(1, 0));

        currentField.addNeighbour(startField, Direction.EAST);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.EAST);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);
        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("Cant go back to a starting field!", evt.msg());
    }

    // Versuch Endfeld mit Restzügen zu betreten
    @Test
    void moveMeepleToEndFieldWithRemainingMovesIsRejected() {

        Field endField = new Field(UUID.randomUUID(), FieldType.END, new Position(1, 0));

        currentField.addNeighbour(endField, Direction.EAST);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.EAST);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("Cant enter End with remaining Moves", evt.msg());
    }

    // Erfolgreiches betreten des Ziels
    @Test
    void moveMeepleLastMoveOntoEndFieldTriggersMeepleReachedEndEvent() {

        player.setRemainingMoves(LAST_MOVE);

        meeple.setCurrentField(nextField);

        // Endfeld nördlich vom aktuellen Feld
        Field endField = new Field(UUID.randomUUID(), FieldType.END, new Position(0, 2));
        nextField.addNeighbour(endField, Direction.NORTH);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        // Meeple erreicht das Ziel
        assertInstanceOf(FrontendMeepleReachedEndEvent.class, result);

        FrontendMeepleReachedEndEvent evt = (FrontendMeepleReachedEndEvent) result;

        assertEquals(meeple.getId(), evt.id());

        // SMeeple wurde bei Spieler entfernt
        assertThrows(IllegalArgumentException.class,
                () -> player.getMeepleWithId(meeple.getId()));

        // Spieler hat keine Züge mehr
        assertEquals(0, player.getRemainingMoves());
    }

    // Verlust der ueberschuessigen Moves beim betreten einer Sackgasse NUR durch
    // Barrieren
    @Test
    void moveMeepleEntersBarrierDeadEndTriggersMoveWithLossEvent() {

        // Nachbarfelder
        Field blocked = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(1, 1));

        nextField.addNeighbour(blocked, Direction.NORTH);

        // Barrieren auf diesen beiden Nachbarfeldern
        Meeple barrier = new Meeple(true);

        barrier.setCurrentField(blocked);

        board.addBarrier(barrier);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        // Bewegung ausführen
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        // Erwartung: Zug endet automatisch
        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        // Prüfen, dass der Meeple tatsächlich auf dem Dead-End-Feld steht
        assertEquals(nextField, meeple.getCurrentField());
    }

    // Verlust der ueberschuessigen Moves beim betreten einer Sackgasse durch
    // Barrieren UND Startfelder
    @Test
    void moveMeepleEntersBarrierAndStartDeadEndTriggersMoveWithLossEvent() {

        // Nachbarfelder
        Field blocked = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(1, 1));
        Field startGreen = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(1, 0));

        nextField.addNeighbour(blocked, Direction.NORTH);
        nextField.addNeighbour(startGreen, Direction.WEST);

        // Barrieren auf diesen beiden Nachbarfeldern
        Meeple barrier = new Meeple(true);

        barrier.setCurrentField(blocked);

        board.addBarrier(barrier);

        board.setStartGreen(startGreen);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        // Bewegung ausführen
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        // Erwartung: Zug endet automatisch
        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        // Prüfen, dass der Meeple tatsächlich auf dem Dead-End-Feld steht
        assertEquals(nextField, meeple.getCurrentField());
    }

    // Verlust der ueberschuessigen Moves beim betreten einer Sackgasse durch
    // Barrieren UND Startfelder UND Ziel
    @Test
    void moveMeepleEntersBarrieAndStartAndEndDeadEndTriggersMoveWithLossEvent() {

        // Nachbarfelder
        Field blocked = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(1, 1));
        Field startGreen = new Field(UUID.randomUUID(), FieldType.START_GREEN, new Position(1, 0));
        Field endField = new Field(UUID.randomUUID(), FieldType.END, new Position(0, 2));

        nextField.addNeighbour(endField, Direction.EAST);
        nextField.addNeighbour(blocked, Direction.NORTH);
        nextField.addNeighbour(startGreen, Direction.WEST);

        // Barrieren auf diesen beiden Nachbarfeldern
        Meeple barrier = new Meeple(true);

        barrier.setCurrentField(blocked);

        board.addBarrier(barrier);

        board.setStartGreen(startGreen);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        // Bewegung ausführen
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        // Erwartung: Zug endet automatisch
        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        // Prüfen, dass der Meeple tatsächlich auf dem Dead-End-Feld steht
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
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveEvent.class, result);
    }

    // Verlust der restlichen Moves, wenn man in eine Barriere rennt
    @Test
    void moveMeepleRunsIntoBarrierLosesRemainingMoves() {

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Meeple barrier = new Meeple(true);
        barrier.setCurrentField(nextField);
        board.addBarrier(barrier);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendRejectedByBarrierEvent.class, result);

        assertEquals(currentField, meeple.getCurrentField());

        assertEquals(0, player.getRemainingMoves());
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

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendTriggerBarrierMoveEvent.class, result);

        FrontendTriggerBarrierMoveEvent evt = (FrontendTriggerBarrierMoveEvent) result;
        assertEquals(meeple.getId(), evt.meepleId());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(barrier.getId(), evt.barrierId());

        assertEquals(nextField, meeple.getCurrentField());

        assertEquals(0, player.getRemainingMoves());
    }

    // Versuch auf ein Feld zu ziehen, auf dem bereits ein eigener Meeple steht
    @Test
    void moveMeepleOntoOwnMeepleIsRejected() {

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Meeple otherMeeple = player.getMeeples()[1];
        otherMeeple.setCurrentField(nextField);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveRejectedEvent.class, result);

        FrontendMoveRejectedEvent evt = (FrontendMoveRejectedEvent) result;
        assertEquals("MOVE_ERROR", evt.type());
        assertEquals("Attempt to occupy a field with multiple meeple failed", evt.msg());

        assertEquals(currentField, meeple.getCurrentField());
    }

    // Move in Sackgasse aus eigenen Meeplen fuehrt zu Verlust des letzten Moves,
    // wenn man sich mit dem vorletzten in die Sackgasse begibt
    @Test
    void moveMeepleEntersDeadEndOfOwnMeeplesTriggersMoveWithLossEvent() {
        // Spieler hat genau SECOND_TO_LAST_MOVE übrig
        player.setRemainingMoves(SECOND_TO_LAST_MOVE);

        // 🧩 Setup: nextField ist umstellt von eigenen Meeplen
        Field north = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        Field west = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(-1, 1));

        nextField.addNeighbour(north, Direction.NORTH);
        nextField.addNeighbour(west, Direction.WEST);

        // Eigene Meeple auf den Nachbarfeldern platzieren
        Meeple m1 = player.getMeeples()[1];
        Meeple m2 = player.getMeeples()[2];
        m1.setCurrentField(north);
        m2.setCurrentField(west);

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        // Bewegung ausführen
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        // ✅ Erwartung: MoveWithLossEvent, Zug endet
        assertInstanceOf(FrontendMoveWithLossEvent.class, result);

        FrontendMoveWithLossEvent evt = (FrontendMoveWithLossEvent) result;
        assertEquals(meeple.getId(), evt.id());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        // Meeple steht jetzt auf nextField
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
        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

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

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendMoveEvent.class, result);

        FrontendMoveEvent evt = (FrontendMoveEvent) result;

        assertEquals(nextField.getId(), evt.targetField());

        assertEquals(MOVES - 1, evt.remainingMoves());
    }

    // Duell, wenn man mit dem letzte Move auf einem Feld mit einem gegnerischen
    // Meeple landet
    @Test
    void moveMeepleOnLastMoveOntoRivalMeepleTriggersDuelEvent() {
        player.setRemainingMoves(LAST_MOVE);

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Player rival = new Player(Color.BLUE);
        Meeple rivalMeeple = rival.getMeeples()[0];
        rivalMeeple.setCurrentField(nextField);

        lobby.setPlayers(List.of(player, rival));

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        assertInstanceOf(FrontendDuelEvent.class, result);

        FrontendDuelEvent evt = (FrontendDuelEvent) result;
        assertEquals(meeple.getId(), evt.firstMeepleId());
        assertEquals(rivalMeeple.getId(), evt.secondMeepleId());
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(0, evt.remainingMoves());

        assertEquals(nextField, meeple.getCurrentField());

        assertEquals(0, player.getRemainingMoves());
    }

    // gegnerischer Meeple wird uebersprungen, wenn man nicht mit dem letzten Move
    // auf ihm landet
    @Test
    void moveMeepleOntoRivalMeepleWithoutLastMoveDoesNotTriggerDuel() {

        Field dummyField = new Field(UUID.randomUUID(), FieldType.NORMAL, new Position(0, 2));
        nextField.addNeighbour(dummyField, Direction.NORTH);

        Player rival = new Player(Color.BLUE);
        Meeple rivalMeeple = rival.getMeeples()[0];
        rivalMeeple.setCurrentField(nextField);

        lobby.setPlayers(List.of(player, rival));

        MovementCommand cmd = new MovementCommand(meeple.getId(), Direction.NORTH);

        FrontendEvent result = movementService.moveMeeple(lobby.getId(), cmd, principal, sha);

        // ✅ Erwartung: normaler Move, kein Duell
        assertInstanceOf(FrontendMoveEvent.class, result);

        FrontendMoveEvent evt = (FrontendMoveEvent) result;
        assertEquals(nextField.getId(), evt.targetField());
        assertEquals(MOVES - 1, evt.remainingMoves());
    }

}
