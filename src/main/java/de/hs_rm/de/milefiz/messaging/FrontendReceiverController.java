package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.commands.RollDiceCommand;

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
        System.out.println("Received " + lobbyId.toString()  + ": " + message);
        return "Server received: " + message; // Body von Weiterleitung an alle Clients
    }

    @MessageMapping("/milefiz/lobby/{lobbyId}/rollDice")
    public void handleRollDice(@DestinationVariable UUID lobbyId, RollDiceCommand command) {
        logger.info("Player {} wants to roll dice in lobby {}", command.playerId(), lobbyId);
        
        try {
            Lobby lobby = lobbyManager.getLobby(lobbyId);
            // Das triggert das Event-System
            gameService.rollDice(lobby, command.playerId());
            
        } catch (LobbyNotFoundException e) {
            logger.warn("Lobby not found: {}", lobbyId, e);
            
        } catch (RuntimeException e) {
            logger.error("Error rolling dice: {}", e.getMessage());
        }
    }
}
