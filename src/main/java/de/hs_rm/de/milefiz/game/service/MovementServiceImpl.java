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
 * Implementierung des {@link MovementService}, die für die komplette
 * Bewegungslogik von Meeples und Barrieren im Spiel verantwortlich ist.
 * 
 * Diese Service-Klasse kapselt sämtliche Regeln zur Spielerbewegung,
 * Interaktion mit Barrieren sowie Sonderfälle wie Duelle, Sackgassen
 * und das Erreichen des Zielfelds. Sie dient als zentrale Instanz zur
 * Validierung und Ausführung von Spielzügen innerhalb einer {@link Lobby}.
 *
 * Die {@code MovementServiceImpl} verarbeitet eingehende Bewegungs-
 * und Barrieren-Kommandos aus dem Frontend und erzeugt entsprechende
 * {@link FrontendEvent}s, die den aktualisierten Spielzustand oder
 * Ablehnungsgründe an die Clients zurückmelden.
 *
 * Unterstützte Spiellogiken umfassen unter anderem:
 * 
 * -> Bewegung von Meeples in kardinalen Richtungen
 * -> Verwaltung verbleibender Spielzüge pro Spieler
 * -> Erkennung ungültiger Züge (z.B. Richtungswechsel, blockierte Felder)
 * -> Interaktion mit Barrieren inklusive Verlust verbleibender Züge
 * -> Auslösen von Duellen zwischen gegnerischen Meeplen
 * -> Erkennen von Sackgassen durch Barrieren oder eigene Meeple
 * -> Erreichen des Zielfelds und Auslösen eines Spielsiegs
 * 
 * Die Klasse ist als Spring {@link Service} annotiert und wird über
 * Dependency Injection mit einem {@link LobbyManager} versorgt.
 *
 * @author Maximilian Ressel
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

    public MovementServiceImpl(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    /**
     * Bewegt einen Meeple eines Spielers um genau ein Feld in die angegebene
     * Richtung.
     * 
     * Die Methode verarbeitet einen {@link MovementCommand} und prüft anhand der
     * aktuellen Spielsituation, ob der Zug erlaubt ist. Dabei werden u.a. folgende
     * Regeln berücksichtigt:
     * 
     * -> Der Spieler muss noch verbleibende Bewegungen besitzen.
     * -> Das Zielfeld muss in der angegebenen Richtung existieren.
     * -> Ein Richtungswechsel (Zurückgehen auf das zuletzt betretene Feld)
     * ist nicht erlaubt.
     * -> Startfelder dürfen nach dem Verlassen nicht erneut betreten werden.
     * -> Zielfelder dürfen nur beim exakt letzten Schritt betreten werden.
     * -> Barrieren können Bewegungen blockieren oder – bei einem exakten Treffer
     * im letzten Schritt – eine Barrierenverschiebung auslösen.
     * -> Das Betreten eines Feldes mit eigenen Meeples ist im letzten Schritt
     * nicht erlaubt.
     * -> Trifft ein Meeple im letzten Schritt auf einen gegnerischen Meeple,
     * wird ein Duell ausgelöst.
     * -> Bewegungen in Sackgassen (durch Barrieren oder eigene Meeples)
     * können zum Verlust verbleibender Züge führen.
     *
     * Abhängig vom Ergebnis der Prüfungen wird entweder ein erfolgreiches
     * Bewegungs-Event oder ein spezielles Ablehnungs- bzw. Sonderereignis
     * (z.B. Sieg, Duell oder Barriereninteraktion) an das Frontend zurückgegeben.
     *
     * @param lobbyId die eindeutige ID der Lobby, in der der Zug ausgeführt wird
     * @param moveCmd das Bewegungskommando mit Meeple-ID und Bewegungsrichtung
     * @param player  der Spieler, der den Zug ausführt
     *
     * @return ein {@link FrontendEvent}, das den Ausgang des Zuges beschreibt
     *         (z.B. erfolgreiche Bewegung, Zugablehnung, Duell, Sieg oder
     *         Barriereninteraktion)
     *
     * @author Maximilian Ressel
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
                logger.info("player {} has won", player.getPlayerName());
                return new FrontendPlayerHasWonEvent(player.getPlayerName(), player.getColor(), meeple.getId(),
                        nextField.getId());
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
                    player.getId(),
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
                            player.getId(),
                            meeple.getId(),
                            nextField.getId(),
                            player.getRemainingMoves(),
                            tempBarrier.getId());
                }
                // ansonsten wird der zug beendet
                player.setRemainingMoves(0);
                meeple.clearLastField();
                logger.info("ran into barrier, cant go any further! (loses remaining moves)");
                return new FrontendRejectedByBarrierEvent(player.getId(), player.getRemainingMoves());
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
                        player.getId(),
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
                        return new FrontendDuelEvent(player.getId(), meeple.getId(), rivalMeeple.getId(),
                                nextField.getId(),
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
     * @param nextField    das Feld, auf das sich der Meeple bewegen möchte
     * @param currentField das Feld, auf dem sich der Meeple aktuell befindet
     * @param board        das aktuelle Spielfeld, das alle Barrieren kennt
     * @return true, wenn alle Nachbarfelder des Zielfelds entweder
     *         Felder mit Barrieren sind oder nicht betretbar sind,
     *         andernfalls false
     *
     * @author Maximilian Ressel
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
     * @author Maximilian Ressel
     */
    private boolean hasOnlyOwnMeepleNeighbours(Field nextField, Field currentField, List<Field> ownMeepleFields) {
        return nextField.getNeighbours().values().stream()
                .allMatch(neighbour -> neighbour.equals(currentField)
                        || neighbour.getType().isStart()
                        || ownMeepleFields.contains(neighbour));
    }

    /**
     * Beendet den aktuellen Zug eines Spielers, indem der übergebene Meeple
     * auf das angegebene Zielfeld bewegt wird, das lastField des Meeple entfernt
     * wird
     * und alle verbleibenden Bewegungen des Spielers verfallen.
     *
     * @param player    der Spieler, dessen Zug beendet wird
     * @param meeple    das Meeple, das bewegt wird
     * @param nextField das Zielfeld, auf das das Meeple gesetzt wird
     *
     * @author Maximilian Ressel
     */
    private void endTurnWithMove(Player player, Meeple meeple, Field nextField) {
        meeple.setCurrentField(nextField);
        meeple.clearLastField();
        player.setRemainingMoves(0);
    }

    /**
     * Verschiebt eine bestehende Barriere auf ein anderes Feld des Spielfelds.
     * 
     * Die Methode verarbeitet einen {@link MoveBarrierCommand} und prüft,
     * ob die gewünschte Zielposition gültig ist. Eine Barriere darf weder
     * auf ein Start- noch auf ein Zielfeld gesetzt werden und das Zielfeld
     * darf nicht bereits durch einen Meeple oder eine andere Barriere belegt sein.
     * 
     * Bei einem ungültigen Zug wird ein {@link FrontendMoveBarrierRejectedEvent}
     * zurückgegeben. Ist der Zug gültig, wird die Barriere auf das Zielfeld gesetzt
     * und ein {@link FrontendMoveBarrierEvent} erzeugt.
     *
     * @param lobbyId     die eindeutige ID der Lobby, in der die Barriere bewegt
     *                    wird
     * @param moveBarrCmd das Kommando mit Informationen zur Barriere und zum
     *                    Zielfeld
     * @param player      der Spieler, der die Aktion ausführt
     *
     * @return ein {@link FrontendEvent}, das entweder die erfolgreiche
     *         Barrierenbewegung oder die Ablehnung des Zuges repräsentiert
     *
     * @author Maximilian Ressel
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
     * @author Maximilian Ressel
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
     * Boards erreichbar ist.
     * 
     * Diese Methode dient ausschließlich zu Testzwecken, um Bewegungen oder
     * Barrierenverschiebungen simulieren zu können, solange das Frontend noch keine
     * gültigen Feld-IDs übermittelt.
     *
     * @param board das aktuelle Spielfeld
     * @return die ID eines zufällig gewählten Feldes, das vom Start aus erreichbar
     *         ist
     *
     * @author Maximilian Ressel
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
