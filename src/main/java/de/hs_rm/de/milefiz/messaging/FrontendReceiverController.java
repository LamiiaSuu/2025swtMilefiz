package de.hs_rm.de.milefiz.messaging;

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
import de.hs_rm.de.milefiz.game.lobby.PlayerHasNoPermissionException;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.mapper.LobbyMapper;
import de.hs_rm.de.milefiz.game.service.GameService;
import de.hs_rm.de.milefiz.messaging.commands.EnergyCommand;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.commands.RollDiceCommand;
import de.hs_rm.de.milefiz.messaging.commands.UpdateLobbySettingsCommand;
import de.hs_rm.de.milefiz.messaging.commands.UpdatePlayerNameCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendConsumeEnergyEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendConsumeEnergyRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendCooldownFinishedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendGameStartEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendLobbyUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceRejectedEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceRejectedMovesLeftEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendSaveEnergyEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendSaveEnergyRejectedEvent;

@Controller
public class FrontendReceiverController {

    private final Logger logger = LoggerFactory.getLogger(FrontendReceiverController.class);
    private LobbyManager lobbyManager;
    private GameService gameService;
    private LobbyMapper lobbyMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private FrontendMessagingService messagingService;

    public FrontendReceiverController(LobbyManager lobbyManager, GameService gameService,
            SimpMessagingTemplate messagingTemplate, FrontendMessagingServiceImpl frontendMessagingServiceImpl,
            LobbyMapper lobbyMapper) {
        this.lobbyManager = lobbyManager;
        this.gameService = gameService;
        this.lobbyMapper = lobbyMapper;
        this.messagingTemplate = messagingTemplate;
        this.messagingService = frontendMessagingServiceImpl;
    }

