package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent;

@Service
public class GameService {

    private final DiceService diceService;
    private final ApplicationEventPublisher publisher;

    public GameService(DiceService diceService, ApplicationEventPublisher publisher) {
        this.diceService = diceService;
        this.publisher = publisher;
    }

    public void rollDice(Lobby lobby, UUID playerId) {

        // 1. Zahl würfeln
        int number = diceService.roll();

        // 2. Frontend-Event erzeugen
        FrontendRollDiceEvent event = new FrontendRollDiceEvent(playerId, number);

        // 3. Event in LobbyMessage packen
        LobbyMessage message = new LobbyMessage(lobby, event);

        // 4. Spring-Event feuern – MessagingService fängt das ab
        publisher.publishEvent(message);
    }
}
