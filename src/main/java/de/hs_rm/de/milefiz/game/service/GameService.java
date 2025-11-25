package de.hs_rm.de.milefiz.game.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent;

/**
 * Service für die Verwaltung von Spiellogik und Spielaktionen.
 * 
 * <p>Der GameService koordiniert Spielaktionen wie das Würfeln und kommuniziert
 * diese über das Event-System an das Frontend. Er fungiert als zentrale
 * Schnittstelle zwischen Frontend-Anfragen und Backend-Spiellogik.</p>
 * 
 * <h3>Hauptfunktionen:</h3>
 * <ul>
 *   <li>Würfeln für Spieler</li>
 *   <li>Event-basierte Kommunikation mit Frontend</li>
 *   <li>Koordination zwischen verschiedenen Game-Services</li>
 * </ul>
 * 
 * @author Leon Schäfer
 */
@Service
public class GameService {

    private final DiceServiceImpl diceService;
    private final ApplicationEventPublisher publisher;

    public GameService(DiceServiceImpl diceService, ApplicationEventPublisher publisher) {
        this.diceService = diceService;
        this.publisher = publisher;
    }

    /**
     * Führt einen Würfelwurf für einen Spieler aus und sendet das Ergebnis an das Frontend.
     * 
     * <p>Diese Methode orchestriert den kompletten Würfelvorgang:</p>
     * <ol>
     *   <li>Würfelt eine Zufallszahl über den DiceService</li>
     *   <li>Erstellt ein FrontendRollDiceEvent mit Spieler-ID und Würfelergebnis</li>
     *   <li>Verpackt das Event in eine LobbyMessage für lobby-spezifische Übertragung</li>
     *   <li>Publiziert das Event über Spring's Event-System</li>
     * </ol>
     * 
     * <p>Das publizierte Event wird automatisch vom MessagingService abgefangen 
     * und über WebSocket an alle Frontend-Clients der entsprechenden Lobby gesendet.</p>
     * 
     * 
     * @param lobby Die Lobby in der gewürfelt wird (bestimmt die WebSocket-Zielgruppe)
     * @param playerId Eindeutige ID des Spielers der würfelt
     *  
     * @see DiceService#roll()
     * @see FrontendRollDiceEvent
     * @see LobbyMessage
     * @see de.hs_rm.de.milefiz.messaging.FrontendMessagingServiceImpl#sendEvent(LobbyMessage)
     * 
     */
    public void rollDice(Lobby lobby, UUID playerId) {

        int number = diceService.roll();
        FrontendRollDiceEvent event = new FrontendRollDiceEvent(playerId, number);
        LobbyMessage message = new LobbyMessage(lobby, event);
        publisher.publishEvent(message);
    }
}
