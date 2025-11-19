package de.hs_rm.de.milefiz.messaging;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class FrontendReceiverController {

    @MessageMapping("/milefiz")
    @SendTo("/topic/milefiz")
    public String handleMessage(String message) {
        System.out.println("Received: " + message);
        return "Server received: " + message; // Body von Weiterleitung an alle Clients
    }

}
