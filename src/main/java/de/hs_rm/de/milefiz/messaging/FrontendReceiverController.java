package de.hs_rm.de.milefiz.messaging;

import java.security.Principal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;
import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Direction;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.commands.RollDiceCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendCooldownFinishedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceRejectedEvent;

@Controller
public class FrontendReceiverController {

    private final Logger logger = LoggerFactory.getLogger(FrontendReceiverController.class);
    private LobbyManager lobbyManager;
    private GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public FrontendReceiverController(LobbyManager lobbyManager, GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.lobbyManager = lobbyManager;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Verarbeitet eingehende Bewegungsbefehle eines Spielers innerhalb einer
     * bestimmten Lobby
     * und gibt ein entsprechendes Frontend-Event an alle Clients in dieser Lobby
     * zurück.
     *
     * Diese Methode wird über einen STOMP-Nachrichtentyp unter dem Endpunkt /milefiz/lobby/{lobbyId}/move aufgerufen. 
     * Der Client sendet einen {@link MovementCommand}, der die Bewegungsrichtung und Meeple-ID enthält. 
     * Nach der Verarbeitung wird das Ergebnis (z. B. eine erfolgreiche Bewegung oder eine Fehlermeldung) 
     * an das Topic /topic/milefiz/lobby/{lobbyId} gesendet, sodass alle verbundenen Clients die Änderung erhalten.
     *
     * Ablauf:
     * 1. Die Methode ermittelt die betreffende {@link Lobby} anhand der übergebenen lobbyId.
     * 2. Der Spieler wird über das {@link Principal}-Objekt identifiziert.
     * 3. Das zu bewegende {@link Meeple} wird aus dem {@link MovementCommand} ausgelesen.
     * 4. Das Ziel-Feld wird basierend auf der angegebenen {@link Direction} vom aktuellen Feld bestimmt.
     * 5. Es erfolgen verschiedene Validierungen:
     * - Existiert das Zielfeld überhaupt?
     * - Hat der Spieler überhaupt Züge frei
     * - Blockiert ein anderes Meeple oder eine Barriere das Feld?
     * - Steht dort ein Meeple eines anderen Spielers (→ Duell)?
     * - Ist der Zug eine verbotene Rückwärtsbewegung?
     * 6. Wenn keine Regel verletzt wird, wird das Meeple auf das neue Feld gesetzt, ein Zug verbraucht und 
     * ein {@link FrontendMoveEvent} an alle Clients der Lobby gesendet.
     * 7. Bei einem ungültigen Zug wird stattdessen ein
     * {@link FrontendMoveRejectedEvent} mit einer Fehlermeldung gesendet.
     *
     * WebSocket-Mapping:
     * Eingang: /milefiz/lobby/{lobbyId}/move
     * Ausgang: /topic/milefiz/lobby/{lobbyId}
     *
     * @param lobbyId   die eindeutige ID der Lobby, in der der Zug ausgeführt wird
     * @param moveCmd   der empfangene Bewegungsbefehl mit Meeple-ID und
     *                  {@link Direction}
     * @param principal der authentifizierte Benutzer, der die Nachricht gesendet
     *                  hat
     * @return ein {@link FrontendEvent}, das entweder den erfolgreichen Zug
     *         ({@link FrontendMoveEvent}) oder einen Fehler
     *         ({@link FrontendMoveRejectedEvent}) an die Clients sendet
     */

    @MessageMapping("/milefiz/lobby/{lobbyId}/move")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleMove(@DestinationVariable("lobbyId") UUID lobbyId, MovementCommand moveCmd,
            Principal principal) {
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
            player = lobby.getPlayers().stream().findFirst().orElse(null);
        }
        System.out.println("PLAYER " + player.getPlayerToken() + " | " + player.getColor());

        // nur zum testen
        // lobby.setBoard(gameService.getTestBoard());
        player.getMeeples()[0].setId(moveCmd.meepleId());
        if (player.getMeeples()[0].getCurrentField() == null) {
            player.getMeeples()[0].setCurrentField(lobby.getBoard().getStartGreen());
        }

        Board board = lobby.getBoard();
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

        if (!player.canMove()) {
            logger.info("No more moves left");
            return new FrontendMoveRejectedEvent("no moves left");
        }

        for (Meeple tempBarrier : board.getBarriers()) {
            if (tempBarrier.getCurrentField().equals(nextField)) {
                // TODO player loses all unspent steps
                System.out.println("reached blockade, cant go any further!");
                return new FrontendMoveRejectedEvent("ran into barrier");
            }
        }

        for (Player tempPlayer : lobby.getPlayers()) {
            if (player.equals(tempPlayer)) {
                continue;
            }

            for (Meeple tempMeeple : tempPlayer.getMeeples()) {
                // Keine Barriere und Meeple vom anderen Spieler steht drauf
                if (!tempMeeple.isBarrier() && tempMeeple.getCurrentField().equals(nextField)) {
                    // TODO duel starts !!! Erst wenn letzter Move des Wuerfel-Zuges
                    System.out.println("oh oh, looks like its time to duel!");
                    return new FrontendMoveRejectedEvent("time to duel first");
                }
            }
        }

        // Rückwärtsbewegung nicht erlaubt
        if (nextField.equals(lastField)) {
            System.out.println("cannot change direction!");
            return new FrontendMoveRejectedEvent("cannot change direction!");
        }

        // Spielfeld-Zustand aktualisieren
        // lastField wird jetzt im Meeple.setCurrentField aktualisiert
        meeple.setCurrentField(nextField);

        //Spieler nutzt einen Zug
        player.useMove();

        // Erfolgreiche Bewegung an Clients senden
        FrontendMoveEvent move = new FrontendMoveEvent(
                meeple.getId(),
                nextField.getId(),
                player.getRemainingMoves());

        return move;
    }

