package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.service.GameService;

@Controller
public class FrontendReceiverController {

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
        System.out.println("Player " + command.playerId() + " wants to roll dice in lobby " + lobbyId);
        
        try {
            Lobby lobby = lobbyManager.getLobby(lobbyId);
            // Das triggert das Event-System
            gameService.rollDice(lobby, command.playerId());
            
        } catch (Exception e) {
            System.err.println("Error rolling dice: " + e.getMessage());
        }
    }
    
    public record RollDiceCommand(UUID playerId) {}
}
