package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.commands.RollDiceCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent;

@Controller
public class FrontendReceiverController {

    private final Logger logger = LoggerFactory.getLogger(FrontendReceiverController.class);
    private LobbyManager lobbyManager;
    private GameService gameService;

    public FrontendReceiverController(LobbyManager lobbyManager, GameService gameService) {
        this.lobbyManager = lobbyManager;
        this.gameService = gameService;
    }

    @MessageMapping("/milefiz/lobby/{lobbyId}")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public String handleMessage(@DestinationVariable UUID lobbyId, String message) {
        System.out.println("Received " + lobbyId.toString() + ": " + message);
        return "Server received: " + message; // Body von Weiterleitung an alle Clients
    }

    @MessageMapping("/milefiz/lobby/{lobbyId}/move")
    @SendTo("/topic/milefiz/lobby/{lobbyId}/move")
    public FrontendEvent handleMove(@DestinationVariable UUID lobbyId, @Header("simpSessionId") String sessionId,
            MovementCommand moveCmd) {

        UUID testLobby = UUID.fromString("271c95db-3737-496f-9081-ae920e8ebbf7");
        
        Lobby lobby = null;
        try {
            lobby = lobbyManager.getLobby(testLobby);
        } catch (LobbyNotFoundException e) {
            e.printStackTrace();
        }
        
        Player player = null;
        try {
            player = lobby.getPlayerBySessionId(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // nur zum testen
        lobby.setField(gameService.getTestBoard());

        if (player == null) {
            System.out.println("No player found for session " + sessionId + ", creating dummy player...");
            player = lobby.getPlayers().stream().findAny().orElse(null);
            player.setSessionId(sessionId);
            System.out.println("createt dummy player" + player.getId() + "with sessionId" + player.getSessionId());
            player.getMeeples()[0].setId(moveCmd.meepleId());
            player.getMeeples()[0].setCurrentField(lobby.getField());
        }
        //

        
        Meeple meeple = player.getMeepleWithId(moveCmd.meepleId());
        Field currentField = meeple.getCurrentField();
        Field lastField = meeple.getLastField();
        Direction direction = moveCmd.direction();

        // Ziel-Feld anhand der Bewegungsrichtung bestimmen
        Field nextField = switch (direction) {
            case NORTH -> currentField.getNorth();
            case EAST -> currentField.getEast();
            case SOUTH -> currentField.getSouth();
            case WEST -> currentField.getWest();
        };

        if (nextField == null) {
            System.out.println("invalid direction!");
            return new FrontendMoveRejectedEvent("Field doesnt exist");
        }

        if (nextField.isBarrier()) {
            // TODO player loses all unspent steps
            System.out.println("reached blockade, cant go any further!");
            return new FrontendMoveRejectedEvent("ran into barrier");
        }

        if (nextField.getOccupant() != null) {
            // TODO duel starts
            System.out.println("oh oh, looks like its time to duel!");
            return new FrontendMoveRejectedEvent("time to duel first");
        }

        // Rückwärtsbewegung nicht erlaubt
        if (nextField.equals(lastField)) {
            System.out.println("cannot change direction!");
            return new FrontendMoveRejectedEvent("cannot change direction!");
        }

        // Spielfeld-Zustand aktualisieren
        currentField.setOccupant(null);
        nextField.setOccupant(meeple);
        meeple.setLastField(currentField);
        meeple.setCurrentField(nextField);

        // Erfolgreiche Bewegung an Clients senden
        FrontendMoveEvent move = new FrontendMoveEvent(
                meeple.getId(),
                nextField.getId());

        return move;
    }

    @MessageMapping("/milefiz/lobby/{lobbyId}/rollDice")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendRollDiceEvent handleRollDice(@DestinationVariable UUID lobbyId, RollDiceCommand command) {
        logger.info("Player {} wants to roll dice in lobby {}", command.playerId(), lobbyId);
        int number = gameService.rollDice();
        return new FrontendRollDiceEvent(lobbyId, number);
    }
}