    /**
 * WebSocket Message Handler für Würfel-Aktionen in einer Lobby.
 * 
 * <p>Diese Methode verarbeitet eingehende Würfel-Befehle von Clients und 
 * broadcastet das Würfelergebnis an alle Teilnehmer der entsprechenden Lobby.
 * Der Würfelwurf wird über den {@link GameService} durchgeführt und das Ergebnis
 * als {@link FrontendRollDiceEvent} an alle verbundenen Clients gesendet.</p>
 * 
 * Ablauf:
 * <ol>
 *   <li>Client sendet {@link RollDiceCommand} an den WebSocket-Endpoint</li>
 *   <li>Methode loggt die Würfel-Anfrage mit Spieler-ID und Lobby-ID</li>
 *   <li>{@link GameService#rollDice()} wird aufgerufen um Zufallszahl zu generieren</li>
 *   <li> Speichert die gewürfelte zahl im Spieler ab</li>
 *   <li>Würfelergebnis wird in {@link FrontendRollDiceEvent} verpackt</li>
 *   <li>Event wird an Topic {@code /topic/milefiz/lobby/{lobbyId}} gesendet</li>
 *   <li>Alle Clients der Lobby erhalten das Würfelergebnis</li>
 * </ol>
 * 
 * <h4>WebSocket-Mapping:</h4>
 * <ul>
 *   <li><strong>Eingang:</strong> {@code /milefiz/lobby/{lobbyId}/rollDice}</li>
 *   <li><strong>Ausgang:</strong> {@code /topic/milefiz/lobby/{lobbyId}}</li>
 *   <li><strong>Protokoll:</strong> STOMP über WebSocket</li>
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
        if(gameService.getRollDiceCooldown(command.playerId()) <= 0){
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
            return new FrontendRollDiceEvent(command.playerId(), number, gameService.getRollDiceCooldown(command.playerId()));
        }
        else{
            logger.info("Player {} tried to roll dice in Lobby {}. But they still have a cooldown of {} to roll their dice!", command.playerId(), lobbyId, gameService.getRollDiceCooldown(command.playerId()));
            return new FrontendRollDiceRejectedEvent(command.playerId(), gameService.getRollDiceCooldown(command.playerId()));
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
     * <p>Dieser Listener reagiert auf {@link FrontendCooldownFinishedEvent}-Events,
     * die vom {@link de.hs_rm.de.milefiz.game.service.CooldownService} publiziert 
     * werden, sobald der Cooldown eines Spielers den Wert 0 erreicht.</p>
     *
     * Ablauf:
     * <ol>
     *   <li>Der Listener ermittelt anhand der playerId, in welcher {@link Lobby}
     *       sich der Spieler aktuell befindet.</li>
     *   <li>Es wird ein neues {@link FrontendCooldownFinishedEvent} erzeugt,
     *       das zusätzlich die Lobby-ID enthält.</li>
     *   <li>Dieses Event wird via STOMP über den WebSocket-Broker an alle Clients
     *       der betroffenen Lobby gesendet.</li>
     * </ol>
     *
     * WebSocket-Ausgang:
     * <ul>
     *   <li><strong>Topic:</strong> {@code /topic/milefiz/lobby/{lobbyId}}</li>
     *   <li>Enthält: {@code playerId} und {@code lobbyId}</li>
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
            lobby.getId()
        );

        messagingTemplate.convertAndSend(
            "/topic/milefiz/lobby/" + lobby.getId(),
            payload
        );
    }
}
