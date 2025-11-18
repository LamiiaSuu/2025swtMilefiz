package de.hs_rm.de.milefiz.ui;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.hs_rm.de.milefiz.messaging.FrontendJumpEvent;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingServiceImpl;

@Controller
public class TestController {

    private FrontendMessagingServiceImpl messageingService;

    public TestController(FrontendMessagingServiceImpl messageingService) {
        this.messageingService = messageingService;
    }

    @GetMapping("/testMessage")
    public String getMethodName() {
        messageingService.sendEvent(new FrontendJumpEvent(UUID.randomUUID(), UUID.randomUUID()));
        return "";
    }

}