    /**
     * Empfängt ein Meeple-Bewegungskommando vom Frontend über WebSocket
     * und leitet es an den {@link GameService} weiter.
     * 
     * Das vom {@link GameService} zurückgegebene {@link FrontendEvent}
     * wird automatisch an alle Clients der entsprechenden Lobby gesendet.
     *
     * @param lobbyId die eindeutige ID der Lobby, in der der Zug ausgeführt wird
     * @param moveCmd das Bewegungskommando mit Meeple-ID und Bewegungsrichtung
     * @param player  der Spieler, der den Zug ausgelöst hat
     *
     * @return ein {@link FrontendEvent}, das den Ausgang des Zuges beschreibt
     *
     * @see GameService#moveMeeple(UUID, MovementCommand, Player)
     *
     * @author Maximilian Ressel
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/move")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleMove(@DestinationVariable("lobbyId") UUID lobbyId, MovementCommand moveCmd,
            Player player) {

        logger.info("Received movement command in lobby {} from player '{}': meeple {} moving {} ",
                lobbyId,
                player != null ? player.getName() : "anonymous",
                moveCmd.meepleId(),
                moveCmd.direction());

        return gameService.moveMeeple(lobbyId, moveCmd, player);
    }

    /**
     * Empfängt ein Barrieren-Bewegungskommando vom Frontend über WebSocket
     * und leitet es an den {@link GameService} weiter.
     *
     * Das vom {@link GameService} erzeugte {@link FrontendEvent} wird an
     * alle Clients der Lobby verteilt.
     *
     * @param lobbyId     die eindeutige ID der Lobby, in der die Barriere bewegt
     *                    wird
     * @param moveBarrCmd das Kommando mit Barrieren-ID und Ziel-Feld-ID
     * @param player      der Spieler, der die Aktion ausgelöst hat
     *
     * @return ein {@link FrontendEvent}, das den Ausgang der Barrierenbewegung
     *         beschreibt
     *
     * @see GameService#moveBarrier(UUID, MoveBarrierCommand, Player)
     *
     * @author Maximilian Ressel
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/movebarrier")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleBarrierMove(@DestinationVariable("lobbyId") UUID lobbyId, MoveBarrierCommand moveBarrCmd,
            Player player) {

        logger.info(
                "Received MoveBarrierCommand in lobby {} from player '{}': barrier {} moving to field {}",
                lobbyId,
                player != null ? player.getName() : "anonymous",
                moveBarrCmd.barrierId(),
                moveBarrCmd.targetFieldId());

        return gameService.moveBarrier(lobbyId, moveBarrCmd, player);

    }

    /**
     * WebSocket Message Handler für Würfel-Aktionen in einer Lobby.
     *
     * <p>
     * Diese Methode verarbeitet eingehende Würfel-Befehle von Clients und
     * broadcastet das Würfelergebnis an alle Teilnehmer der entsprechenden
     * Lobby. Der Würfelwurf wird über den {@link GameService} durchgeführt und
     * das Ergebnis als {@link FrontendRollDiceEvent} an alle verbundenen
     * Clients gesendet.
     * </p>
     *
     * Ablauf:
     * <ol>
     * <li>Client sendet {@link RollDiceCommand} an den WebSocket-Endpoint</li>
     * <li>Methode loggt die Würfel-Anfrage mit Spieler-ID und Lobby-ID</li>
     * <li>{@link GameService#rollDice()} wird aufgerufen um Zufallszahl zu
     * generieren</li>
     * <li>Speichert die gewürfelte zahl im Spieler ab</li>
     * <li>Spieler flagt, dass er sich noch nicht bewegt hat (neuer Zug
     * beginnt)</li>
     * <li>Würfelergebnis wird in {@link FrontendRollDiceEvent} verpackt</li>
     * <li>Event wird an Topic {@code /topic/milefiz/lobby/{lobbyId}}
     * gesendet</li>
     * <li>Alle Clients der Lobby erhalten das Würfelergebnis</li>
     * </ol>
     *
     * <h4>WebSocket-Mapping:</h4>
     * <ul>
     * <li><strong>Eingang:</strong>
     * {@code /milefiz/lobby/{lobbyId}/rollDice}</li>
     * <li><strong>Ausgang:</strong> {@code /topic/milefiz/lobby/{lobbyId}}</li>
     * <li><strong>Protokoll:</strong> STOMP über WebSocket</li>
     * </ul>
     *
     * @param lobbyId die eindeutige UUID der Lobby in der gewürfelt wird
     * @param command der Würfel-Befehl vom Client, enthält die Spieler-ID
     * @return {@link FrontendRollDiceEvent} mit Lobby-ID und Würfelergebnis
     *         (1-6)
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
    public FrontendEvent handleRollDice(@DestinationVariable("lobbyId") UUID lobbyId, RollDiceCommand command,
            Player player) {
        logger.info("Player {} wants to roll dice in lobby {}", player.getId(), lobbyId);
        if (gameService.getRollDiceCooldown(player.getId()) <= 0) {
            if (player.getRemainingMoves() > 0) {
                logger.info("Cannot roll. There are still {} moves remaining for player {}", player.getRemainingMoves(),
                        player.getId());
                return new FrontendRollDiceRejectedMovesLeftEvent(command.playerId(), player.getRemainingMoves());
            }
            int number = gameService.rollDice();
            try {
                player.setRemainingMoves(number); // Spieler weiß was er gewürfelt hat
                player.setMoved(false);
                logger.info("Set {} remaining moves for player {}", number, player.getId());
            } catch (RuntimeException e) {
                logger.error("Unexpected error setting remaining moves for player {}", player.getId(), e);
            }

            gameService.addRollDiceCooldown(player.getId());
            logger.info("Player {} rolled a {} in lobby {}.", player.getId(), number, lobbyId);
            return new FrontendRollDiceEvent(player.getId(), number,
                    gameService.getRollDiceCooldown(player.getId()));
        } else {
            logger.info(
                    "Player {} tried to roll dice in Lobby {}. But they still have a cooldown of {} to roll their dice!",
                    player.getId(), lobbyId, gameService.getRollDiceCooldown(player.getId()));
            return new FrontendRollDiceRejectedEvent(player.getId(),
                    gameService.getRollDiceCooldown(player.getId()));
        }
    }

    /**
     * Wird aufgerufen wenn das Spiel losgehen soll (kann nur vom Leader
     * ausgeführt werden)
     *
     * @param lobbyId
     * @param player
     * @return
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/startGame")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleStartGame(@DestinationVariable("lobbyId") UUID lobbyId, Player player) {
        Lobby lobby = null;
        try {
            lobby = lobbyManager.getLobby(lobbyId);
        } catch (LobbyNotFoundException e) {
            e.printStackTrace();
        }
        if (!player.equals(lobby.getLeader())) {
            throw new PlayerHasNoPermissionException("Der Spieler ist kein Leader");
        }
        logger.info("Spiel {} wurde gestartet", lobbyId);
        lobby.setGameStarted(true);
        return new FrontendGameStartEvent("Das Spiel wurde gestartet!");
    }

    /**
     * Handelt bei disconnects die Spieler -> Leave aus Lobby Sende per STOMP
     * zuätzlich allen bereits in der Lobby vorhandenen Spielern ein Update
     *
     * @param event
     * @throws PlayerNotFoundException
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) throws PlayerNotFoundException {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Player player = (Player) headerAccessor.getSessionAttributes().get("player");

        Lobby lobby = lobbyManager.getLobbyFromPlayer(player);
        lobby.leave(player);

        // Wenn kein Spieler mehr drin: Lobby Löschen
        if (lobby.isEmpty()) {
            lobbyManager.deleteLobby(lobby);
        }

        // Sende per STOMP allen bereits in der Lobby vorhandenen Spielern ein Update
        messagingService.sendEvent(new LobbyMessage(lobby,
                new FrontendLobbyUpdateEvent(lobbyMapper.toDTO(lobby), "Ein Spieler ist geleavt")));
        logger.info("WebSocket disconnected - Player Token: {}", player.getPlayerToken());
    }

    /**
     * Event-Listener, der ausgelöst wird, sobald der Cooldown eines Spielers
     * abgelaufen ist.
     *
     * <p>
     * Dieser Listener reagiert auf
     * {@link FrontendCooldownFinishedEvent}-Events, die vom
     * {@link de.hs_rm.de.milefiz.game.service.CooldownService} publiziert
     * werden, sobald der Cooldown eines Spielers den Wert 0 erreicht.
     * </p>
     *
     * Ablauf:
     * <ol>
     * <li>Der Listener ermittelt anhand der playerId, in welcher {@link Lobby}
     * sich der Spieler aktuell befindet.</li>
     * <li>Es wird ein neues {@link FrontendCooldownFinishedEvent} erzeugt, das
     * zusätzlich die Lobby-ID enthält.</li>
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

    /**
     * WebSocket Message Handler für Energie-Speichern-Aktionen in einer Lobby.
     *
     * <p>
     * Diese Methode verarbeitet eingehende Energie-Speichern-Befehle von
     * Clients. Spieler können damit ihre Würfelzüge in Energie umwandeln, um
     * diese für spätere Spezialaktionen zu nutzen. Das Ergebnis wird an alle
     * Teilnehmer der Lobby gebroadcastet.
     * </p>
     *
     * Ablauf:
     * <ol>
     * <li>Client sendet {@link EnergyCommand} an den WebSocket-Endpoint</li>
     * <li>Methode loggt die Energie-Speicher-Anfrage mit Spieler-ID und
     * Lobby-ID</li>
     * <li>Validierung: Spieler darf sich noch nicht bewegt haben</li>
     * <li>Validierung: Spieler darf nicht bereits maximale Energie haben</li>
     * <li>Falls gültig: {@link Player#saveEnergy()} konvertiert gewürfelte Züge
     * in Energie</li>
     * <li>Erfolg: {@link FrontendSaveEnergyEvent} mit neuer Energie wird
     * gesendet</li>
     * <li>Fehler: {@link FrontendSaveEnergyRejectedEvent} mit Fehlermeldung
     * wird gesendet</li>
     * <li>Alle Clients der Lobby erhalten das Event</li>
     * </ol>
     *
     * <p>
     * <strong>Validierungsregeln:</strong>
     * </p>
     * <ul>
     * <li>{@code player.isMoved() == false} - Spieler darf sich in dieser Runde
     * noch nicht bewegt haben</li>
     * <li>{@code player.hasFullEnergy() == false} - Spieler darf nicht bereits
     * {@link Player#MAX_ENERGY} erreicht haben</li>
     * </ul>
     *
     * <p>
     * <strong>Erfolgsfall:</strong>
     * </p>
     * <ul>
     * <li>Verbleibende Züge werden zu Energie addiert</li>
     * <li>{@code remainingMoves} wird auf 0 gesetzt</li>
     * <li>Energie wird auf {@link Player#MAX_ENERGY} begrenzt falls nötig</li>
     * <li>{@link FrontendSaveEnergyEvent} enthält Lobby-ID und neuen
     * Energie-Wert</li>
     * </ul>
     *
     * <p>
     * <strong>Fehlerfall:</strong>
     * </p>
     * <ul>
     * <li>Spieler hat sich bereits bewegt → Energie-Speichern nicht
     * möglich</li>
     * <li>Spieler hat bereits maximale Energie → keine weitere Speicherung
     * möglich</li>
     * <li>{@link FrontendSaveEnergyRejectedEvent} enthält Fehlermeldung:
     * "Player moved or has full energy"</li>
     * </ul>
     *
     * <p>
     * <strong>WebSocket-Mapping:</strong>
     * </p>
     * <ul>
     * <li><strong>Eingang:</strong>
     * {@code /milefiz/lobby/{lobbyId}/saveEnergy}</li>
     * <li><strong>Ausgang:</strong> {@code /topic/milefiz/lobby/{lobbyId}}</li>
     * <li><strong>Protokoll:</strong> STOMP über WebSocket</li>
     * </ul>
     *
     * @param lobbyId die eindeutige UUID der Lobby in der Energie gespeichert
     *                wird
     * @param command der Energie-Befehl vom Client, enthält die Spieler-ID
     * @param player  der authentifizierte Spieler, der Energie speichern möchte
     *
     * @return {@link FrontendSaveEnergyEvent} bei Erfolg mit Lobby-ID und neuer
     *         Energie, oder {@link FrontendSaveEnergyRejectedEvent} bei ungültiger
     *         Anfrage
     *
     * @see Player#saveEnergy()
     * @see Player#hasFullEnergy()
     * @see Player#hasMoved()
     * @see FrontendSaveEnergyEvent
     * @see FrontendSaveEnergyRejectedEvent
     * @see EnergyCommand
     *
     * @author Elisabeth Gehdt
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/saveEnergy")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleSaveEnergy(@DestinationVariable("lobbyId") UUID lobbyId, EnergyCommand command,
            Player player) {
        logger.info("Player {} wants to save Energy {}", player.getId(), lobbyId);

        if (!player.hasMoved() && !player.hasFullEnergy()) {
            try {
                player.saveEnergy();
                logger.info("Saved Energy for player {}", player.getId());
            } catch (RuntimeException e) {
                logger.error("Unexpected error saving energy for Player {}", player.getId(), e);
            }

            return new FrontendSaveEnergyEvent(player.getId(), player.getEnergy(), player.getMaxEnergy(),
                    player.hasFullEnergy());
        }

        return new FrontendSaveEnergyRejectedEvent("Maximale Energie oder Meeple bereits bewegt!", player.getId());
    }

    /**
     * WebSocket Message Handler für Energie-Verbrauchsaktionen in einer Lobby.
     *
     * <p>
     * Diese Methode verarbeitet eingehende Anfragen zum Verbrauchen von Energie,
     * z. B. für eine Sprungaktion eines Spielers.
     * </p>
     *
     * <p>
     * Ablauf:
     * </p>
     * <ol>
     * <li>Client sendet einen {@link EnergyCommand} an den WebSocket-Endpunkt</li>
     * <li>Die Anfrage wird mit Spieler-ID und Lobby-ID protokolliert</li>
     * <li>Validierung: Der Spieler muss genügend Energie besitzen
     * ({@link Player#hasFullEnergy()} muss {@code true} liefern)</li>
     * <li>Bei erfolgreicher Validierung wird {@link Player#consumeEnergy()}
     * ausgeführt,
     * wodurch die Energie des Spielers reduziert wird</li>
     * <li>Ein {@link FrontendConsumeEnergyEvent} mit dem neuen Energiewert wird
     * an alle Clients der Lobby gesendet</li>
     * <li>Bei fehlender Energie wird ein {@link FrontendConsumeEnergyRejectedEvent}
     * mit einer Fehlermeldung gesendet</li>
     * </ol>
     *
     * <p>
     * <strong>Validierungsregeln:</strong>
     * </p>
     * <ul>
     * <li>{@link Player#hasFullEnergy()} muss {@code true} sein</li>
     * </ul>
     *
     * <p>
     * <strong>Erfolgsfall:</strong>
     * </p>
     * <ul>
     * <li>Die Energie des Spielers wird um die für die Aktion definierte Menge
     * reduziert</li>
     * <li>Ein {@link FrontendConsumeEnergyEvent} mit dem aktuellen Energiewert
     * und dem Status {@code hasFullEnergy} wird gesendet</li>
     * </ul>
     *
     * <p>
     * <strong>Fehlerfall:</strong>
     * </p>
     * <ul>
     * <li>Der Spieler besitzt nicht genügend Energie für die Aktion</li>
     * <li>Ein {@link FrontendConsumeEnergyRejectedEvent} mit einer Fehlermeldung
     * wird gesendet</li>
     * </ul>
     *
     * <p>
     * <strong>WebSocket-Mapping:</strong>
     * </p>
     * <ul>
     * <li><strong>Eingang:</strong>
     * {@code /milefiz/lobby/{lobbyId}/consumeEnergy}</li>
     * <li><strong>Ausgang:</strong> {@code /topic/milefiz/lobby/{lobbyId}}</li>
     * <li><strong>Protokoll:</strong> STOMP über WebSocket</li>
     * </ul>
     *
     * @param lobbyId die eindeutige UUID der Lobby, in der die Aktion ausgeführt
     *                wird
     * @param command der Energie-Befehl vom Client, enthält die Spieler-ID
     * @param player  der authentifizierte Spieler, der Energie verbrauchen möchte
     *
     * @return {@link FrontendConsumeEnergyEvent} bei erfolgreichem Energieverbrauch
     *         oder {@link FrontendConsumeEnergyRejectedEvent} bei ungültiger
     *         Anfrage
     *
     * @see Player#consumeEnergy()
     * @see Player#hasFullEnergy()
     * @see FrontendConsumeEnergyEvent
     * @see FrontendConsumeEnergyRejectedEvent
     * @see EnergyCommand
     *
     * @author Kevin Tran
     */

