package de.hs_rm.de.milefiz.game.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendDuelEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveBarrierEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveBarrierRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveWithLossEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendPlayerHasWonEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRejectedByBarrierEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendTriggerBarrierMoveEvent;

/**
 * Implementierung des {@link MovementService}, die die Spiellogik für
 * Bewegungen im Spiel Milefiz kapselt.
 *
 * Diese Service-Klasse ist für die Verarbeitung und Validierung von
 * Bewegungsbefehlen zuständig, die vom Frontend über WebSocket-Nachrichten
 * empfangen werden. Sie prüft, ob ein Zug nach den Spielregeln gültig ist,
 * führt die Bewegung auf dem Spielfeld aus und erzeugt das passende
 * {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent} für das Frontend.
 *
 * Hauptaufgaben dieser Klasse:
 * - Validierung eingehender Bewegungsbefehle (z. B. Richtung, erlaubte Felder)
 * - Aktualisierung der Spielfiguren-Positionen (Meeples) auf dem Board
 * - Behandlung spezieller Spielsituationen wie:
 * - - Bewegung in eine Barriere
 * - - direktes Landen auf einer Barriere (Barriere darf verschoben werden)
 * - - Bewegungen in Sackgassen (Barrieren oder eigene Meeples)
 * - - Duelle zwischen Meeples verschiedener Spieler
 * - - Erreichen des Zielfelds („End“) und Entfernen des Meeples vom Spielfeld
 * - Verwaltung der verbleibenden Bewegungen pro Spieler
 * - Kommunikation mit dem Frontend über Ereignisse
 *
 * Verwendet intern:
 * - {@link de.hs_rm.de.milefiz.game.lobby.LobbyManager} zur Verwaltung der
 * Lobbys
 * - {@link de.hs_rm.de.milefiz.game.model.Board},
 * {@link de.hs_rm.de.milefiz.game.model.Meeple},
 * {@link de.hs_rm.de.milefiz.game.model.Field} und
 * {@link de.hs_rm.de.milefiz.game.model.Player}
 * zur Repräsentation des Spielzustands
 *
 * Alle relevanten Spielregeln (wie das Verlieren von Restzügen, Barrierenlogik
 * oder Duelle) sind in dieser Klasse zentral implementiert.
 *
 * Author: Maximilian Ressel
 */
@Service
public class MovementServiceImpl implements MovementService {

    private final Logger logger = LoggerFactory.getLogger(MovementServiceImpl.class);
    private LobbyManager lobbyManager;
    private static final int LAST_MOVE = 1;
    private static final int SECOND_TO_LAST_MOVE = 2;
    private static final boolean TESTING_LOCALLY = false; // true wenn es bei sich lokal laufen lässt, damit die
                                                         // barriere vorerst randomly verschoben wird.
                                                         // muss false sein für die unit tests

