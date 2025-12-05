package de.hs_rm.de.milefiz.game.service;

import java.security.Principal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.FieldType;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendDuelEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveBarrierEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;

@Service
public class MovementServiceImpl implements MovementService {

    private final Logger logger = LoggerFactory.getLogger(MovementServiceImpl.class);
    private LobbyManager lobbyManager;
    private final int LAST_MOVE = 1;
    private final int SECOND_TO_LAST_MOVE = 2;

    public MovementServiceImpl(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public FrontendEvent moveMeeple(UUID lobbyId, MovementCommand moveCmd, Principal principal,
            SimpMessageHeaderAccessor sha) {

        logger.info("Moving meeple {} from player '{}' in lobby {} in direction {} (sessionId={})",
                moveCmd.meepleId(),
                principal != null ? principal.getName() : "anonymous",
                lobbyId,
                moveCmd.direction(),
                sha.getSessionId());

        Lobby lobby = null;
        try {
            lobby = lobbyManager.getLobby(lobbyId);
        } catch (LobbyNotFoundException e) {
            e.printStackTrace();
        }

        String principalName = null;
        if (principal != null) {
            principalName = principal.getName();
        }

        Player player = null;
        try {
            player = lobby.getPlayerByToken(principalName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // *********************************************************************************************************************
        // */
        // nur zum testen
        player.getMeeples()[0].setId(moveCmd.meepleId());
        if (player.getMeeples()[0].getCurrentField() == null) {
            player.getMeeples()[0].setCurrentField(lobby.getBoard().getStartGreen());
        }
        // *********************************************************************************************************************
        // */

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

        // Fehler, wenn in der angegeben Richtung kein Feld ist
        if (nextField == null) {
            logger.info("No Field in this Direction");
            return new FrontendMoveRejectedEvent("No Field in this Direction");
        }

        // Fehler bei Versuch das Feld zu betreten auf dem man zuletzt war
        // (Richtungswechsel ist verboten)
        if (lastField != null && nextField.equals(lastField)) {
            logger.info("Cant change direction!");
            return new FrontendMoveRejectedEvent("Cant change direction!");
        }
        
        // Nachdem das Startfeld verlassen wurde, kann man nicht zurückkehren (damit
        // kann man auch nicht die der anderen betreten)
        if (nextField.getType().isStart()) {
            logger.info("Cant go back to a starting field!");
            return new FrontendMoveRejectedEvent("Cant go back to a starting field!");
        }
        
        // Überprüfung, ob das Zielfeld abgesehen vom aktuellen Feld nur Barrieren als
        // Nachbarn hat
        boolean onlyBarrierNeighbors = nextField.getNeighbours().values().stream().allMatch(
                neighbour -> neighbour.equals(currentField)
                        || board.getBarriers().stream().map(Meeple::getCurrentField).anyMatch(
                                barrierField -> barrierField != null && barrierField.equals(neighbour)));

        // Wenn man ein Feld betritt, das als einzig angrenzende Felder Barrieren hat,
        // wird der Zug automatisch beendet ohne dass man sich noch in Richtung der
        // Barriere bewegen muss.
        if ((onlyBarrierNeighbors) && (player.getRemainingMoves() != SECOND_TO_LAST_MOVE)) {
            meeple.setCurrentField(nextField);
            meeple.clearLastField();
            player.setRemainingMoves(0);
            logger.info("All possible moves would lead into Barriers, player loses remaining Moves, turn is over");
            return new FrontendMoveEvent(
                    meeple.getId(),
                    nextField.getId(),
                    player.getRemainingMoves());
        }

        // Wenn man in eine Barriere läuft, verliert man seine restlichen Schritte,
        // außer man landet genau darauf
        for (Meeple tempBarrier : board.getBarriers()) {
            if (tempBarrier.getCurrentField().equals(nextField)) {
                // wenn man genau drauf landet, darf man sie verschieben
                if (player.getRemainingMoves() == LAST_MOVE) {
                    meeple.setCurrentField(nextField);
                    meeple.clearLastField();
                    player.useMove();
                    logger.info("Direct hit on barrier {} with meeple {}", tempBarrier.getId(), meeple.getId());
                    return new FrontendMoveBarrierEvent(tempBarrier.getId());
                }
                // ansonsten wird der zug beendet
                player.setRemainingMoves(0);
                meeple.clearLastField();
                logger.info("ran into barrier, cant go any further! (loses remaining moves)");
                return new FrontendMoveRejectedEvent("ran into barrier, cant go any further! (loses remaining moves)");
            }
        }

        // Duell einleiten, wenn man auf einem Feld landet, auf dem ein Meeple eines
        // anderen Spielers steht
        for (Player rivalPlayer : lobby.getPlayers()) {
            if (player.equals(rivalPlayer)) {
                continue;
            }
            for (Meeple rivalMeeple : rivalPlayer.getMeeples()) {
                if (rivalMeeple.getCurrentField().equals(nextField)) {
                    if (player.getRemainingMoves() == LAST_MOVE) {
                        meeple.setCurrentField(nextField);
                        meeple.clearLastField();
                        player.useMove();
                        logger.info("Initiating duel between meeple {} and meeple {}", meeple.getId(),
                                rivalMeeple.getId());
                        return new FrontendDuelEvent(meeple.getId(), rivalMeeple.getId());
                    }
                }
            }
        }

        // Spielfeld-Zustand aktualisieren
        // lastField wird jetzt im Meeple.setCurrentField aktualisiert
        meeple.setCurrentField(nextField);

        // Spieler nutzt einen Zug
        player.useMove();

        // Erfolgreiche Bewegung an Clients senden
        FrontendMoveEvent move = new FrontendMoveEvent(
                meeple.getId(),
                nextField.getId(),
                player.getRemainingMoves());

        return move;
    }
}