    @MessageMapping("/milefiz/lobby/{lobbyId}/consumeEnergy")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleConsumeEnergy(@DestinationVariable("lobbyId") UUID lobbyId, EnergyCommand command,
            Player player) {
        logger.info("Player {} wants to consume energy for jump.", player.getId());
        if (player.hasFullEnergy()) {
            try {
                player.consumeEnergy();
                logger.info("Energy consumed for player {}.", player.getId());
            } catch (RuntimeException e) {
                logger.error("Unexpected error occurred when trying to consume energy for Player {}", player.getId(),
                        e);
            }

            return new FrontendConsumeEnergyEvent(player.getId(),command.meepleId(), player.getEnergy(), player.hasFullEnergy());
        }

        return new FrontendConsumeEnergyRejectedEvent(player.getId(), "Nicht genug Energie für einen Sprung!");
    }

    /**
     * WebSocket Message Handler zur Aktualisierung der Lobby-Einstellungen
     *
     * Verarbeitet Anfragen zum Ändern von Lobby-Name und maximaler
     * Spieleranzahl. Nur der Lobby-Leader darf diese Einstellungen ändern. Das
     * Update wird an alle Clients der Lobby broadcastet.
     *
     * WebSocket: Eingang /milefiz/lobby/{lobbyId}/rename Weiterleitung
     * /topic/milefiz/lobby/{lobbyId}
     *
     * @param lobbyId                die UUID der zu aktualisierenden Lobby
     * @param lobbyUpdateSettingsCmd Command mit newLobbyName und maxPlayers
     * @param player                 der authentifizierte Leader-Spieler
     * @return FrontendLobbyUpdateEvent mit aktualisiertem Lobby-DTO
     * @throws PlayerHasNoPermissionException falls Spieler nicht Leader ist
     * @see FrontendLobbyUpdateEvent
     * @see UpdateLobbySettingsCommand
     * @see LobbyMapper
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/updateSettings")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handleLobbyUpdate(@DestinationVariable("lobbyId") UUID lobbyId,
            UpdateLobbySettingsCommand lobbyUpdateSettingsCmd,
            Player player) {
        if (!player.isLeader()) {
            throw new PlayerHasNoPermissionException();
        }
        logger.info(
                "Received UpdateLobbySettingsCommand in lobby {} from player '{}': lobby Name {} maxplayers {}",
                lobbyId,
                player.getName(),
                lobbyUpdateSettingsCmd.newLobbyName(),
                lobbyUpdateSettingsCmd.maxPlayers());

        Lobby lobby;
        try {
            lobby = lobbyManager.getLobby(lobbyId);
            lobby.setLobbyName(lobbyUpdateSettingsCmd.newLobbyName());
            lobby.setMaxPlayers(lobbyUpdateSettingsCmd.maxPlayers());
            return new FrontendLobbyUpdateEvent(lobbyMapper.toDTO(lobby), "Update der Einstellungen");
        } catch (LobbyNotFoundException e) {
            e.printStackTrace();
        }
        return new FrontendLobbyUpdateEvent(null, "");
    }

    /**
     * WebSocket Message Handler zur Aktualisierung vom Usernamen
     * 
     * Verarbeitet Anfragen zu Änderungen des Usernamen/Playernamen.
     * Update wird an alle Clients der Lobby gesendet.
     * 
     * @param lobbyId                 die UUID der zu aktualisierenden Lobby
     * @param updatePlayerNameCommand Command mit newPlayerName
     * @param player                  der authentifizierte Spieler, der seinen Namen
     *                                ändern möchte
     * @return FrontendLobbyUpdateEvent mit aktualisiertem Lobby-DTO
     * @see FrontendLobbyUpdateEvent
     * @see LobbyMapper
     * @see UpdatePlayerNameCommand
     * 
     * @author Thilo Wittmer
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/updatePlayerName")
    @SendTo("/topic/milefiz/lobby/{lobbyId}")
    public FrontendEvent handlePlayerNameChange(@DestinationVariable("lobbyId") UUID lobbyId,
            UpdatePlayerNameCommand updatePlayerNameCommand, Player player) {
        logger.info(
                "Recieved UpdatePlayerNameCommand in lobby {} from player '{}': PlayerName {}' ",
                lobbyId,
                player.getName(),
                updatePlayerNameCommand.newPlayerName());

        player.setPlayerName(updatePlayerNameCommand.newPlayerName());

        Lobby lobby;
        try {
            lobby = lobbyManager.getLobby(lobbyId);
            return new FrontendLobbyUpdateEvent(lobbyMapper.toDTO(lobby), "Update PlayerName");
        } catch (LobbyNotFoundException e) {
            e.printStackTrace();
        }
        return new FrontendLobbyUpdateEvent(null, "");

    }
}
