package de.hs_rm.de.milefiz.messaging;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;

@Controller
public class FrontendReceiverController {

    private LobbyManager lobbyManager;

    public FrontendReceiverController(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @MessageMapping("/milefiz/lobby/{lobbyId}")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public String handleMessage(@DestinationVariable UUID lobbyId, String message) {
        System.out.println("Received " + lobbyId.toString()  + ": " + message);
        return "Server received: " + message; // Body von Weiterleitung an alle Clients
    }

}
