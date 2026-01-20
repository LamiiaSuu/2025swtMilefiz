package de.hs_rm.de.milefiz.game.service;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Field;

@Service
public class BoardServiceImpl implements BoardService {

    private final Logger LOGGER = LoggerFactory.getLogger(BoardService.class);

    /**
     * Validiert ein Board nach folgenden Kriterien:
     *
     * <ul>
     * <li>Es müssen mindestens 4 Startfelder existieren</li>
     * <li>Es muss mindestens 1 Ziel existieren</li>
     * <li>Das Ziel muss von überall aus erreichbar sein</li>
     * </ul>
     */
    @Override
    public boolean validateBoard(Board board) throws BoardValidateException {
        if (board == null) {
            LOGGER.debug("Board besitzt nicht alle Startfelder");
            throw new BoardValidateException("Board hat keine Felder");
        }
        // Alle Felder zusammensuchen
        Set<Field> startFields = new HashSet<>();
        if (board.getStartRed() != null) {
            startFields.add(board.getStartRed());
        }
        if (board.getStartGreen() != null) {
            startFields.add(board.getStartGreen());
        }
        if (board.getStartBlue() != null) {
            startFields.add(board.getStartBlue());
        }
        if (null != board.getStartYellow()) {
            startFields.add(board.getStartYellow());
        }

        // Mehr als 2 Startfelder?
        if (startFields.size() < 4) {
            throw new BoardValidateException("Es müssen mindestens 4 Startfelder existieren");
        }

        // Expandiere alle Startfelder und suche nach Zielen, ob diese erreichbar sind
        for (Field startField : startFields) {
            if (!expandFieldAndCheckGoalAvailable(startField, new HashSet<>())) {
                LOGGER.debug(String.format("%s ist nicht mit dem Ziel verbunden", startField.getType().name()));
                throw new BoardValidateException(String.format("%s ist nicht mit einem Ziel verbunden", startField.getType().name()));
            }
            if (!isStartFieldValid(startField)) {
                throw new BoardValidateException(String.format("%s darf nur eine Verbindung haben", startField.getType().name()));
            }
        }
        LOGGER.debug("Map-Validierung abgeschlossen, erfolgreich!");
        return true;
    }

    /**
     * Rekursive Methode zum expandieren von einem Feld aus. Es expandiert in
     * alle Richtungen, bis es ein Ziel findet
     *
     * @param field das Feld von dem aus gestartet wird
     * @param expandedFields bereits expandierte Fields
     * @return true, wenn es ein Ziel findet
     */
    private boolean expandFieldAndCheckGoalAvailable(Field field, Set<Field> expandedFields) {
        expandedFields.add(field);
        if (field.getType().isEnd()) {
            return true;
        }
        for (Field neighbour : field.getNeighbours().values()) {
            if (!expandedFields.contains(neighbour)) {
                if (expandFieldAndCheckGoalAvailable(neighbour, expandedFields)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Überprüft, ob ein Startfeld ansich valide ist. Es wird geprüft, ob es
     * GENAU EINE connection gibt, sodass hinter dem Start kein weiteres Feld
     * sein darf.
     * @param field
     * @return
     */
    private boolean isStartFieldValid(Field field) {
        // Das Startfeld sollte genau einen Nachbarn haben
        return field.getNeighbours().size() == 1;
    }
}