    /**
     * Erstellt eine neue Instanz des MovementServiceImpl.
     *
     * @param lobbyManager der {@link LobbyManager}, der zum Verwalten und Abrufen
     *                     der Lobbys verwendet wird
     *
     *                     Author: Maximilian Ressel
     */
    public MovementServiceImpl(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    /**
     * Führt die Bewegung eines bestimmten Meeples eines Spielers innerhalb einer
     * bestimmten Lobby aus
     * und wendet dabei alle Bewegungsregeln des Spiels Milefiz an.
     *
     * Diese Methode verarbeitet den vom Frontend empfangenen Bewegungsbefehl
     * und entscheidet anhand des aktuellen Spielfeldzustands, ob und wie
     * die Bewegung ausgeführt werden kann. Je nach Spielsituation werden
     * unterschiedliche Ereignisse (Events) an das Frontend zurückgegeben.
     *
     * Ablauf der Methode:
     * - Ermittlung der betroffenen Lobby, des Spielers und des zu bewegenden
     * Meeples
     * - Berechnung des Zielfelds anhand der angegebenen Bewegungsrichtung
     * - Validierung gegen Regelverletzungen (z. B. Rückwärtslaufen oder Betreten
     * eines Startfelds)
     * - Prüfung auf das Erreichen des Zielfelds (Endfeld):
     * - Wenn der Meeple exakt auf dem Endfeld landet, wird er entfernt.
     * - Wenn der Spieler dadurch keine Meeples mehr besitzt, hat er das Spiel
     * gewonnen.
     * - Behandlung spezieller Spielsituationen:
     * - Bewegung in eine Barriere (alle restlichen Schritte verfallen)
     * - Direktes Landen auf einer Barriere (die Barriere darf anschließend
     * verschoben werden)
     * - Bewegung in Sackgassen, die nur Barrieren oder eigene Meeples als Nachbarn
     * haben
     * - Duelle mit Meeples anderer Spieler, wenn man auf deren Feld landet
     * - Aktualisierung des Spielfeldzustands und Verwaltung der verbleibenden
     * Bewegungen
     *
     * Abhängig vom Ergebnis wird eines der folgenden Events an das Frontend
     * gesendet:
     * - {@link de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent} bei
     * erfolgreicher Bewegung
     * - {@link de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent} bei
     * ungültigem Zug
     * - {@link de.hs_rm.de.milefiz.messaging.events.FrontendMoveWithLossEvent} wenn
     * verbleibende Schritte verfallen
     * -
     * {@link de.hs_rm.de.milefiz.messaging.events.FrontendTriggerBarrierMoveEvent}
     * wenn der Meeple direkt auf einer Barriere landet
     * - {@link de.hs_rm.de.milefiz.messaging.events.FrontendRejectedByBarrierEvent}
     * wenn der Meeple gegen eine Barriere läuft und der Zug endet
     * - {@link de.hs_rm.de.milefiz.messaging.events.FrontendMeepleReachedEndEvent}
     * wenn ein Meeple das Zielfeld erreicht
     * - {@link de.hs_rm.de.milefiz.messaging.events.FrontendPlayerHasWonEvent} wenn
     * ein Spieler alle Meeples entfernt hat und gewinnt
     * - {@link de.hs_rm.de.milefiz.messaging.events.FrontendDuelEvent} wenn ein
     * Duell zwischen zwei Meeples ausgelöst wird
     *
     * @param lobbyId die eindeutige ID der Lobby, in der die Bewegung stattfindet
     * @param moveCmd der vom Frontend übermittelte Bewegungsbefehl mit Meeple-ID
     *                und Bewegungsrichtung
     * @param player  der Spieler (bzw. dessen Benutzerkontext), der den Zug
     *                ausführt
     * @return ein {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent}, das
     *         das Ergebnis der Bewegung beschreibt
     *
     *         Author: Maximilian Ressel
     */
    @Override
    public FrontendEvent moveMeeple(UUID lobbyId, MovementCommand moveCmd, Player player) {

        logger.info("Moving meeple {} from player '{}' in lobby {} in direction {} (sessionId={})",
                moveCmd.meepleId(),
                player != null ? player.getName() : "anonymous",
                lobbyId,
                moveCmd.direction());

        Lobby lobby = null;
        try {
            lobby = lobbyManager.getLobby(lobbyId);
        } catch (LobbyNotFoundException e) {
            e.printStackTrace();
        }

        Board board = lobby.getBoard();
        Meeple meeple = player.getMeepleWithId(moveCmd.meepleId());
        Field currentField = meeple.getCurrentField();
        Field lastField = meeple.getLastField();
        Direction direction = moveCmd.direction();

        // Wenn keine weiteren Schritte verfügbar sind, kann man man sich nicht bewegen
        if (!player.canMove()) {
            logger.info("No more moves left");
            return new FrontendMoveRejectedEvent("no moves left");
        }

        // Ziel-Feld anhand der Bewegungsrichtung bestimmen
        Field nextField = switch (direction) {
            case NORTH -> currentField.getNorth();
            case EAST -> currentField.getEast();
            case SOUTH -> currentField.getSouth();
            case WEST -> currentField.getWest();
        };

        // FELD EXISTIERT NICHT
        // Fehler, wenn in der angegeben Richtung kein Feld ist
        if (nextField == null) {
            logger.info("No Field in this Direction");
            return new FrontendMoveRejectedEvent("No Field in this Direction");
        }

        // RICHTUNGSWECHSEL
        // Fehler bei Versuch das Feld zu betreten auf dem man zuletzt war
        // (Richtungswechsel ist verboten)
        if (lastField != null && nextField.equals(lastField)) {
            logger.info("Cant change direction!");
            return new FrontendMoveRejectedEvent("Cant change direction!");
        }

        // START
        // Nachdem das Startfeld verlassen wurde, kann man nicht zurückkehren (damit
        // kann man auch nicht die der anderen betreten)
        if (nextField.getType().isStart()) {
            logger.info("Cant go back to a starting field!");
            return new FrontendMoveRejectedEvent("Cant go back to a starting field!");
        }

        // ZIEL
        // Man kann das Ziel nur betreten, wenn man exakt darauf endet
        if (nextField.getType().isEnd()) {
            // Wenn man darauf endet, wird der meeple entfernt.
            if (player.getRemainingMoves() == LAST_MOVE) {
                player.useMove();
                logger.info("player {} has won", player.getId());
                return new FrontendPlayerHasWonEvent(player.getId(), meeple.getId(), nextField.getId());
            }
            logger.info("Cant enter End with remaining moves");
            return new FrontendMoveRejectedEvent("Cant enter End with remaining Moves");
        }

        // SACKGASSE DURCH BARRIEREN
        // Wenn man ein Feld betritt, das als einzig angrenzende Felder Barrieren
        // und/oder nicht betretbare Felder hat,
        // wird der Zug automatisch beendet ohne dass man sich noch in Richtung der
        // Barriere bewegen muss, außer man macht gerade seinen vorletzten Schritt,
        // was bedeutet, dass man direkt auf der Barriere oder dem Ziel landen kann.
        if ((hasOnlyBarrierNeighbours(nextField, currentField, board))
                && (player.getRemainingMoves() != SECOND_TO_LAST_MOVE)) {
            endTurnWithMove(player, meeple, nextField);
            logger.info("All possible moves would lead into Barriers, player loses remaining Moves, turn is over");
            return new FrontendMoveWithLossEvent(
                    meeple.getId(),
                    nextField.getId(),
                    player.getRemainingMoves());
        }

        // BARRIERE
        // Wenn man in eine Barriere läuft, verliert man seine restlichen Schritte,
        // außer man landet genau darauf
        for (Meeple tempBarrier : board.getBarriers()) {
            if (tempBarrier.getCurrentField() != null && tempBarrier.getCurrentField().equals(nextField)) {
                // wenn man genau drauf landet, darf man sie verschieben
                if (player.getRemainingMoves() == LAST_MOVE) {
                    meeple.setCurrentField(nextField);
                    meeple.clearLastField();
                    player.useMove();
                    logger.info("Direct hit on barrier {} with meeple {}", tempBarrier.getId(), meeple.getId());
                    return new FrontendTriggerBarrierMoveEvent(
                            meeple.getId(),
                            nextField.getId(),
                            player.getRemainingMoves(),
                            tempBarrier.getId());
                }
                // ansonsten wird der zug beendet
                player.setRemainingMoves(0);
                meeple.clearLastField();
                logger.info("ran into barrier, cant go any further! (loses remaining moves)");
                return new FrontendRejectedByBarrierEvent(player.getRemainingMoves());
            }
        }

        // Felder auf denen die eigenen Meeple sich befinden
        List<Field> ownMeepleFields = Arrays.stream(player.getMeeples())
                .map(Meeple::getCurrentField)
                .filter(Objects::nonNull)
                .toList();

        // Ueberpruefen, ob das Zielfeld durch einen eigenen Meeple blockiert ist
        if (player.getRemainingMoves() == LAST_MOVE && ownMeepleFields.contains(nextField)) {
            logger.info("Attempt to occupy a field with multiple meeple failed");
            return new FrontendMoveRejectedEvent("Attempt to occupy a field with multiple meeple failed");
        }

        // SACKGASSE DURCH EIGENE MEEPLE
        // Ueberpruefen, ob sich der Spieler,
        // abgesehen vom aktuellen Feld,
        // in eine Sackgasse aus eigenen Meeplen bewegt
        if (player.getRemainingMoves() == SECOND_TO_LAST_MOVE) {
            if (hasOnlyOwnMeepleNeighbours(nextField, currentField, ownMeepleFields)) {
                // Spieler kann im nächsten Zug nur zurück oder auf eigenen Meeple, also
                // verfällt der letzte Schritt
                endTurnWithMove(player, meeple, nextField);
                logger.info("Player entered dead-end");
                return new FrontendMoveWithLossEvent(
                        meeple.getId(),
                        nextField.getId(),
                        player.getRemainingMoves());
            }
        }

        // DUELL
        // Sonderfaelle wenn es sich um den letzten Zug handelt
        if (player.getRemainingMoves() == LAST_MOVE) {

            // Duell einleiten, wenn man auf einem Feld landet, auf dem ein Meeple eines
            // anderen Spielers steht
            for (Player rivalPlayer : lobby.getPlayers()) {
                if (player.equals(rivalPlayer)) {
                    continue;
                }
                for (Meeple rivalMeeple : rivalPlayer.getMeeples()) {
                    if (rivalMeeple.getCurrentField().equals(nextField)) {
                        meeple.setCurrentField(nextField);
                        meeple.clearLastField();
                        player.useMove();
                        logger.info("Initiating duel between meeple {} and meeple {}", meeple.getId(),
                                rivalMeeple.getId());
                        return new FrontendDuelEvent(meeple.getId(), rivalMeeple.getId(), nextField.getId(),
                                player.getRemainingMoves());
                    }
                }
            }
        }

        // Spielfeld-Zustand aktualisieren
        // lastField wird jetzt im Meeple.setCurrentField aktualisiert
        meeple.setCurrentField(nextField);

        // Spieler nutzt einen Zug
        player.useMove();
        if (player.getRemainingMoves() == 0) {
            meeple.clearLastField();
        }

        // Erfolgreiche Bewegung an Clients senden
        FrontendMoveEvent move = new FrontendMoveEvent(
                player.getId(),
                meeple.getId(),
                nextField.getId(),
                player.getRemainingMoves());

        logger.info("Meeple {} moved to {} ({} remaining moves)", meeple.getId(), nextField.getId(),
                player.getRemainingMoves());
        return move;
    }

    /**
     * Prüft, ob das angegebene Zielfeld ausschließlich Nachbarfelder besitzt,
     * die entweder Felder mit Barrieren sind oder nicht betretbare Felder sind.
     *
     * Diese Methode dient dazu festzustellen, ob ein Spieler sich auf ein Feld
     * bewegt, von dem aus keine weiteren regulären Bewegungen mehr möglich sind,
     * weil alle angrenzenden Felder (außer dem, von dem der Spieler kam) durch
     * Barrieren blockiert werden oder nicht betretbar sind.
     * In diesem Fall verfallen die restlichen Schritte
     * des Spielers und der Zug endet automatisch.
     *
     * @param nextField    das Feld, auf das sich der Meeple bewegen möchte
     * @param currentField das Feld, auf dem sich der Meeple aktuell befindet
     * @param board        das aktuelle Spielfeld, das alle Barrieren kennt
     * @return true, wenn alle Nachbarfelder des Zielfelds entweder
     *         Felder mit Barrieren sind oder nicht betretbar sind,
     *         andernfalls false
     *
     *         Author: Maximilian Ressel
     */
    private boolean hasOnlyBarrierNeighbours(Field nextField, Field currentField, Board board) {
        return nextField.getNeighbours().values().stream().allMatch(
                neighbour -> neighbour.equals(currentField)
                        || neighbour.getType().isStart()
                        || neighbour.getType().isEnd()
                        || board.getBarriers().stream()
                                .map(Meeple::getCurrentField)
                                .filter(Objects::nonNull)
                                .anyMatch(neighbour::equals));
    }

    /**
     * Prüft, ob das angegebene Zielfeld ausschließlich Nachbarfelder besitzt,
     * die entweder Felder mit eigenen Meeples oder nicht betretbare Felder sind.
     *
     * @param nextField       das Feld, auf das sich der Meeple bewegen möchte
     * @param currentField    das Feld, auf dem sich der Meeple aktuell befindet
     * @param ownMeepleFields Liste aller Felder, auf denen sich eigene Meeples des
     *                        Spielers befinden
     * @return true, wenn alle Nachbarfelder des Zielfelds entweder
     *         Felder mit eigenen Meeples sind oder nicht betretbare Felder sind,
     *         andernfalls false
     * 
     *         Author: Maximilian Ressel
     */
    private boolean hasOnlyOwnMeepleNeighbours(Field nextField, Field currentField, List<Field> ownMeepleFields) {
        return nextField.getNeighbours().values().stream()
                .allMatch(neighbour -> neighbour.equals(currentField)
                        || neighbour.getType().isStart()
                        || ownMeepleFields.contains(neighbour));
    }

    /**
     * Beendet den aktuellen Zug eines Spielers, indem der übergebene Meeple
     * auf das angegebene Zielfeld bewegt wird und alle verbleibenden Bewegungen
     * des Spielers verfallen.
     *
     * Diese Methode wird in Situationen aufgerufen, in denen ein Spieler seinen
     * Zug nicht fortsetzen kann oder darf, beispielsweise wenn:
     * - das Zielfeld ausschließlich von Barrieren umgeben ist,
     * - sich der Spieler in eine Sackgasse aus eigenen Meeples bewegt,
     *
     * @param player    der Spieler, dessen Zug beendet wird
     * @param meeple    das Meeple, das bewegt wird
     * @param nextField das Zielfeld, auf das das Meeple gesetzt wird
     *
     *                  Author: Maximilian Ressel
     */
    private void endTurnWithMove(Player player, Meeple meeple, Field nextField) {
        meeple.setCurrentField(nextField);
        meeple.clearLastField();
        player.setRemainingMoves(0);
    }

    /**
     * Führt die Bewegung einer Barriere im Spiel aus, nachdem ein Spieler im
     * Frontend direkt auf einer Barriere gelandet ist und diese verschieben darf.
     *
     * Die Methode überprüft:
     * - ob das angegebene Ziel-Feld existiert,
     * - ob es kein Start- oder Zielfeld ist,
     * - und ob es nicht bereits durch ein anderes Objekt (Meeple oder Barriere)
     * besetzt ist.
     *
     * Ist das Feld gültig, wird die Barriere dort platziert und ein
     * {@link de.hs_rm.de.milefiz.messaging.events.FrontendMoveBarrierEvent}
     * erzeugt, um das Frontend über die neue Position zu informieren.
     * 
     * Andernfalls wird ein
     * {@link de.hs_rm.de.milefiz.messaging.events.FrontendMoveBarrierRejectedEvent}
     * zurückgegeben, um das Scheitern der Aktion mitzuteilen.
     *
     * @param lobbyId     die ID der Lobby, in der die Barriere verschoben wird
     * @param moveBarrCmd der vom Frontend übermittelte Befehl mit Barriere-ID und
     *                    Ziel-Feld-ID
     * @param player      der Spieler, der die Aktion ausführt
     * @return ein passendes {@link FrontendEvent}, das angibt, ob die Bewegung
     *         erfolgreich war oder nicht
     *
     *         Author: Maximilian Ressel
     */
    @Override
    public FrontendEvent moveBarrier(UUID lobbyId, MoveBarrierCommand moveBarrCmd, Player player) {

        logger.info(
                "Moving Barrier {} in lobby {} by player '{}' to field {} (sessionId={})",
                moveBarrCmd.barrierId(),
                lobbyId,
                player != null ? player.getName() : "anonymous",
                moveBarrCmd.targetFieldId());

        Lobby lobby = null;
        try {
            lobby = lobbyManager.getLobby(lobbyId);
        } catch (LobbyNotFoundException e) {
            e.printStackTrace();
        }

        Board board = lobby.getBoard();
        Meeple barrier = board.getBarrierById(moveBarrCmd.barrierId());

        // ⚠️ Temporärer Testcode:
        // zu testzwecken greifen wir auf ein zufälliges feld zurück um die checks
        // testen zu können.
        // TODO: Block aus !TESTING Übernehmen und TESTING streichen
        /**********************************************************************************************************************/

        Field targetField = board.getFieldById(getRandomField(board));

        if (!TESTING_LOCALLY) {
            targetField = board.getFieldById(moveBarrCmd.targetFieldId());
        }
        if (TESTING_LOCALLY) {
            while (targetField.getType().isEnd() || targetField.getType().isStart()
                    || isOccupied(lobby, board, targetField)) {
                targetField = board.getFieldById(getRandomField(board));
            }
        }
        /**********************************************************************************************************************/

        // Fehler, wenn es sich um ein Startfeld oder das Ende handelt
        if (targetField.getType().isEnd() || targetField.getType().isStart()) {
            logger.info("Cant place a barrier on Start or End");
            return new FrontendMoveBarrierRejectedEvent("Cant place a barrier on Start or End");
        }

        // Fehler wenn das Feld besetzt ist
        if (isOccupied(lobby, board, targetField)) {
            logger.info("Cant place a barrier on an occupied Field");
            return new FrontendMoveBarrierRejectedEvent("Cant place a barrier on an occupied Field");
        }

        barrier.setCurrentField(targetField);

        return new FrontendMoveBarrierEvent(barrier.getId(), targetField.getId());
    }

    /**
     * Prüft, ob ein bestimmtes Feld bereits durch einen Meeple oder eine Barriere
     * belegt ist.
     *
     * @param lobby die aktuelle Lobby mit allen Spielern und Meeples
     * @param board das aktuelle Spielfeld (enthält die Barrieren)
     * @param field das Feld, das überprüft werden soll
     * @return true, wenn das Feld besetzt ist, sonst false
     * 
     *         Author: Maximilian Ressel
     */
    private boolean isOccupied(Lobby lobby, Board board, Field field) {
        boolean occupiedByMeeple = lobby.getPlayers().stream()
                .flatMap(p -> Arrays.stream(p.getMeeples()))
                .map(Meeple::getCurrentField)
                .filter(Objects::nonNull)
                .anyMatch(f -> f.equals(field));

        if (occupiedByMeeple)
            return true;

        boolean occupiedByBarrier = board.getBarriers().stream()
                .map(Meeple::getCurrentField)
                .filter(Objects::nonNull)
                .anyMatch(f -> f.equals(field));

        return occupiedByBarrier;
    }

    /**
     * // ⚠️ Temporärer Testcode:
     * Wählt zufällig ein Feld des Spielfelds aus, das über die Startfelder des
     * Boards
     * erreichbar ist.
     * 
     * Diese Methode dient ausschließlich zu Testzwecken, um Bewegungen oder
     * Barrierenverschiebungen simulieren zu können, solange das Frontend noch keine
     * gültigen Feld-IDs übermittelt.
     *
     * @param board das aktuelle Spielfeld
     * @return die ID eines zufällig gewählten Feldes, das vom Start aus erreichbar
     *         ist
     *
     *         Author: Maximilian Ressel
     */
    private UUID getRandomField(Board board) {
        Set<Field> visited = new HashSet<>();
        Queue<Field> queue = new LinkedList<>();

        List<Field> starts = List.of(
                board.getStartGreen(),
                board.getStartYellow(),
                board.getStartBlue(),
                board.getStartRed());

        queue.addAll(starts);
        visited.addAll(starts);

        while (!queue.isEmpty()) {
            Field current = queue.poll();
            for (Field neighbour : current.getNeighbours().values()) {
                if (neighbour != null && !visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        List<UUID> fieldIds = visited.stream()
                .map(Field::getId)
                .toList();

        return fieldIds.get(new Random().nextInt(fieldIds.size()));
    }

}
