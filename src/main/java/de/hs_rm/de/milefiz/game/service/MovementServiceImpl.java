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
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;

@Service
public class MovementServiceImpl implements MovementService {

    private final Logger logger = LoggerFactory.getLogger(MovementServiceImpl.class);
    private LobbyManager lobbyManager;

    public MovementServiceImpl(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public FrontendEvent moveMeeple(UUID lobbyId, MovementCommand moveCmd, Principal principal,
            SimpMessageHeaderAccessor sha) {
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

        // nur zum testen
        // lobby.setBoard(gameService.getTestBoard());
        player.getMeeples()[0].setId(moveCmd.meepleId());
        if (player.getMeeples()[0].getCurrentField() == null) {
            player.getMeeples()[0].setCurrentField(lobby.getBoard().getStartGreen());
        }

        Board board = lobby.getBoard();
        Meeple meeple = player.getMeepleWithId(moveCmd.meepleId());
        Field currentField = meeple.getCurrentField();
        Field lastField = meeple.getLastField();
        Direction direction = moveCmd.direction();

        // Ziel-Feld anhand der Bewegungsrichtung bestimmen
        Field nextField = switch (direction) {
            case NORTH ->
                currentField.getNorth();
            case EAST ->
                currentField.getEast();
            case SOUTH ->
                currentField.getSouth();
            case WEST ->
                currentField.getWest();
        };

        if (nextField == null) {
            System.out.println("invalid direction!");
            return new FrontendMoveRejectedEvent("Field doesnt exist");
        }

        if (!player.canMove()) {
            logger.info("No more moves left");
            return new FrontendMoveRejectedEvent("no moves left");
        }

        for (Meeple tempBarrier : board.getBarriers()) {
            if (tempBarrier.getCurrentField().equals(nextField)) {
                // TODO player loses all unspent steps
                System.out.println("reached blockade, cant go any further!");
                return new FrontendMoveRejectedEvent("ran into barrier");
            }
        }

        for (Player tempPlayer : lobby.getPlayers()) {
            if (player.equals(tempPlayer)) {
                continue;
            }

            for (Meeple tempMeeple : tempPlayer.getMeeples()) {
                // Keine Barriere und Meeple vom anderen Spieler steht drauf
                if (!tempMeeple.isBarrier() && tempMeeple.getCurrentField().equals(nextField)) {
                    // TODO duel starts !!! Erst wenn letzter Move des Wuerfel-Zuges
                    System.out.println("oh oh, looks like its time to duel!");
                    return new FrontendMoveRejectedEvent("time to duel first");
                }
            }
        }

        // Rückwärtsbewegung nicht erlaubt
        if (nextField.equals(lastField)) {
            System.out.println("cannot change direction!");
            return new FrontendMoveRejectedEvent("cannot change direction!");
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
