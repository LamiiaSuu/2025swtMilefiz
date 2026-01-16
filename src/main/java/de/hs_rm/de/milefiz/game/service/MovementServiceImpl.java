package de.hs_rm.de.milefiz.game.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import de.hs_rm.de.milefiz.game.model.minigames.BalloonGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.SlotMachineGame;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.QuizGame;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendCheatedEvent;
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
 * +Bewegung von Meeples in kardinalen Richtungen
 * +Verwaltung verbleibender Spielzüge pro Spieler
 * +Erkennung ungültiger Züge (z.B. Richtungswechsel, blockierte Felder)
 * +Interaktion mit Barrieren inklusive Verlust verbleibender Züge
 * +Auslösen von Duellen zwischen gegnerischen Meeplen
 * +Erkennen von Sackgassen durch Barrieren oder eigene Meeple
 * +Erreichen des Zielfelds und Auslösen eines Spielsiegs
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
    private final DuelService duelService;
    private static final int LAST_MOVE = 1;

    public MovementServiceImpl(LobbyManager lobbyManager, DuelService duelService) {
        this.lobbyManager = lobbyManager;
        this.duelService = duelService;
    }

    /**
     * Bewegt einen Meeple eines Spielers um genau ein Feld in die angegebene
     * Richtung.
     * 
     * Die Methode verarbeitet einen {@link MovementCommand} und prüft anhand der
     * aktuellen Spielsituation, ob der Zug erlaubt ist. Dabei werden u.a. folgende
     * Regeln berücksichtigt:
     * 
     * +Der Spieler muss noch verbleibende Bewegungen besitzen.
     * +Das Zielfeld muss in der angegebenen Richtung existieren.
     * +Ein Richtungswechsel (Zurückgehen auf das zuletzt betretene Feld)
     * ist nicht erlaubt.
     * +Startfelder dürfen nach dem Verlassen nicht erneut betreten werden.
     * +Zielfelder dürfen nur beim exakt letzten Schritt betreten werden.
     * +Barrieren können Bewegungen blockieren oder – bei einem exakten Treffer
     * im letzten Schritt – eine Barrierenverschiebung auslösen.
     * +Das Betreten eines Feldes mit eigenen Meeples ist im letzten Schritt
     * nicht erlaubt.
     * +Trifft ein Meeple im letzten Schritt auf einen gegnerischen Meeple,
     * wird ein Duell ausgelöst.
     * +Bewegungen in Sackgassen (durch Barrieren oder eigene Meeples oder sich
     * duellierende Meeple)
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

        logger.info("Moving meeple {} from player '{}' in lobby {} in direction {})",
                moveCmd.meepleId(),
                player.getName(),
                lobbyId,
                moveCmd.direction());

        Lobby lobby = null;
        try {
            lobby = lobbyManager.getLobby(lobbyId);
        } catch (LobbyNotFoundException e) {
            logger.error("Lobby not found", e);
        }

        Board board = lobby.getBoard();
        Meeple meeple = player.getMeepleWithId(moveCmd.meepleId());
        Field currentField = meeple.getCurrentField();
        Field lastField = meeple.getLastField();
        Direction direction = moveCmd.direction();
        Set<Meeple> rivalMeeples = getRivalMeeples(lobby, player);
        Set<Field> rivalMeepleFields = getRivalMeepleFields(lobby, player);
        Set<Field> barrierFields = getBarrierFields(board);
        Set<Field> otherOwnMeepleFields = getOtherOwnMeepleFields(player);

        // Wenn keine weiteren Schritte verfügbar sind, kann man man sich nicht bewegen
        if (!player.canMove()) {
            logger.info("No more moves left");
            player.setMoved(false);
            return new FrontendMoveRejectedEvent(player.getId(), "MOVE_ERROR_NO_MOVES_LEFT");
        }

        // Spieler hat schon gemoved in diesem Zug und versucht einen anderen Meeple zu
        // bewegen
        if (changedMeeple(player, meeple)) {
            logger.info("Attempt to switch Meeple during move failed.");
            return new FrontendCheatedEvent(player.getId(), "Attempt to switch Meeple during move failed.");
        }

        // Meeple ist stuck, Zug wird zurückgesetzt, sodass der Spieler der Meeple
        // wechseln kann
        if (!existsLegalStopWithinRemainingMoves(currentField, lastField, player.getRemainingMoves(),
                otherOwnMeepleFields, barrierFields, rivalMeeples, rivalMeepleFields)) {

            endTurnWithMove(player, meeple, currentField);
            return meepleIsStuck(player, meeple, currentField, rivalMeepleFields, rivalMeeples, lobby);
        }

        // Ziel-Feld anhand der Bewegungsrichtung bestimmen
        Field nextField = getNextFieldByDirection(currentField, direction);

        // FELD EXISTIERT NICHT
        // Fehler, wenn in der angegeben Richtung kein Feld ist
        if (nextField == null) {
            logger.info("No Field in this Direction");
            return new FrontendMoveRejectedEvent(player.getId(), "MOVE_ERROR_NO_FIELD_IN_DIRECTION");
        }

        // RICHTUNGSWECHSEL
        // Fehler bei Versuch das Feld zu betreten auf dem man zuletzt war
        // (Richtungswechsel ist verboten)
        if (changedDirection(lastField, nextField)) {
            logger.info("Cant change direction!");
            return new FrontendMoveRejectedEvent(player.getId(), "MOVE_ERROR_CANT_CHANGE_DIRECTION");
        }

        // START
        // Nachdem das Startfeld verlassen wurde, kann man nicht zurückkehren (damit
        // kann man auch nicht die der anderen betreten)
        if (nextField.getType().isStart()) {
            logger.info("Cant go back to a starting field!");
            return new FrontendMoveRejectedEvent(player.getId(), "MOVE_ERROR_INTO_START");
        }

        // ZIEL
        // Man kann das Ziel nur betreten, wenn man exakt darauf endet
        if (nextField.getType().isEnd()) {
            return tryMovingOnEnd(meeple, nextField, player);
        }

        // BARRIERE
        // Wenn man in eine Barriere läuft, verliert man seine restlichen Schritte,
        // außer man landet genau darauf
        if (barrierFields.contains(nextField)) {
            Meeple barrier = getBarrierByField(board, nextField);
            return tryMovingOnBarrier(meeple, barrier, currentField, nextField, player, otherOwnMeepleFields,
                    rivalMeepleFields, rivalMeeples, lobby);
        }

        // FELD DURCH EIGENEN MEEPLE BLOCKIERT
        // Ueberpruefen, ob das Zielfeld beim letzten Move durch einen eigenen Meeple
        // blockiert ist
        if (turnWouldEndOnOwnMeeple(player, nextField, otherOwnMeepleFields)) {
            logger.info("Attempt to occupy a field with multiple meeple failed");
            return new FrontendMoveRejectedEvent(player.getId(), "MOVE_ERROR_OCCUPIED_BY_OWN_MEEPLE");
        }

        // SACKGASSE DURCH EIGENE MEEPLE ,BARRIEREN ODER DUELLIERENDE MEEPLE
        // Ueberpruefen, ob sich der Spieler,
        // abgesehen vom aktuellen Feld,
        // in eine Sackgasse aus eigenen Meeplen, Barrieren oder sich duellierenden
        // gegnerischen Meeplen bewegt
        if (player.getRemainingMoves() > LAST_MOVE) {
            Optional<FrontendEvent> frontendEvent = checkPath(meeple, currentField, nextField, player,
                    otherOwnMeepleFields,
                    barrierFields, rivalMeeples,
                    rivalMeepleFields, lobby);

            if (frontendEvent.isPresent()) {
                return frontendEvent.get();
            }
        }

        // DUELL
        // Wenn man mit dem letzten Schritt ein Feld mit einem gegnerischem meeple
        // betritt wird ein duell getriggert, wenn dieser sich nicht gerade in einem
        // duell befindet
        if (turnEndsOnRivalMeeple(player, rivalMeepleFields, nextField)) {

            Optional<FrontendEvent> frontendEvent = tryInitiatingDuel(meeple, nextField, player, rivalMeeples, lobby);

            if (frontendEvent.isPresent()) {
                return frontendEvent.get();
            }
        }

        // Spielfeld-Zustand aktualisieren
        // lastField wird jetzt im Meeple.setCurrentField aktualisiert
        meeple.setCurrentField(nextField);

        // der erste Zug nach dem Würfeln und mehr als 1 move verfügbar
        if (isTurnBegin(player)) {
            player.setActiveMeeple(meeple);
        }

        // Spieler nutzt einen Zug
        player.useMove();
        if (player.getRemainingMoves() == 0) {
            meeple.clearLastField();
            player.setActiveMeeple(null);
            player.setMoved(false);
        }

        // Erfolgreiche Bewegung an Clients senden
        FrontendMoveEvent move = new FrontendMoveEvent(
                player.getId(),
                meeple.getId(),
                nextField.getId(),
                player.getRemainingMoves(),
                player.hasMoved());

        logger.info("Meeple {} moved to {} ({} remaining moves)", meeple.getId(), nextField.getId(),
                player.getRemainingMoves());
        return move;
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
            logger.error("Lobby not found", e);
        }
        if (lobby == null) {
            return new FrontendMoveBarrierRejectedEvent("MOVE_BARRIER_NO_LOBBY");
        }
        Board board = lobby.getBoard();
        Meeple barrier = board.getBarrierById(moveBarrCmd.barrierId());
        Field currentField = barrier.getCurrentField();
        Field targetField = board.getFieldById(moveBarrCmd.targetFieldId());

        // Fehler, wenn es sich um ein Startfeld oder das Ende handelt
        if (targetField.getType().isEnd() || targetField.getType().isStart()) {
            logger.info("Cant place a barrier on Start or End");
            return new FrontendMoveBarrierRejectedEvent("MOVE_BARRIER_REJECTED_START_OR_END");
        }

        // Fehler wenn das Feld besetzt ist
        if (isOccupied(lobby, board, targetField)) {
            logger.info("Cant place a barrier on an occupied Field");
            return new FrontendMoveBarrierRejectedEvent("MOVE_BARRIER_OCCUPIED");
        }

        barrier.setCurrentField(targetField);

        return new FrontendMoveBarrierEvent(barrier.getId(), currentField.getId(), targetField.getId());
    }

    private FrontendEvent meepleIsStuck(Player player, Meeple meeple, Field currentField, Set<Field> rivalMeepleFields,
            Set<Meeple> rivalMeeples, Lobby lobby) {

        if (rivalMeepleFields.contains(currentField)) {

            Meeple rivalMeeple = getRivalMeepleByField(currentField, rivalMeeples);

            Player rivalPlayer = getPlayerByMeeple(lobby, rivalMeeple);

            return startDuel(meeple, rivalMeeple, player, rivalPlayer, currentField, lobby);
        }

        return new FrontendMoveWithLossEvent(
                player.getId(),
                meeple.getId(),
                currentField.getId(),
                player.getRemainingMoves(),
                player.hasMoved());
    }

    /**
     * Prüft, ob sich die Bewegungsrichtung geändert hat.
     *
     * @param lastField das vorherige Feld
     * @param nextField das nächste Feld
     * @return true, wenn ein Richtungswechsel erkannt wurde, sonst false
     * 
     * @author Maximilian Ressel
     */
    private boolean changedDirection(Field lastField, Field nextField) {
        return (lastField != null && nextField.equals(lastField));
    }

    /**
     * Prüft, ob ein Spieler nach bereits begonnenen Schritten versucht,
     * einen anderen Meeple zu bewegen.
     *
     * @param player der aktuelle Spieler
     * @param meeple der Meeple, der bewegt werden soll
     * @return true, wenn der Spieler bereits gezogen hat und ein anderer Meeple
     *         als der aktive Meeple bewegt werden soll, sonst false
     *
     * @author Maximilian Ressel
     */
    private boolean changedMeeple(Player player, Meeple meeple) {
        return (player.getActiveMeeple() != null
                && player.hasMoved()
                && !player.getActiveMeeple().equals(meeple));
    }

    /**
     * Gibt das nächste Feld in der angegebenen Richtung zurück.
     *
     * @param currentField das aktuelle Feld
     * @param direction    die Richtung, in die gegangen werden soll
     * @return das benachbarte Feld in der angegebenen Richtung
     * 
     * @author Maximilian Ressel
     */
    private Field getNextFieldByDirection(Field currentField, Direction direction) {
        return switch (direction) {
            case NORTH -> currentField.getNorth();
            case EAST -> currentField.getEast();
            case SOUTH -> currentField.getSouth();
            case WEST -> currentField.getWest();
        };
    }

    /**
     * Prüft, ob ein Spieler das Zielfeld betreten darf, und gibt das passende
     * FrontendEvent zurück.
     *
     * Betritt der Spieler das Zielfeld mit dem letzten erlaubten Zug,
     * wird der Sieg ausgelöst. Andernfalls wird der Zug abgelehnt.
     *
     * @param meeple der Meeple, der bewegt werden soll
     * @param end    das Zielfeld
     * @param player der aktuelle Spieler
     * @return ein FrontendEvent, das entweder den Sieg signalisiert oder den Zug
     *         ablehnt
     * 
     * @author Maximilian Ressel
     */
    private FrontendEvent tryMovingOnEnd(Meeple meeple, Field end, Player player) {
        // Wenn man darauf endet, hat man gewonnen
        if (player.getRemainingMoves() == LAST_MOVE) {
            player.useMove();
            logger.info("player {} has won", player.getPlayerName());
            return new FrontendPlayerHasWonEvent(player.getPlayerName(), player.getColor(), meeple.getId(),
                    end.getId());
        }
        logger.info("Cant enter End with remaining moves");
        return new FrontendMoveRejectedEvent(player.getId(), "MOVE_ERROR_TOO_MANY_MOVES_FOR_GOAL");
    }

    /**
     * Prüft, ob der Versuch, ein Feld mit einer Barriere zu betreten, erfolgreich
     * ist.
     * Bei Erfolg darf die Barriere verschoben werden.
     * Bei Misserfolg wird der Zug abgelehnt; steht auf dem aktuellen Feld ein
     * gegnerischer Meeple, kann stattdessen ein Duell gestartet werden.
     *
     * @param meeple               der Meeple, der bewegt werden soll
     * @param barrier              die Barriere, die das Feld blockiert
     * @param currentField         das aktuelle Feld des Meeples
     * @param targetField          das Feld mit der Barriere, das betreten werden
     *                             soll
     * @param player               der aktuelle Spieler
     * @param otherOwnMeepleFields Felder, auf denen eigene andere Meeples stehen
     * @param rivalMeepleFields    Felder, auf denen gegnerische Meeples stehen
     * @param rivalMeeples         alle gegnerischen Meeples
     * @param lobby                die aktuelle Lobby/Spielumgebung
     * @return ein FrontendEvent passend zum Ergebnis (Barriereschub, Ablehnung oder
     *         Duellstart)
     * 
     * @author Maximilian Ressel
     */
    private FrontendEvent tryMovingOnBarrier(Meeple meeple, Meeple barrier, Field currentField, Field targetField,
            Player player,
            Set<Field> otherOwnMeepleFields, Set<Field> rivalMeepleFields, Set<Meeple> rivalMeeples, Lobby lobby) {
        // wenn man genau drauf landet, darf man sie verschieben
        if (player.getRemainingMoves() == LAST_MOVE) {
            endTurnWithMove(player, meeple, targetField);
            logger.info("Direct hit on barrier {} with meeple {}", barrier.getId(), meeple.getId());
            return new FrontendTriggerBarrierMoveEvent(
                    player.getId(),
                    meeple.getId(),
                    targetField.getId(),
                    player.getRemainingMoves(),
                    barrier.getId());
        }

        return new FrontendRejectedByBarrierEvent(player.getId(), player.getRemainingMoves());
    }

    /**
     * Prüft, ob der Zug auf einem Feld mit einem eigenen Meeple enden würde.
     *
     * @param player               der aktuelle Spieler
     * @param targetField          das Zielfeld des Zuges
     * @param otherOwnMeepleFields Felder, auf denen eigene (andere) Meeples stehen
     * @return true, wenn es der letzte verbleibende Zug ist und das Zielfeld von
     *         einem eigenen Meeple belegt ist,
     *         sonst false
     * 
     * @author Maximilian Ressel
     */
    private boolean turnWouldEndOnOwnMeeple(Player player, Field targetField, Set<Field> otherOwnMeepleFields) {
        return (player.getRemainingMoves() == LAST_MOVE
                && otherOwnMeepleFields.contains(targetField));
    }

    /**
     * Prüft, ob es in der gewählten Richtung noch legale Stop-Felder innerhalb der
     * verbleibenden Züge gibt
     * und ob der Meeple in eine Sackgasse läuft.
     *
     * Wenn innerhalb der Reichweite noch ein legales Endfeld existiert, wird
     * {@code Optional.empty()} zurückgegeben.
     * Andernfalls wird der Zug ggf. abgelehnt (wenn das Zielfeld durch eigenen
     * Meeple oder duellierende Meeples blockiert ist)
     * oder der Zug endet auf dem Zielfeld (mit möglichem Duell bzw. Verlust-Event).
     *
     * @param meeple               der Meeple, der bewegt wird
     * @param currentField         das aktuelle Feld des Meeples
     * @param targetField          das Feld, das als nächstes angesteuert
     *                             wird
     * @param player               der aktuelle Spieler
     * @param otherOwnMeepleFields Felder, auf denen eigene (andere) Meeples stehen
     * @param barrierFields        Felder, die von Barrieren belegt sind
     * @param rivalMeeples         gegnerische Meeples
     * @param rivalMeepleFields    Felder, auf denen gegnerische Meeples stehen
     * @param lobby                die aktuelle Lobby/Spielumgebung
     * @return Optional.empty(), wenn noch ein legales Stop-Feld existiert; sonst
     *         ein FrontendEvent, das
     *         Ablehnung, Duellstart oder Zugende mit Verlust signalisiert
     * 
     * @author Maximilian Ressel
     */
    private Optional<FrontendEvent> checkPath(Meeple meeple, Field currentField, Field targetField, Player player,
            Set<Field> otherOwnMeepleFields, Set<Field> barrierFields, Set<Meeple> rivalMeeples,
            Set<Field> rivalMeepleFields, Lobby lobby) {

        if (existsLegalStopWithinRemainingMoves(targetField, currentField, player.getRemainingMoves() - 1,
                otherOwnMeepleFields, barrierFields, rivalMeeples, rivalMeepleFields)) {
            return Optional.empty();
        }

        if (otherOwnMeepleFields.contains(targetField)
                || isOccupiedByDuelingMeeples(targetField, rivalMeeples, rivalMeepleFields)) {
            logger.info("No valid Fields to End this Meeples run in this Direction");
            return Optional.of(new FrontendMoveRejectedEvent(player.getId(), "MOVE_ERROR_NO_VALID_FIELDS"));
        }

        endTurnWithMove(player, meeple, targetField);
        logger.info("No further possible Fields within reach - turn ends");
        if (rivalMeepleFields.contains(targetField)) {

            Meeple rivalMeeple = getRivalMeepleByField(targetField, rivalMeeples);

            Player rivalPlayer = getPlayerByMeeple(lobby, rivalMeeple);

            return Optional.of(startDuel(meeple, rivalMeeple, player, rivalPlayer, targetField, lobby));
        }
        return Optional.of(new FrontendMoveWithLossEvent(
                player.getId(),
                meeple.getId(),
                targetField.getId(),
                player.getRemainingMoves(),
                player.hasMoved()));
    }

    /**
     * Prüft, ob der Zug auf einem Feld mit einem gegnerischen Meeple enden würde.
     *
     * @param player            der aktuelle Spieler
     * @param rivalMeepleFields Felder, auf denen gegnerische Meeples stehen
     * @param targetField       das Zielfeld des Zuges
     * @return true, wenn es der letzte verbleibende Zug ist und das Zielfeld von
     *         einem gegnerischen Meeple belegt ist, sonst false
     * 
     * @author Maximilian Ressel
     */
    private boolean turnEndsOnRivalMeeple(Player player, Set<Field> rivalMeepleFields, Field targetField) {
        return (player.getRemainingMoves() == LAST_MOVE && rivalMeepleFields.contains(targetField));
    }

    /**
     * Prüft, ob durch den Zug ein Duell ausgelöst wird, und leitet dieses
     * gegebenenfalls ein.
     *
     * @param ownMeeple    der eigene Meeple, der bewegt wird
     * @param targetField  das Zielfeld des Zuges
     * @param player       der aktuelle Spieler
     * @param rivalMeeples alle gegnerischen Meeples
     * @param lobby        die aktuelle Lobby/Spielumgebung
     * @return Optional.empty(), wenn kein Duell ausgelöst wird; andernfalls ein
     *         FrontendEvent zum Starten des Duells oder zur Ablehnung des Zuges
     * 
     * @author Maximilian Ressel
     */
    private Optional<FrontendEvent> tryInitiatingDuel(Meeple ownMeeple, Field targetField, Player player,
            Set<Meeple> rivalMeeples, Lobby lobby) {
        Meeple rivalMeeple = getRivalMeepleByField(targetField, rivalMeeples);
        Player rivalPlayer = getPlayerByMeeple(lobby, rivalMeeple);

        if (rivalMeeple == null) {
            return Optional.empty();
        }

        if (duelService.isMeepleInDuel(rivalMeeple.getId())) {
            logger.info("Move blocked — rival meeple {} is already in a duel", rivalMeeple.getId());

            return Optional.of(new FrontendMoveRejectedEvent(
                    player.getId(),
                    "MEEPLE_IN_DUEL"));
        }

        ownMeeple.setCurrentField(targetField);
        ownMeeple.clearLastField();
        player.setActiveMeeple(ownMeeple);
        player.useMove();

        logger.info("Initiating duel between meeple {} and meeple {}",
                ownMeeple.getId(), rivalMeeple.getId());

        return Optional.of(startDuel(ownMeeple, rivalMeeple, player, rivalPlayer, targetField, lobby));
    }

    /**
     * Startet ein Duell zwischen dem Meeple des aktuellen Spielers und dem Meeple
     * eines gegnerischen Spielers auf einem bestimmten Feld.
     *
     * Andernfalls wird ein neues Duell erstellt, ein zufälliges Minispiel
     * zugewiesen und die beteiligten Spieler werden abhängig vom Minispiel
     * initialisiert.
     *
     * @param ownMeeple   das Meeple des aktuellen Spielers, das das Duell startet
     * @param rivalMeeple das Meeple des gegnerischen Spielers
     * @param player      der aktuelle Spieler
     * @param rivalPlayer der gegnerische Spieler, kann {@code null} sein
     * @param field       das Spielfeld, auf dem das Duell stattfindet
     * @param lobby       die Lobby, die von bestimmten Minispielen benötigt wird
     *
     * @return ein FrontendEvent, das entweder einen verlorenen Zug oder ein
     *         initialisiertes Duell mit zugewiesenem Minispiel repräsentiert
     * 
     * @author Robert Bothfeld
     * @author Maximilian Ressel
     */
    private FrontendEvent startDuel(Meeple ownMeeple, Meeple rivalMeeple, Player player, Player rivalPlayer,
            Field field, Lobby lobby) {

        if (rivalPlayer == null) {
            return new FrontendMoveWithLossEvent(
                    player.getId(),
                    ownMeeple.getId(),
                    field.getId(),
                    player.getRemainingMoves(),
                    player.hasMoved());
        }
        var duel = duelService.createDuel(
                player.getId(),
                rivalPlayer.getId(),
                ownMeeple.getId(),
                rivalMeeple.getId());

        var miniGame = duelService.assignRandomGameToDuel(duel.getId());

        if (miniGame instanceof DiceGame dice) {
            dice.initPlayers(player.getId(), rivalPlayer.getId());
        } else if (miniGame instanceof SlotMachineGame game) {
            game.initPlayers(player, rivalPlayer);
        }
        if (miniGame instanceof BalloonGame game) {
            game.initPlayers(player.getId(), rivalPlayer.getId());
        }
        if (miniGame instanceof QuizGame quiz) {
            quiz.initPlayers(player.getId(), rivalPlayer.getId());
        }

        return new FrontendDuelEvent(
                duel.getId(),
                player.getId(),
                rivalPlayer.getId(),
                ownMeeple.getId(),
                rivalMeeple.getId(),
                field.getId(),
                player.getRemainingMoves(),
                miniGame);
    }

    /**
     * Prüft, ob es sich um den Beginn eines neuen Zuges des Spielers handelt.
     *
     * @param player der aktuelle Spieler
     * @return true, wenn der Spieler noch mehr als einen verbleibenden Zug hat
     *         und in diesem Zug noch keine Bewegung durchgeführt wurde,
     *         sonst false
     * 
     * @author Maximilian Ressel
     */
    private boolean isTurnBegin(Player player) {
        return (player.getRemainingMoves() > 1 && !player.hasMoved());
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
        player.setActiveMeeple(null);
        player.setMoved(false);
    }

    /**
     * Diese Methode dient als öffentlicher Einstiegspunkt für die rekursive
     * Tiefensuche und initialisiert die benötigte Memoisierung.
     * Die eigentliche Logik der Pfadsuche ist in
     * {@link #existsLegalStopWithinRemainingMovesDfs(Field, Field, int, Set, Set, Map)}
     * implementiert.
     *
     * @param startingField     das Feld, von dem aus die Suche gestartet wird
     * @param lastField         das zuletzt betretene Feld,
     *                          oder {@code null}, falls keines existiert
     * @param remainingMoves    die Anzahl der noch verfügbaren Schritte
     * @param ownMeepleFields   alle Felder, die aktuell von eigenen Meeples besetzt
     *                          sind
     * 
     * @param barrierFields     alle Felder, die aktuell von Barrieren besetzt sind
     * 
     * @param rivalMeeples      alle gegnerischen Meeples, die im Spiel vorhanden
     *                          sind
     * @param rivalMeepleFields Menge der Felder, auf denen gegnerische Meeples
     *                          aktuell stehen;
     *                          dient als Optimierung zur schnellen Vorprüfung
     * 
     * @return {@code true}, wenn innerhalb der verbleibenden Schritte mindestens
     *         ein legales Stopfeld erreichbar ist, andernfalls {@code false}
     *
     * @author Maximilian Ressel
     */
    private boolean existsLegalStopWithinRemainingMoves(Field startingField, Field lastField, int remainingMoves,
            Set<Field> ownMeepleFields, Set<Field> barrierFields, Set<Meeple> rivalMeeples,
            Set<Field> rivalMeepleFields) {
        Map<String, Boolean> memo = new HashMap<>();
        return existsLegalStopWithinRemainingMovesDfs(
                startingField, lastField, remainingMoves, ownMeepleFields, barrierFields, rivalMeeples,
                rivalMeepleFields, memo);
    }

    /**
     * Prüft rekursiv, ob von einem gegebenen Startfeld aus innerhalb der
     * verbleibenden Anzahl an Schritten mindestens ein legales Stopfeld
     * erreichbar ist.
     *
     * Ein legales Stopfeld ist ein Feld, auf dem der Zug beendet werden darf,
     * d.h. ein Feld, das:
     * über ausschließlich legale Zwischenschritte erreichbar ist
     * (gemäß {@link #isLegalTarget(Field, Field, int, Set, Set)})
     * und nicht von einem eigenen Meeple oder sich duellierenden
     * gegnerischen Meeplen besetzt ist.
     *
     * Die Methode durchsucht den Bewegungsraum per Tiefensuche (DFS) und
     * verwendet Memoisierung, um bereits geprüfte Zustände zu cachen.
     * Ein Zustand ist eindeutig definiert durch:
     * 
     * das aktuelle Feld,
     * das zuletzt betretene Feld,
     * die verbleibende Anzahl an Schritten
     *
     * Die Suche endet erfolgreich, sobald ein legales Stopfeld gefunden wird.
     * Wird innerhalb der verfügbaren Schritte kein solches Feld erreicht,
     * liefert die Methode {@code false}.
     *
     * @param startingField     das Feld, von dem aus die Suche gestartet wird
     * @param lastField         das zuletzt betretene Feld (zur Erkennung von
     *                          Richtungswechseln),
     *                          oder {@code null}, falls keines existiert
     * @param remainingMoves    die Anzahl der noch verfügbaren Schritte
     * @param ownMeepleFields   alle Felder, die aktuell von eigenen Meeples besetzt
     *                          sind
     * 
     * @param barrierFields     alle Felder, die aktuell von Barrieren besetzt sind
     * 
     * @param rivalMeeples      alle gegnerischen Meeples, die im Spiel vorhanden
     *                          sind
     * @param rivalMeepleFields Menge der Felder, auf denen gegnerische Meeples
     *                          aktuell stehen;
     *                          dient als Optimierung zur schnellen Vorprüfung
     * 
     * @param memo              Cache zur Memoisierung bereits geprüfter Zustände
     *                          (Key: Feld + letztes Feld + verbleibende Schritte)
     *
     * @return {@code true}, wenn innerhalb der verbleibenden Schritte mindestens
     *         ein legales Stopfeld erreichbar ist, andernfalls {@code false}
     *
     * @author Maximilian Ressel
     */
    private boolean existsLegalStopWithinRemainingMovesDfs(Field startingField, Field lastField, int remainingMoves,
            Set<Field> ownMeepleFields, Set<Field> barrierFields, Set<Meeple> rivalMeeples,
            Set<Field> rivalMeepleFields, Map<String, Boolean> memo) {

        if (remainingMoves == 0) {
            return false;
        }

        if (remainingMoves < 0) {
            return false;
        }

        String key = startingField.getId().toString() + "-"
                + (lastField == null ? "null" : lastField.getId().toString()) + "-" + remainingMoves;

        Boolean cached = memo.get(key);

        if (cached != null) {
            return cached;
        }

        for (Field neighbourField : startingField.getNeighbours().values()) {
            if (neighbourField == null) {
                continue;
            }

            if (!isLegalTarget(neighbourField, lastField, remainingMoves, ownMeepleFields, barrierFields, rivalMeeples,
                    rivalMeepleFields)) {
                continue;
            }

            if (!ownMeepleFields.contains(neighbourField)
                    && !isOccupiedByDuelingMeeples(neighbourField, rivalMeeples, rivalMeepleFields)) {
                return true;
            }

            if (existsLegalStopWithinRemainingMovesDfs(neighbourField, startingField, remainingMoves - 1,
                    ownMeepleFields, barrierFields, rivalMeeples, rivalMeepleFields, memo)) {
                memo.put(key, true);
                return true;
            }
        }

        memo.put(key, false);
        return false;
    }

    /**
     * Prüft, ob ein bestimmtes Feld als nächstes Zielfeld betreten werden darf.
     *
     * @param nextField         das Feld, das als nächstes betreten werden soll
     * @param lastField         das zuvor betretene Feld (zur Erkennung von
     *                          Richtungswechseln),
     *                          oder {@code null}, falls keiner existiert
     * @param remainingMoves    die Anzahl der verbleibenden Moves
     * 
     * @param ownMeepleFields   alle Felder, die aktuell von eigenen Meeplen besetzt
     *                          sind
     * 
     * @param barrierFields     alle Felder, die aktuell von Barrieren besetzt sind
     * 
     * @param rivalMeeples      alle gegnerischen Meeples, die im Spiel vorhanden
     *                          sind
     * @param rivalMeepleFields Menge der Felder, auf denen gegnerische Meeples
     *                          aktuell stehen;
     *                          dient als Optimierung zur schnellen Vorprüfung
     * 
     * @return {@code true}, wenn das Zielfeld unter den gegebenen Bedingungen
     *         betreten werden darf, andernfalls {@code false}
     *
     * @author Maximilian Ressel
     */
    private boolean isLegalTarget(Field nextField, Field lastField, int remainingMoves,
            Set<Field> ownMeepleFields, Set<Field> barrierFields, Set<Meeple> rivalMeeples,
            Set<Field> rivalMeepleFields) {

        // FELD EXISTIERT NICHT
        if (nextField == null) {
            logger.info("No Field in this Direction");
            return false;
        }

        // RICHTUNGSWECHSEL
        if (lastField != null && nextField.equals(lastField)) {
            logger.info("Cant change direction!");
            return false;
        }

        // START
        if (nextField.getType().isStart()) {
            logger.info("Cant go back to a starting field!");
            return false;
        }

        // ZIEL
        if (nextField.getType().isEnd() && remainingMoves != LAST_MOVE) {
            logger.info("Cant enter End with remaining moves!");
            return false;
        }

        // BARRIERE
        if (barrierFields.contains(nextField) && remainingMoves != LAST_MOVE) {
            logger.info("Field blocked by barrier (not your last move)!");
            return false;
        }

        // EIGENE MEEPLE
        if (ownMeepleFields.contains(nextField) && remainingMoves == LAST_MOVE) {
            logger.info("Field blocked by own Meeple!");
            return false;
        }

        // DUELL ZWISCHEN ZWEI ANDEREN MEEPLE
        if (isOccupiedByDuelingMeeples(nextField, rivalMeeples, rivalMeepleFields) && remainingMoves == LAST_MOVE) {
            logger.info("Field blocked by dueling Meeple!");
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob das angegebene Feld durch einen gegnerischen Meeple belegt ist,
     * der sich aktuell in einem Duell befindet.
     *
     * @param targetField       das Feld, das auf eine Belegung durch duellierende
     *                          gegnerische Meeples geprüft werden soll
     * @param rivalMeeples      alle gegnerischen Meeples, die im Spiel vorhanden
     *                          sind
     * @param rivalMeepleFields Menge der Felder, auf denen gegnerische Meeples
     *                          aktuell stehen;
     *                          dient als Optimierung zur schnellen Vorprüfung
     * @return true, wenn das Feld durch einen gegnerischen Meeple belegt ist,
     *         der sich in einem Duell befindet, sonst false
     *
     * @author Maximilian Ressel
     */
    private boolean isOccupiedByDuelingMeeples(Field targetField, Set<Meeple> rivalMeeples,
            Set<Field> rivalMeepleFields) {
        if (!rivalMeepleFields.contains(targetField)) {
            return false;
        }
        for (Meeple meeple : rivalMeeples) {
            if (meeple.getCurrentField().equals(targetField) && duelService.isMeepleInDuel(meeple.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft, ob ein bestimmtes Feld aktuell von einem Meeple belegt ist.
     *
     * @param lobby die aktuelle Lobby mit allen Spielern und Meeples
     * @param field das zu prüfende Feld
     * @return true, wenn sich ein Meeple auf dem Feld befindet,
     *         sonst false
     */
    private boolean isOccupiedByMeeple(Lobby lobby, Field field) {
        return lobby.getPlayers().stream()
                .flatMap(player -> Arrays.stream(player.getMeeples()))
                .map(Meeple::getCurrentField)
                .filter(Objects::nonNull)
                .anyMatch(f -> f.equals(field));
    }

    /**
     * Prüft, ob ein bestimmtes Feld aktuell durch einen Meeple oder eine Barriere
     * belegt ist.
     *
     * @param lobby die aktuelle Lobby mit allen Spielern und Meeples
     * @param board das Spielbrett mit allen Barrieren
     * @param field das zu prüfende Feld
     * @return true, wenn das Feld durch einen Meeple oder eine Barriere belegt ist,
     *         sonst false
     */
    private boolean isOccupied(Lobby lobby, Board board, Field field) {

        if (isOccupiedByMeeple(lobby, field))
            return true;

        boolean occupiedByBarrier = board.getBarriers().stream()
                .map(Meeple::getCurrentField)
                .filter(Objects::nonNull)
                .anyMatch(f -> f.equals(field));

        return occupiedByBarrier;
    }

    /**
     * Ermittelt alle Felder, die aktuell von eigenen Meeples des Spielers belegt
     * sind,
     * ausgenommen der aktive Meeple.
     *
     * @param player der aktuelle Spieler
     * @return eine Menge aller Felder, auf denen eigene (nicht aktive) Meeples
     *         stehen
     */
    private Set<Field> getOtherOwnMeepleFields(Player player) {
        Meeple activeMeeple = player.getActiveMeeple();
        return Arrays.stream(player.getMeeples())
                .filter(meeple -> activeMeeple == null || !meeple.equals(activeMeeple))
                .map(Meeple::getCurrentField)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Ermittelt alle gegnerischen Meeples
     *
     * @param lobby  die aktuelle Lobby mit allen Spielern
     * @param player der Spieler selbst, dessen eigene Meeple ignoriert werden
     *               sollen
     * @return eine Menge aller gegnerischen Meeples
     */
    private Set<Meeple> getRivalMeeples(Lobby lobby, Player player) {
        Set<Meeple> rivalMeeples = new HashSet<>();
        for (Player rivalPlayer : lobby.getPlayers()) {
            if (player.equals(rivalPlayer)) {
                continue;
            }
            for (Meeple rivalMeeple : rivalPlayer.getMeeples()) {
                rivalMeeples.add(rivalMeeple);
            }
        }
        return rivalMeeples;
    }

    /**
     * Ermittelt alle Felder, die aktuell von gegnerischen Meeples belegt sind.
     *
     * @param lobby  die aktuelle Lobby mit allen Spielern
     * @param player der Spieler selbst, dessen eigene Meeple ignoriert werden
     *               sollen
     * @return eine Menge aller Felder, auf denen gegnerische Meeples stehen
     */
    private Set<Field> getRivalMeepleFields(Lobby lobby, Player player) {
        return getRivalMeeples(lobby, player).stream()
                .map(Meeple::getCurrentField).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Ermittelt alle Felder, die aktuell von Barrieren belegt sind.
     *
     * @param board das Spielbrett mit allen Barrieren
     * @return eine Menge aller Felder, auf denen sich Barrieren befinden
     */
    private Set<Field> getBarrierFields(Board board) {
        return board.getBarriers().stream()
                .map(Meeple::getCurrentField)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Ermittelt die Barriere, die sich auf dem angegebenen Feld befindet.
     *
     * @param board das Spielbrett mit allen Barrieren
     * @param field das Feld, auf dem die Barriere gesucht wird
     * @return die gefundene Barriere oder null, wenn sich keine Barriere auf dem
     *         Feld befindet
     */
    private Meeple getBarrierByField(Board board, Field field) {
        return board.getBarriers().stream()
                .filter(barrier -> field.equals(barrier.getCurrentField()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Liefert den gegnerischen Meeple, der sich auf dem angegebenen Feld befindet.
     *
     * @param field        das Feld, auf dem der gegnerische Meeple gesucht wird
     * @param rivalMeeples die Menge aller gegnerischen Meeples
     * @return der gefundene gegnerische Meeple oder null, wenn sich keiner auf dem
     *         Feld befindet
     */
    private Meeple getRivalMeepleByField(Field field, Set<Meeple> rivalMeeples) {
        return rivalMeeples.stream()
                .filter(m -> field.equals(m.getCurrentField()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Ermittelt den Spieler, zu dem der angegebene Meeple gehört.
     *
     * @param lobby  die aktuelle Lobby mit allen Spielern
     * @param meeple der Meeple, dessen Besitzer ermittelt werden soll
     * @return der zugehörige Spieler oder null, wenn kein Spieler gefunden wird
     */
    private Player getPlayerByMeeple(Lobby lobby, Meeple meeple) {
        if (meeple == null)
            return null;
        return lobby.getPlayers().stream()
                .filter(p -> Arrays.stream(p.getMeeples())
                        .anyMatch(m -> m.getId().equals(meeple.getId())))
                .findFirst()
                .orElse(null);
    }

}
