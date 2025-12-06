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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.commands.RollDiceCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendCooldownFinishedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceRejectedEvent;

@Controller
public class FrontendReceiverController {

    private final Logger logger = LoggerFactory.getLogger(FrontendReceiverController.class);
    private LobbyManager lobbyManager;
    private GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public FrontendReceiverController(LobbyManager lobbyManager, GameService gameService,
            SimpMessagingTemplate messagingTemplate) {
        this.lobbyManager = lobbyManager;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Empfängt einen Bewegungsbefehl
     * ({@link de.hs_rm.de.milefiz.messaging.commands.MovementCommand})
     * vom Frontend über WebSocket und leitet ihn an den GameService zur
     * Verarbeitung weiter.
     *
     * Diese Methode fungiert als Bindeglied zwischen der WebSocket-Kommunikation
     * und der serverseitigen Spiellogik. Sie wird aufgerufen, wenn ein Spieler
     * im Frontend eine Bewegung für eine seiner Spielfiguren (Meeples) ausführt.
     *
     * Ablauf:
     * - Empfang der Nachricht über den Endpunkt
     * {@code /milefiz/lobby/{lobbyId}/move}
     * - Logging des Befehls mit relevanten Daten (Lobby, Spieler, Meeple-ID,
     * Richtung)
     * - Weiterleitung an
     * {@link de.hs_rm.de.milefiz.game.service.GameService#moveMeeple(UUID, MovementCommand, Principal, SimpMessageHeaderAccessor)}
     * - Rückgabe des vom Service erzeugten
     * {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent}
     * - Automatische Weiterleitung des Ergebnisses an alle Clients der betroffenen
     * Lobby über {@code /topic/milefiz/lobby/{lobbyId}}
     *
     * @param lobbyId   die eindeutige ID der Lobby, in der der Zug ausgeführt wird
     * @param moveCmd   der vom Frontend übermittelte Bewegungsbefehl mit Meeple-ID
     *                  und Bewegungsrichtung
     * @param principal der Spieler (bzw. dessen Benutzerkontext), der den Zug
     *                  ausgelöst hat
     * @param sha       der WebSocket-Header mit Sitzungsinformationen (z. B.
     *                  Session-ID)
     * @return ein {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent}, das
     *         das Ergebnis des Zuges beschreibt
     *
     *         Author: Maximilian Ressel
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/move")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleMove(@DestinationVariable("lobbyId") UUID lobbyId, MovementCommand moveCmd,
            Principal principal, SimpMessageHeaderAccessor sha) {

        logger.info("Received movement command in lobby {} from player '{}': meeple {} moving {} (sessionId={})",
                lobbyId,
                principal != null ? principal.getName() : "anonymous",
                moveCmd.meepleId(),
                moveCmd.direction(),
                sha.getSessionId());

        return gameService.moveMeeple(lobbyId, moveCmd, principal, sha);
    }

    /**
     * Empfängt einen Barrierenbewegungsbefehl
     * ({@link de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand})
     * vom Frontend über WebSocket und leitet ihn an den GameService zur
     * Verarbeitung weiter.
     *
     * Diese Methode wird aufgerufen, wenn ein Spieler im Frontend eine Barriere
     * verschiebt,
     * nachdem einer seiner Meeples direkt auf dieser Barriere gelandet ist.
     *
     * Ablauf:
     * - Empfang der Nachricht über den Endpunkt
     * {@code /milefiz/lobby/{lobbyId}/movebarrier}
     * - Logging des Befehls (Lobby, Spieler, Barrieren-ID, Ziel-Feld-ID)
     * - Weiterleitung an {@link de.hs_rm.de.milefiz.game.service.GameService#moveBarrier(UUID, MoveBarrierCommand, Principal, SimpMessageHeaderAccessor)}
     * - Rückgabe des vom Service erzeugten {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent}
     * - Automatische Weiterleitung des Ergebnisses an alle Clients der betroffenen
     * Lobby über {@code /topic/milefiz/lobby/{lobbyId}}
     *
     * @param lobbyId     die eindeutige ID der Lobby, in der die Barriere
     *                    verschoben wird
     * @param moveBarrCmd der vom Frontend übermittelte Befehl mit Barrieren-ID und
     *                    Ziel-Feld-ID
     * @param principal   der Spieler (bzw. dessen Benutzerkontext), der die
     *                    Barriere verschiebt
     * @param sha         der WebSocket-Header mit Sitzungsinformationen (z. B.
     *                    Session-ID)
     * @return ein {@link de.hs_rm.de.milefiz.messaging.events.FrontendEvent}, das
     *         das Ergebnis der Barrierenbewegung beschreibt
     *
     *         Author: Maximilian Ressel
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/movebarrier")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleBarrierMove(@DestinationVariable("lobbyId") UUID lobbyId, MoveBarrierCommand moveBarrCmd,
            Principal principal, SimpMessageHeaderAccessor sha) {

        logger.info(
                "Received MoveBarrierCommand in lobby {} from player '{}': barrier {} moving to field {} (sessionId={})",
                lobbyId,
                principal != null ? principal.getName() : "anonymous",
                moveBarrCmd.barrierId(),
                moveBarrCmd.targetFieldId(),
                sha.getSessionId());

        return gameService.moveBarrier(lobbyId, moveBarrCmd, principal, sha);
    }

    /**
     * WebSocket Message Handler für Würfel-Aktionen in einer Lobby.
     * 
     * <p>
     * Diese Methode verarbeitet eingehende Würfel-Befehle von Clients und
     * broadcastet das Würfelergebnis an alle Teilnehmer der entsprechenden Lobby.
     * Der Würfelwurf wird über den {@link GameService} durchgeführt und das
     * Ergebnis
     * als {@link FrontendRollDiceEvent} an alle verbundenen Clients gesendet.
     * </p>
     * 
     * Ablauf:
     * <ol>
     * <li>Client sendet {@link RollDiceCommand} an den WebSocket-Endpoint</li>
     * <li>Methode loggt die Würfel-Anfrage mit Spieler-ID und Lobby-ID</li>
     * <li>{@link GameService#rollDice()} wird aufgerufen um Zufallszahl zu
     * generieren</li>
     * <li>Speichert die gewürfelte zahl im Spieler ab</li>
     * <li>Würfelergebnis wird in {@link FrontendRollDiceEvent} verpackt</li>
     * <li>Event wird an Topic {@code /topic/milefiz/lobby/{lobbyId}} gesendet</li>
     * <li>Alle Clients der Lobby erhalten das Würfelergebnis</li>
     * </ol>
     * 
     * <h4>WebSocket-Mapping:</h4>
     * <ul>
     * <li><strong>Eingang:</strong> {@code /milefiz/lobby/{lobbyId}/rollDice}</li>
     * <li><strong>Ausgang:</strong> {@code /topic/milefiz/lobby/{lobbyId}}</li>
     * <li><strong>Protokoll:</strong> STOMP über WebSocket</li>
     * </ul>
     * 
     * @param lobbyId die eindeutige UUID der Lobby in der gewürfelt wird
     * @param command der Würfel-Befehl vom Client, enthält die Spieler-ID
     * @return {@link FrontendRollDiceEvent} mit Lobby-ID und Würfelergebnis (1-6)
     * 
     * @see GameService#rollDice()
     * @see FrontendRollDiceEvent
     * @see RollDiceCommand
     * @see FrontendRollDiceRejectedEvent
     * 
     * @author Leon Schäfer
     * 
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/rollDice")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleRollDice(@DestinationVariable("lobbyId") UUID lobbyId, RollDiceCommand command) {
        logger.info("Player {} wants to roll dice in lobby {}", command.playerId(), lobbyId);
        if (gameService.getRollDiceCooldown(command.playerId()) <= 0) {
            int number = gameService.rollDice();
            try {
                Lobby lobby = lobbyManager.getLobby(lobbyId);
                Player player = lobby.getPlayers().stream()
                        .filter(p -> p.getId().equals(command.playerId()))
                        .findFirst()
                        .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

                player.setRemainingMoves(number);
                logger.info("Set {} remaining moves for player {}", number, player.getId());

            } catch (LobbyNotFoundException e) {
                logger.error("Lobby {} not found for dice roll", lobbyId, e);
            } catch (PlayerNotFoundException e) {
                logger.error("Player {} not found in lobby {}", command.playerId(), lobbyId, e);
            } catch (RuntimeException e) {
                logger.error("Unexpected error setting remaining moves for player {}", command.playerId(), e);
            }

            gameService.addRollDiceCooldown(command.playerId());
            logger.info("Player {} rolled a {} in lobby {}.", command.playerId(), number, lobbyId);
            return new FrontendRollDiceEvent(command.playerId(), number,
                    gameService.getRollDiceCooldown(command.playerId()));
        } else {
            logger.info(
                    "Player {} tried to roll dice in Lobby {}. But they still have a cooldown of {} to roll their dice!",
                    command.playerId(), lobbyId, gameService.getRollDiceCooldown(command.playerId()));
            return new FrontendRollDiceRejectedEvent(command.playerId(),
                    gameService.getRollDiceCooldown(command.playerId()));
        }
    }

    /**
     * Handelt bei disconnects die Spieler -> Leave aus Lobby
     * 
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

    /**
     * Event-Listener, der ausgelöst wird, sobald der Cooldown eines Spielers
     * abgelaufen ist.
     *
     * <p>
     * Dieser Listener reagiert auf {@link FrontendCooldownFinishedEvent}-Events,
     * die vom {@link de.hs_rm.de.milefiz.game.service.CooldownService} publiziert
     * werden, sobald der Cooldown eines Spielers den Wert 0 erreicht.
     * </p>
     *
     * Ablauf:
     * <ol>
     * <li>Der Listener ermittelt anhand der playerId, in welcher {@link Lobby}
     * sich der Spieler aktuell befindet.</li>
     * <li>Es wird ein neues {@link FrontendCooldownFinishedEvent} erzeugt,
     * das zusätzlich die Lobby-ID enthält.</li>
     * <li>Dieses Event wird via STOMP über den WebSocket-Broker an alle Clients
     * der betroffenen Lobby gesendet.</li>
     * </ol>
     *
     * WebSocket-Ausgang:
     * <ul>
     * <li><strong>Topic:</strong> {@code /topic/milefiz/lobby/{lobbyId}}</li>
     * <li>Enthält: {@code playerId} und {@code lobbyId}</li>
     * </ul>
     *
     * @param event das ursprüngliche CooldownFinishedEvent mit der Spieler-ID
     */
    @EventListener
    public void handleCooldownFinished(FrontendCooldownFinishedEvent event) {
        logger.info("Cooldown finished for Player {} in Lobby {}",
                event.playerId(), lobbyManager.getLobbyFromPlayerUUID(event.playerId()).getId());

        Lobby lobby = lobbyManager.getLobbyFromPlayerUUID(event.playerId());

        var payload = new FrontendCooldownFinishedEvent(
                event.playerId(),
                lobby.getId());

        messagingTemplate.convertAndSend(
                "/topic/milefiz/lobby/" + lobby.getId(),
                payload);
    }
}
