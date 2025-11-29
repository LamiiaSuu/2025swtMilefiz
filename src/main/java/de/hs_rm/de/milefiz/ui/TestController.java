package de.hs_rm.de.milefiz.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingServiceImpl;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendJumpEvent;

@Controller
public class TestController {

    private FrontendMessagingServiceImpl messageingService;

    private LobbyManager lobbyManager;

    public TestController(FrontendMessagingServiceImpl messageingService, LobbyManager lobbyManager) {
        this.messageingService = messageingService;
        this.lobbyManager = lobbyManager;
    }

    @GetMapping("/test")
    public String getMethodName() {
        Lobby lobby = lobbyManager.getDummyLobby();
        Meeple meeple = new Meeple(false);
        messageingService.sendEvent(new LobbyMessage(lobby, new FrontendJumpEvent(meeple.getId())));
        return "";
    }

}
