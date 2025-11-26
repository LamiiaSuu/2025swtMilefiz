package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.service.GameService;
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
    public String handleMessage(@DestinationVariable("lobbyId") UUID lobbyId, String message) {
        System.out.println("Received " + lobbyId.toString() + ": " + message);
        return "Server received: " + message; // Body von Weiterleitung an alle Clients
    }

    @MessageMapping("/milefiz/lobby/{lobbyId}/move")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleMove(@DestinationVariable("lobbyId") UUID lobbyId, MovementCommand moveCmd,
            Principal principal) {

        System.out.println("kommt was an? " + principal.getName());

        Lobby lobby = lobbyManager.getDummyLobby();

        Player player = null;
        try {
            player = lobby.getPlayerBySessionId(principal.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // nur zum testen
        if (player == null) {
            System.out.println("No player found for session " + player.getSessionId() + ", creating dummy player...");
            player = lobby.getPlayers().stream().findAny().orElse(null);
            player.getMeeples()[0].setId(moveCmd.meepleId());
            player.getMeeples()[0].setCurrentField(lobby.getField());
        }

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
