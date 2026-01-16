package de.hs_rm.de.milefiz.game.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.minigames.BalloonGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.QuizGame;
import de.hs_rm.de.milefiz.game.model.minigames.EinarmigerBanditGame;
import de.hs_rm.de.milefiz.game.service.DuelResolutionService;
import de.hs_rm.de.milefiz.game.service.DuelService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendBalloonGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendDiceGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEinarmigerBanditGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendQuizGameUpdateEvent;

/**
 * Controller für die Mini-Spiele innerhalb eines Duells.
 * <p>
 * Verarbeitet zwei verschiedene Mini-Games:
 * <ul>
 * <li>Würfel-Minigame (DiceGame)</li>
 * <li>Ballon-Minigame (BalloonGame)</li>
 * </ul>
 * 
 * Der Controller:
 * <ul>
 * <li>empfängt Würfelaktionen vom Client</li>
 * <li>führt den Wurf im entsprechenden Mini-Spiel aus</li>
 * <li>sendet Live-Updates an das Frontend</li>
 * <li>setzt Verlierer-Meeples nach Spielende zurück in ihre Basis</li>
 * </ul>
 */
@Controller
public class MiniGameController {

        private static final Logger logger = LoggerFactory.getLogger(MiniGameController.class);

        private final DuelService duelService;
        private final FrontendMessagingService messaging;
        private final LobbyManager lobbyManager;
        private final DuelResolutionService duelResolutionService;

        public MiniGameController(
                        DuelService duelService,
                        FrontendMessagingService messaging,
                        LobbyManager lobbyManager,
                        DuelResolutionService duelResolutionService) {
                this.duelService = duelService;
                this.messaging = messaging;
                this.lobbyManager = lobbyManager;
                this.duelResolutionService = duelResolutionService;
        }

