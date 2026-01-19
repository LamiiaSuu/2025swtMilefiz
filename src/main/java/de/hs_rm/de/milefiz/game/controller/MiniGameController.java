package de.hs_rm.de.milefiz.game.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.LobbyNotFoundException;
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.model.dto.minigames.MonkeyTypeProgressDTO;
import de.hs_rm.de.milefiz.game.model.minigames.BalloonGame;
import de.hs_rm.de.milefiz.game.model.minigames.ColorbrainGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.QuizGame;
import de.hs_rm.de.milefiz.game.model.minigames.monkeyTypeGame.MonkeyTypeGame;
import de.hs_rm.de.milefiz.game.model.minigames.SlotMachineGame;
import de.hs_rm.de.milefiz.game.model.minigames.MathGame;
import de.hs_rm.de.milefiz.game.model.minigames.RockPaperScissorsGame;
import de.hs_rm.de.milefiz.game.service.DuelResolutionService;
import de.hs_rm.de.milefiz.game.service.DuelService;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.commands.MathGameCommand;
import de.hs_rm.de.milefiz.messaging.commands.ToggleSelectionModeCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendBalloonGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendColorbrainGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendDiceGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendSlotMachineGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMathGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendQuizGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRockPaperScissorsGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMonkeyTypeGameUpdateEvent;

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
         * Schaltet den Auswahlmodus für Minispiele zwischen
         * zufällig (RANDOM) und festgelegter Reihenfolge (IN_ORDER) um.
         *
         * 
         * Diese Methode wird über einen STOMP-WebSocket-Endpunkt vom Frontend
         * ausgelöst (z. B. per Hotkey).
         * Der Client übermittelt dabei, ob Minispiele zufällig ausgewählt werden
         * sollen.
         * 
         *
         * 
         * Ablauf:
         * 
         * Empfängt den Toggle-Befehl aus dem Frontend
         * Liest den gewünschten Auswahlmodus aus dem Command
         * Delegiert die eigentliche Umschaltung an den {@link DuelService}
         * 
         * 
         *
         * 
         * Die {@code lobbyId} wird aktuell nur zur Routing-Zuordnung verwendet.
         * Die eigentliche Zustandsänderung erfolgt zentral im {@code DuelService}.
         * 
         *
         * @param lobbyId
         *        Die eindeutige ID der Lobby, aus der der Toggle-Befehl stammt
         * @param command
        *         Enthält die Information, ob Minispiele zufällig ausgewählt werden sollen ({@code true} = RANDOM, {@code false} = IN_ORDER)
         *
         * @see DuelService#setSelectRandom(boolean)
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/toggleMinigameSelectionMode")
        public void toggleMinigameSelection(
                        @DestinationVariable UUID lobbyId, ToggleSelectionModeCommand command) {
                System.out.println("CONTROLLER: command.selectRandom = " + command.selectRandomMinigame());
                duelService.setSelectRandom(command.selectRandomMinigame());
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
         * <li>Mini-Game (in dem Fall Slot Machine) des Duells wird geholt</li>
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
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/slotMachine/stop")
        public void handleSlotStop(
                        @DestinationVariable UUID lobbyId,
                        @DestinationVariable UUID duelId,
                        Player player) throws LobbyNotFoundException {

                logger.info("Player {} stopping slot in duel {} (lobby {})",
                                player.getId(), duelId, lobbyId);
                // Lobby laden
                Lobby lobby = lobbyManager.getLobby(lobbyId);

                // MiniGame holen (bereits zu diesem Zeitpunkt dem Duell zugewiesen)
                SlotMachineGame game = (SlotMachineGame) duelService.getMiniGame(duelId);

                // zieht den einarmigen Banditen für diesen Spieler
                game.stop(player.getId());

                broadcastSlotMachineGameUpdate(lobby, duelId, game);

                if (game.isFinished()) {
                        logger.info("Slot Machine Game finished in duel {}, winner: {}",
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
         * {@link SlotMachineGame#checkFinished()})
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
        private void broadcastSlotMachineGameUpdate(Lobby lobby, UUID duelId, SlotMachineGame game) {
                logger.info("Broadcasting Slot Machine update for duel {}", duelId);

                Integer energy = null;
                if (game.getWinner() != null) {
                        energy = lobby.getPlayer(game.getWinner()).getEnergy();
                }
                var event = new FrontendSlotMachineGameUpdateEvent(duelId,
                                game.getPlayer1().getId(),
                                game.getPlayer2().getId(),
                                game.getResultPlayer1(),
                                game.getResultPlayer2(),
                                game.getResultComp(),
                                game.getWinner(),
                                game.isJackpot(),
                                energy != null ? energy : 0,
                                game.isFinished());
                messaging.sendEvent(new LobbyMessage(lobby, event));

                logger.info("SlotMachine event sent for duel {}", duelId);

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

        /**
         * Verarbeitet Spieler-Eingaben für das Kopfrechnen-Minispiel.
         *
         * @param lobbyId         UUID der Lobby
         * @param duelId          UUID des Duells
         * @param mathGameCommand Command mit {@code input}
         * @param player          Spieler
         * @throws LobbyNotFoundException
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/math/input")
        public void handleMathInput(@DestinationVariable UUID lobbyId,
                        @DestinationVariable UUID duelId,
                        MathGameCommand mathGameCommand, Player player) throws LobbyNotFoundException {
                logger.info("{}", mathGameCommand);
                logger.info("Player {} locked input {} in math game duel {} (lobby {})",
                                player.getId(), mathGameCommand.input(), duelId, lobbyId);

                // MiniGame holen (bereits zu diesem Zeitpunkt dem Duell zugewiesen)
                MathGame game = (MathGame) duelService.getMiniGame(duelId);

                // setze value für spieler
                game.setValue(player.getId(), mathGameCommand.input());
        }

        /**
         * Verarbeitet Anfrage für Term durch Spieler.
         * Sendet zu Duell zugehörigen Term über STOMP.
         * 
         * @param lobbyId UUID der Lobby
         * @param duelId  UUID des Duells
         * @param player  Spiele
         * @throws LobbyNotFoundException
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/math/term")
        public void handleMathRequest(@DestinationVariable UUID lobbyId, @DestinationVariable UUID duelId,
                        Player player) throws LobbyNotFoundException {

                Lobby lobby = lobbyManager.getLobby(lobbyId);

                MathGame mathGame = (MathGame) duelService.getMiniGame(duelId);

                var update = new FrontendMathGameUpdateEvent(
                                duelId,
                                mathGame.getPlayer1(),
                                mathGame.getPlayer2(),
                                null,
                                null,
                                mathGame.getTermRepresentaion(),
                                null,
                                null,
                                mathGame.isFinished());

                messaging.sendEvent(new LobbyMessage(lobby, update));
        }

        /**
         * Verarbeitet eine Anfrage für das Target-Word im MonkeyTypeGame.
         * 
         * Analog zu /quiz/getQuestion: Frontend fragt das Wort an,
         * Backend sendet Update-Event mit dem Wort.
         *
         * @param lobbyId ID der Lobby
         * @param duelId  ID des Duells
         * @param player  der Spieler, der das Zielwort anfordert
         * @throws LobbyNotFoundException wenn die Lobby nicht existiert
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/monkeyType/getWord")
        public void handleGetWordRequest(
                        @DestinationVariable UUID lobbyId,
                        @DestinationVariable UUID duelId,
                        Player player) throws LobbyNotFoundException {

                logger.info("Player {} requesting word for monkeyType duel {}",
                                player.getId(), duelId);

                Lobby lobby = lobbyManager.getLobby(lobbyId);
                MonkeyTypeGame game = (MonkeyTypeGame) duelService.getMiniGame(duelId);

                // Initialisiere Spieler
                if (game.getPlayer1() == null) {
                        logger.info("Initializing players for monkeyType game {}", duelId);
                        Duel duel = duelService.getDuel(duelId);
                        game.initPlayers(duel.getPlayer1(), duel.getPlayer2(), lobby);
                }

                broadcastMonkeyTypeUpdate(lobby, duelId, game);
        }

        /**
         * Sendet den aktuellen Zustand des MonkeyTypeGames an alle Clients
         * innerhalb der angegebenen Lobby.
         * <p>
         * Diese Methode wird verwendet, um alle Spieler zu synchronisieren, z. B.:
         * <ul>
         * <li>nach einer gültigen Fortschrittsmeldung ({@code /progress})</li>
         * <li>nach der Initialisierung des Spiels (Zielwort/Spieler gesetzt)</li>
         * <li>nach Spielende (Gewinner ermittelt)</li>
         * </ul>
         *
         * @param lobby  die Lobby, in der das Duell stattfindet
         * @param duelId ID des Duells
         * @param game   aktueller Zustand des MonkeyTypeGames
         */
        public void broadcastMonkeyTypeUpdate(Lobby lobby, UUID duelId, MonkeyTypeGame game) {

                var event = new FrontendMonkeyTypeGameUpdateEvent(
                                duelId,
                                game.getPlayer1(),
                                game.getPlayer2(),
                                game.getTargetWord(),
                                game.getPlayer1Progress(),
                                game.getPlayer2Progress(),
                                game.getWinner(),
                                game.isFinished(),
                                game.getStartedAt());

                messaging.sendEvent(new LobbyMessage(lobby, event));
        }

        /**
         * Verarbeitet Fortschrittsmeldungen (Progress) für das MonkeyType-Minigame.
         * <p>
         * Die Validierung einzelner Tasteneingaben erfolgt clientseitig.
         * Der Server prüft ausschließlich, ob der gemeldete Fortschritt
         * gültig ist (z. B. keine Sprünge) und entscheidet autoritativ
         * über Spielende und Gewinner.
         * <p>
         * Ein Status-Update wird nur dann an alle Clients gesendet,
         * wenn sich der Spielzustand durch den Progress geändert hat.
         *
         * @param lobbyId  ID der Lobby
         * @param duelId   ID des Duells
         * @param progress das übermittelte Progress-DTO (Spieler-ID und neuer
         *                 Fortschritt)
         * @throws LobbyNotFoundException wenn die Lobby nicht existiert
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/monkeyType/progress")
        public void handleTypingProgress(
                        @DestinationVariable UUID lobbyId,
                        @DestinationVariable UUID duelId,
                        @Payload MonkeyTypeProgressDTO progress) throws LobbyNotFoundException {

                Lobby lobby = lobbyManager.getLobby(lobbyId);
                MonkeyTypeGame game = (MonkeyTypeGame) duelService.getMiniGame(duelId);

                // Verarbeite die Eingabe
                boolean changed = game.processInput(progress.playerId(), progress.progress());

                // Sende Update an alle Clients wenn progress aktualisiert wurde
                if (changed)
                        broadcastMonkeyTypeUpdate(lobby, duelId, game);

                // Wenn Spiel beendet, Verlierer zurücksetzen
                if (game.isFinished()) {
                        Duel duel = duelService.getDuel(duelId);
                        duelResolutionService.sendLoserHome(lobby, duel, game);
                }
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
                                game.getQuestionDTO(), game.getCorrectAnswer(), game.getWinner(), game.isFinished());

                messaging.sendEvent(new LobbyMessage(lobby, event));
        }

        /**
         * Verarbeitet die Zugauswahl eines Spielers im Schere-Stein-Papier-Duell.
         *
         * Diese Methode wird aufgerufen, wenn ein Spieler seinen Zug
         * (ROCK, PAPER oder SCISSORS) an das Backend sendet. Der Zug wird
         * im entsprechenden Mini-Game gespeichert und der aktuelle
         * Spielstand an alle Clients im Lobby-Kontext verteilt.
         *
         * Sobald das Mini-Game beendet ist, wird die Duellauflösung
         * angestoßen und der Verlierer auf sein Startfeld zurückgesetzt.
         *
         * @param lobbyId ID der Lobby, in der das Duell stattfindet
         * @param duelId  ID des Duells
         * @param move    Gewählter Zug des Spielers
         * @param player  Der Spieler, der den Zug ausgeführt hat
         * @throws LobbyNotFoundException Falls die angegebene Lobby nicht existiert
         * 
         * @author Maximilian Ressel
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/rockpaperscissors/choose")
        public void handleChooseMove(
                        @DestinationVariable UUID lobbyId,
                        @DestinationVariable UUID duelId,
                        String move,
                        Player player) throws LobbyNotFoundException {

                logger.info("Schere Stein Papier Move from player {} move: {}", player.getId(), move);

                Lobby lobby = lobbyManager.getLobby(lobbyId);

                RockPaperScissorsGame game = (RockPaperScissorsGame) duelService.getMiniGame(duelId);

                game.choose(player.getId(), move);

                broadcastRockPaperScissorsUpdate(lobby, duelId, game);

                if (game.isFinished()) {
                        Duel duel = duelService.getDuel(duelId);
                        duelResolutionService.sendLoserHome(lobby, duel, game);
                }

        }

        /**
         * Sendet ein Update-Event zum aktuellen Zustand des
         * Schere-Stein-Papier-Mini-Games an alle Clients der Lobby.
         *
         * Das Event enthält die Spieler, ihre Züge, den Gewinner
         * sowie den Status des Spiels.
         *
         * @param lobby  Lobby, an die das Update gesendet wird
         * @param duelId ID des Duells
         * @param game   Aktueller Zustand des Mini-Games
         * 
         * @author Maximilian Ressel
         */
        private void broadcastRockPaperScissorsUpdate(Lobby lobby, UUID duelId, RockPaperScissorsGame game) {

                var event = new FrontendRockPaperScissorsGameUpdateEvent(
                                duelId,
                                game.getP1(),
                                game.getP2(),
                                game.getMoveP1(),
                                game.getMoveP2(),
                                game.getWinner(),
                                game.isFinished());

                messaging.sendEvent(new LobbyMessage(lobby, event));
        }

        /**
         * 
         * @param lobbyId
         * @param duelId
         * @param player
         * @param clickedColorName
         * @throws LobbyNotFoundException
         */
        @MessageMapping("/milefiz/lobby/{lobbyId}/duel/{duelId}/colorbrain/click")
        public void handleColorbrainClick(@DestinationVariable UUID lobbyId, @DestinationVariable UUID duelId,
                        Player player, @Payload String clickedColorName) throws LobbyNotFoundException {

                Lobby lobby = lobbyManager.getLobby(lobbyId);
                ColorbrainGame game = (ColorbrainGame) duelService.getMiniGame(duelId);

                // Quotes entfernen falls Payload als JSON-String kommt ("RED")
                String cleaned = clickedColorName == null ? "" : clickedColorName.trim();

                if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1);
                }

                if (clickedColorName != null) {
                        ColorbrainGame.ColorbrainColor clickedColor = ColorbrainGame.ColorbrainColor
                                        .valueOf(cleaned.toUpperCase());

                        game.handlePlayerClick(player.getId(), clickedColor);

                        broadcastColorbrainUpdate(lobby, duelId, game);
                }
        }

        /**
         * 
         * @param lobby
         * @param duelId
         * @param game
         */
        public void broadcastColorbrainUpdate(Lobby lobby, UUID duelId, ColorbrainGame game) {

                var event = new FrontendColorbrainGameUpdateEvent(
                                duelId,
                                game.getPlayer1(),
                                game.getPlayer2(),
                                game.getPlayer1Pick(),
                                game.getPlayer2Pick(),
                                game.getSelectedColorNames(),
                                game.getWinner(),
                                game.isFinished());

                messaging.sendEvent(new LobbyMessage(lobby, event));
        }
}
