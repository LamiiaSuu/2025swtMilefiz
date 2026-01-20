package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.FieldType;

@SpringBootTest
class BoardValidateTest {

    @Autowired
    private BoardService boardService;

    @Test
    @DisplayName("Es muss mindestens 2 Startfelder geben")
    void testStartFields() {       // Board mit nur einem Startfeld -> muss fehlschlagen
        Board board = new Board();

        Field startRed = new Field(FieldType.START_RED, null);
        board.setStartRed(startRed);

        assertThrows(BoardValidateException.class, () -> boardService.validateBoard(board));
    }

    @Test
    @DisplayName("Es muss mindesten 1 Ende geben")
    void testEndFields() {
        // Board mit 2 Startfeldern, aber ohne END-Feld -> muss fehlschlagen
        Board board = new Board();

        Field startRed = new Field(FieldType.START_RED, null);
        Field startBlue = new Field(FieldType.START_BLUE, null);

        // einfache Verbindung ohne Ziel
        Field normal = new Field(FieldType.NORMAL, null);
        startRed.getNeighbours().put(null, normal); // Richtung egal, nur Verbindung nötig
        normal.getNeighbours().put(null, startRed);

        board.setStartRed(startRed);
        board.setStartBlue(startBlue);

        assertThrows(BoardValidateException.class, () -> boardService.validateBoard(board));

    }

    @Test
    @DisplayName("Jedes Ziel muss erreichbar sein")
    void testEndFieldAccess() {
        // Ein Start erreicht das Ziel, das andere Startfeld nicht -> muss fehlschlagen
        Board board = new Board();

        Field startRed = new Field(FieldType.START_RED, null);
        Field startBlue = new Field(FieldType.START_BLUE, null);
        Field mid = new Field(FieldType.NORMAL, null);
        Field end = new Field(FieldType.END, null);

        // Pfad: startRed -> mid -> end
        startRed.getNeighbours().put(null, mid);
        mid.getNeighbours().put(null, startRed);
        mid.getNeighbours().put(null, end);
        end.getNeighbours().put(null, mid);

        // startBlue ohne Verbindung zum Ziel
        Field alone = new Field(FieldType.NORMAL, null);
        startBlue.getNeighbours().put(null, alone);
        alone.getNeighbours().put(null, startBlue);

        board.setStartRed(startRed);
        board.setStartBlue(startBlue);

        assertThrows(BoardValidateException.class, () -> boardService.validateBoard(board));
    }

    @Test
    @DisplayName("Ein korrektes Board")
    void testValidBoard() {
        Board board = new Board();

        Field startRed = new Field(FieldType.START_RED, null);
        Field startBlue = new Field(FieldType.START_BLUE, null);
        Field startYellow = new Field(FieldType.START_YELLOW, null);
        Field startGreen = new Field(FieldType.START_GREEN, null);
        Field field1 = new Field(FieldType.NORMAL, null);
        Field field2 = new Field(FieldType.NORMAL, null);
        Field end = new Field(FieldType.END, null);

        // Gemeinsamer Pfad zum Ziel: beide Starts -> mid1 -> mid2 -> end
        startRed.getNeighbours().put(null, field1);
        field1.getNeighbours().put(null, startRed);

        startBlue.getNeighbours().put(null, field1);
        field1.getNeighbours().put(null, startBlue);

        startYellow.getNeighbours().put(null, field1);
        field1.getNeighbours().put(null, startRed);

        startGreen.getNeighbours().put(null, field1);
        field1.getNeighbours().put(null, startRed);

        field1.getNeighbours().put(null, field2);
        field2.getNeighbours().put(null, field1);

        field2.getNeighbours().put(null, end);
        end.getNeighbours().put(null, field2);

        board.setStartRed(startRed);
        board.setStartBlue(startBlue);
        board.setStartGreen(startGreen);
        board.setStartYellow(startYellow);

        // darf keine Exception werfen
        assertDoesNotThrow(() -> boardService.validateBoard(board));
    }
}