        /**
         * Verarbeitet einen Würfelwurf im Duel-Mini-Game.
         *
         * <p>
         * Ablauf:
         * <ol>
         * <li>Lobby wird geladen</li>
         * <li>Mini-Game des Duells wird geholt</li>
         * <li>Spieler würfelt</li>
         * <li>Frontend erhält Update</li>
         * <li>Falls Spiel beendet -> Loser-Meeples werden zurück in die Basis gesetzt.
         * Das können auch beide sein.</li>
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
                        Player player) throws LobbyNotFoundException {

                // Lobby laden
                Lobby lobby = lobbyManager.getLobby(lobbyId);

                // MiniGame holen (bereits zu diesem Zeitpunkt dem Duell zugewiesen)
                DiceGame game = (DiceGame) duelService.getMiniGame(duelId);

                // würfelt für diesen Spieler
                game.roll(player.getId());

                broadcastDiceUpdate(lobby, duelId, game);

                if (game.isFinished()) {
                        Duel duel = duelService.getDuel(duelId);
                        duelResolutionService.sendLoserHome(lobby, duel, game);
                }

        }

        /**
         * Verarbeitet einen Slot stop im Duel-Mini-Game.
         * <p>
         * Ablauf:
         * <ol>
         * <li>Lobby wird geladen</li>
         * <li>Mini-Game (in dem Fall einarmiger Bandit) des Duells wird geholt</li>
         * <li>Spieler stoppt seine Slot</li>
         * <li>Frontend erhält Update</li>
         * <li>Falls Spiel beendet -> Loser-Meeples werden zurück in die Basis
         * gesetzt.</li>
         * </ol>
         * 
         * @param lobbyId ID der Lobby
         * @param duelId  ID des Duells
         * @param player  ID des Spielers, der seine Slot stoppen will
         * @throws LobbyNotFoundException wird geworfen, wenn keine Lobby mit der
         *                                angegebenen LobbyId gefunden wurde
         * @author Leon Schäfer
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/einarmigerBandit/stop")
        public void handleSlotStop(
                        @DestinationVariable UUID lobbyId,
                        @DestinationVariable UUID duelId,
                        Player player) throws LobbyNotFoundException {

                logger.info("Player {} stopping slot in duel {} (lobby {})",
                                player.getId(), duelId, lobbyId);
                // Lobby laden
                Lobby lobby = lobbyManager.getLobby(lobbyId);

                // MiniGame holen (bereits zu diesem Zeitpunkt dem Duell zugewiesen)
                EinarmigerBanditGame game = (EinarmigerBanditGame) duelService.getMiniGame(duelId);

                // zieht den einarmigen Banditen für diesen Spieler
                game.stop(player.getId());

                broadcastEinarmigerBanditUpdate(lobby, duelId, game);

                if (game.isFinished()) {
                        logger.info("Einarmiger Bandit game finished in duel {}, winner: {}",
                                        duelId, game.getWinner());
                        Duel duel = duelService.getDuel(duelId);
                        duelResolutionService.sendLoserHome(lobby, duel, game);
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
         * <li>die IDs beider Spieler</li>
         * <li>die aktuellen Würfelergebnisse</li>
         * <li>den Gewinner (falls bereits ermittelt)</li>
         * <li>den Finished-Status</li>
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
                                game.isFinished());

                messaging.sendEvent(new LobbyMessage(lobby, event));
        }

        /**
         * Sendet den aktuellen Status des einarmigen Bandit-Minigames an alle Clients
         * der Lobby.
         *
         * <p>
         * Diese Methode wird immer dann aufgerufen, wenn sich der Zustand des
         * Duell-Minigames ändert – z. B. nach einem Slot stopp oder nach Ablauf
         * des Timeouts.
         * 
         * Bevor das event erstellt wird, wird die aktuelle Energy des Gewinners
         * gespeichert. Diese wurden zuvor, sofern ein Jackpot erreicht wurde, auf den
         * maximalen Wert gesetzt wurde (Dies geschieht in
         * {@link EinarmigerBanditGame#checkFinished()})
         *
         * <p>
         * Das Frontend erhält dadurch:
         * <ul>
         * <li>die IDs beider Spieler</li>
         * <li>die aktuellen Ergebnisse der Slots</li>
         * <li>den Gewinner (falls bereits ermittelt)</li>
         * <li>Ob ein Jackpot (alle 3 Slots sind gleich) erzielt wurde</li>
         * <li>Die maximal zu erreichende Energie, als Jackpot-Belohnung</li>
         * <li>den Finished-Status</li>
         * </ul>
         *
         * Das Frontend aktualisiert daraufhin die Duel-UI und zeigt ggf.
         * das Ergebnis an.
         *
         * @param lobby  die Lobby, in der das Duell stattfindet
         * @param duelId ID des Duells
         * @param game   aktueller Zustand des einarmigen Bandit-Minigames
         * @author Leon Schäfer
         */
        private void broadcastEinarmigerBanditUpdate(Lobby lobby, UUID duelId, EinarmigerBanditGame game) {
                logger.info("Broadcasting Einarmiger Bandit update for duel {}", duelId);

                Integer energy = null;
                if (game.getWinner() != null) {
                        energy = lobby.getPlayer(game.getWinner()).getEnergy();
                }
                var event = new FrontendEinarmigerBanditGameUpdateEvent(duelId,
                                game.getP1(),
                                game.getP2(),
                                game.getResultP1(),
                                game.getResultP2(),
                                game.getResultComp(),
                                game.getWinner(),
                                game.isJackpot(),
                                energy != null ? energy : 0,
                                game.isFinished());
                messaging.sendEvent(new LobbyMessage(lobby, event));

                logger.info("Einarmiger Bandit event sent for duel {}", duelId);

        }

