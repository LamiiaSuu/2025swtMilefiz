package de.hs_rm.de.milefiz.game.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Field;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.service.DuelService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendDiceGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;

/**
 * Controller für die Mini-Spiele innerhalb eines Duells.
 * <p>
 * Aktuell wird hier nur das Würfel-Minigame verarbeitet.
 * Der Controller:
 * <ul>
 *   <li>empfängt Würfelaktionen vom Client</li>
 *   <li>führt den Wurf im entsprechenden Mini-Spiel aus</li>
 *   <li>sendet Live-Updates an das Frontend</li>
 *   <li>setzt Verlierer-Meeples nach Spielende zurück in ihre Basis</li>
 * </ul>
 */
@Controller
public class MiniGameController {

    private final DuelService duelService;
    private final FrontendMessagingService messaging;
    private final LobbyManager lobbyManager;

    public MiniGameController(
            DuelService duelService,
            FrontendMessagingService messaging,
            LobbyManager lobbyManager
    ) {
        this.duelService = duelService;
        this.messaging = messaging;
        this.lobbyManager = lobbyManager;
    }

    /**
     * Verarbeitet einen Würfelwurf im Duel-Mini-Game.
     *
     * <p>
     * Ablauf:
     * <ol>
     *   <li>Lobby wird geladen</li>
     *   <li>Mini-Game des Duells wird geholt</li>
     *   <li>Spieler würfelt</li>
     *   <li>Frontend erhält Update</li>
     *   <li>Falls Spiel beendet -> Loser-Meeples werden zurück in die Basis gesetzt. Das können auch beide sein.</li>
     * </ol>
     *
     * @param lobbyId ID der Lobby
     * @param duelId  ID des Duells
     * @param player  Spieler, der gerade würfelt
     */
    @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/dice/roll")
    public void handleDiceRoll(
            @DestinationVariable UUID lobbyId,
            @DestinationVariable UUID duelId,
            Player player
    ) throws LobbyNotFoundException {

        // Lobby laden 
        Lobby lobby = lobbyManager.getLobby(lobbyId);

        // MiniGame holen (bereits zu diesem Zeitpunkt dem Duell zugewiesen)
        DiceGame game = (DiceGame) duelService.getMiniGame(duelId);

        // würfelt für diesen Spieler
        game.roll(player.getId());

        broadcastDiceUpdate(lobby, duelId, game);

        if (game.isFinished()) {
                sendLoserHome(lobby, duelId, game);
        }
        
    }
    

    /**
     * Setzt nach einem beendeten Duell die Loser-Meeples
     * zurück auf ihr jeweiliges Startfeld. Das können beide sein.
     *
     * <p>
     * Regeln:
     * <ul>
     *   <li>Gewinner bleibt stehen</li>
     *   <li>Verlierer gehen zurück in die Basis</li>
     *   <li>Bei Unentschieden verlieren beide</li>
     * </ul>
     *
     * <p>
     * Zusätzlich wird ein {@link FrontendMoveEvent}
     * gesendet, damit das Update im Frontend animiert wird.
     *
     * @param lobby  aktuelle Lobby
     * @param duelId ID des Duells
     * @param game   beendetes Mini-Game
     */
    private void sendLoserHome(Lobby lobby, UUID duelId, MiniGame game){
        
        var duel = duelService.getDuel(duelId);

        UUID winner = game.getWinner();

        UUID p1 = duel.getPlayer1();
        UUID p2 = duel.getPlayer2();

        Meeple m1 = lobby.getMeepleById(duel.getFirstMeeple());
        Meeple m2 = lobby.getMeepleById(duel.getSecondMeeple());

        // Hilfsmethode: Startfeld des Spielers ermitteln
        Field start1 = lobby.getBoard().getStartField(
                lobby.getPlayer(p1).getColor()
        );

        Field start2 = lobby.getBoard().getStartField(
                lobby.getPlayer(p2).getColor()
        );

        // Spieler 1 verliert?
        if (winner == null || !winner.equals(p1)) {
                lobby.getPlayer(p1).setMoved(false);
                if(lobby.getPlayer(p1).getActiveMeeple().equals(m1)){
                        lobby.getPlayer(p1).setRemainingMoves(0);
                }
                messaging.sendEvent(new LobbyMessage(
                        lobby,
                        new FrontendMoveEvent(
                                p1,
                                m1.getId(),
                                start1.getId(),
                                lobby.getPlayer(p1).getRemainingMoves(),
                                lobby.getPlayer(p1).hasMoved()
                        )
                ));
            
                m1.setCurrentField(start1);
                m1.clearLastField();
        }

        // Spieler 2 verliert?
        if (winner == null || !winner.equals(p2)) {
            lobby.getPlayer(p2).setMoved(false);
            if(lobby.getPlayer(p2).getActiveMeeple().equals(m2)){
                lobby.getPlayer(p2).setRemainingMoves(0);
            }
            messaging.sendEvent(new LobbyMessage(
                    lobby,
                    new FrontendMoveEvent(
                            p2,
                            m2.getId(),
                            start2.getId(),
                            lobby.getPlayer(p2).getRemainingMoves(),
                            lobby.getPlayer(p2).hasMoved()
                    )
            ));
            
            m2.setCurrentField(start2);
            m2.clearLastField();
        }
    }

        /**
         * Sendet den aktuellen Status des Würfel-Minigames an alle Clients der Lobby.
         *
         * <p>
         * Diese Methode wird immer dann aufgerufen, wenn sich der Zustand des
         * Duell-Minigames ändert – z. B. nach einem Würfelwurf oder nach Ablauf
         * des Timeouts.
         *
         * <p>
         * Das Frontend erhält dadurch:
         * <ul>
         *   <li>die IDs beider Spieler</li>
         *   <li>die aktuellen Würfelergebnisse</li>
         *   <li>den Gewinner (falls bereits ermittelt)</li>
         *   <li>den Finished-Status</li>
         * </ul>
         *
         * Das Frontend aktualisiert daraufhin die Duel-UI und zeigt ggf.
         * das Ergebnis an.
         *
         * @param lobby  die Lobby, in der das Duell stattfindet
         * @param duelId ID des Duells
         * @param game   aktueller Zustand des Würfel-Minigames
         */
    private void broadcastDiceUpdate(Lobby lobby, UUID duelId, DiceGame game) {

        var event = new FrontendDiceGameUpdateEvent(
                duelId,
                game.getP1(),
                game.getP2(),
                game.getRollP1(),
                game.getRollP2(),
                game.getWinner(),
                game.isFinished()
        );

        messaging.sendEvent(new LobbyMessage(lobby, event));
        }
}
