package de.hs_rm.de.milefiz.messaging;

import java.security.Principal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.commands.RollDiceCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
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
            Principal principal, SimpMessageHeaderAccessor sha) {
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
        lobby.setField(gameService.getTestBoard());
        player.getMeeples()[0].setId(moveCmd.meepleId());
        if (player.getMeeples()[0].getCurrentField() == null) {
            player.getMeeples()[0].setCurrentField(lobby.getField());
        }

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

    /**
     * Handelt bei disconnects die Spieler -> Leave aus Lobby
     * @param event
     * @throws PlayerNotFoundException
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) throws PlayerNotFoundException {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String playerToken = (String) headerAccessor.getSessionAttributes().get("player-token");
        Player player = lobbyManager.getPlayerByTokenFromLobbies(playerToken);
        Lobby lobby = lobbyManager.getLobbyFromPlayer(player);
        lobby.leave(player);
        logger.info("WebSocket disconnected - Player Token: {}", playerToken);
    }
}