        /**
         * Verarbeitet einen Klick im Ballon-Minigame.
         *
         * <p>
         * Ablauf:
         * <ol>
         * <li>Lobby wird geladen</li>
         * <li>BalloonGame des Duells wird geholt</li>
         * <li>Klick wird verarbeitet (erhöht Click-Counter und Phase)</li>
         * <li>Falls Phase sich geändert hat -> Frontend erhält Update</li>
         * <li>Falls Spiel beendet -> Loser-Meeples werden zurück in die Basis
         * gesetzt</li>
         * </ol>
         *
         * <p>
         * Ein Spieler gewinnt, wenn er Phase 4 erreicht (30+ Klicks).
         * Bei Timeout verlieren beide Spieler.
         *
         * @param lobbyId ID der Lobby
         * @param duelId  ID des Duells
         * @param player  Spieler, der gerade geklickt hat
         * @throws LobbyNotFoundException wenn die Lobby nicht gefunden wird
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/balloon/click")
        public void handleBalloonClick(@DestinationVariable UUID lobbyId,
                        @DestinationVariable UUID duelId,
                        Player player) throws LobbyNotFoundException {

                Lobby lobby = lobbyManager.getLobby(lobbyId);
                BalloonGame game = (BalloonGame) duelService.getMiniGame(duelId);

                boolean phaseChanged = game.processClick(player.getId());

                if (phaseChanged) {
                        broadcastBalloonUpdate(lobby, duelId, game);
                }

                if (game.isFinished()) {
                        Duel duel = duelService.getDuel(duelId);
                        duelResolutionService.sendLoserHome(lobby, duel, game);
                }
        }

        /**
         * Sendet den aktuellen Status des Ballon-Minigames an alle Clients der Lobby.
         *
         * <p>
         * Diese Methode wird immer dann aufgerufen, wenn sich der Zustand des
         * BalloonGames ändert – z. B. nach einem Phasenwechsel oder nach Ablauf des
         * Timeouts.
         *
         * <p>
         * Das Frontend erhält dadurch:
         * <ul>
         * <li>die IDs beider Spieler</li>
         * <li>die aktuellen Phasen beider Spieler (0-4)</li>
         * <li>den Gewinner (falls bereits ermittelt)</li>
         * <li>den Finished-Status</li>
         * </ul>
         *
         * <p>
         * Phasen-System:
         * <ul>
         * <li>Phase 0: Ballon unaufgeblasen (0 Klicks)</li>
         * <li>Phase 1: Leicht aufgeblasen (1-9 Klicks)</li>
         * <li>Phase 2: Mittel aufgeblasen (10-19 Klicks)</li>
         * <li>Phase 3: Stark aufgeblasen (20-29 Klicks)</li>
         * <li>Phase 4: Ballon geplatzt (30+ Klicks → Gewinner!)</li>
         * </ul>
         *
         * Das Frontend aktualisiert daraufhin die Ballon-Bilder und zeigt ggf.
         * das Ergebnis an.
         *
         * @param lobby  die Lobby, in der das Duell stattfindet
         * @param duelId ID des Duells
         * @param game   aktueller Zustand des Ballon-Minigames
         */
        public void broadcastBalloonUpdate(Lobby lobby, UUID duelId, BalloonGame game) {
                var event = new FrontendBalloonGameUpdateEvent(
                                duelId,
                                game.getPlayer1(),
                                game.getPlayer2(),
                                game.getPhasePlayer1(),
                                game.getPhasePlayer2(),
                                game.getWinner(),
                                game.isFinished());

                messaging.sendEvent(new LobbyMessage(lobby, event));
        }

        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/quiz/getQuestion")
        public void handleQuestionRequest(@DestinationVariable UUID lobbyId, @DestinationVariable UUID duelId,
                        Player player) throws LobbyNotFoundException {

                Lobby lobby = lobbyManager.getLobby(lobbyId);

                QuizGame game = (QuizGame) duelService.getMiniGame(duelId);

                broadcastQuizUpdate(lobby, duelId, game);
        }

        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/quiz/sendAnswer/{answerIndex}")
        public void handleAnswerRequest(@DestinationVariable UUID lobbyId, @DestinationVariable UUID duelId,
                        Player player, @DestinationVariable int answerIndex) throws LobbyNotFoundException {

                Lobby lobby = lobbyManager.getLobby(lobbyId);

                QuizGame game = (QuizGame) duelService.getMiniGame(duelId);

                game.checkAnswer(player.getId(), answerIndex);

                broadcastQuizUpdate(lobby, duelId, game);

                if (game.isFinished()) {
                        Duel duel = duelService.getDuel(duelId);
                        duelResolutionService.sendLoserHome(lobby, duel, game);
                }
        }

        public void broadcastQuizUpdate(Lobby lobby, UUID duelId, QuizGame game) {

                var event = new FrontendQuizGameUpdateEvent(duelId, game.getPlayer1(), game.getPlayer2(),
                                game.getQuestionDTO(), game.getWinner(), game.isFinished());

                messaging.sendEvent(new LobbyMessage(lobby, event));
        }
}
