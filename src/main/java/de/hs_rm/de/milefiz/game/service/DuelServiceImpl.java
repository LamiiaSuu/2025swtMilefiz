package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.minigames.BalloonGame;
import de.hs_rm.de.milefiz.game.model.minigames.ColorbrainGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.QuizGame;
import de.hs_rm.de.milefiz.game.model.minigames.EinarmigerBanditGame;
import de.hs_rm.de.milefiz.game.model.minigames.RockPaperScissorsGame;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendBalloonGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendColorbrainGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendDiceGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendEinarmigerBanditGameUpdateEvent;
import jakarta.annotation.PreDestroy;
import de.hs_rm.de.milefiz.messaging.events.FrontendQuizGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendRockPaperScissorsGameUpdateEvent;

@Service
public class DuelServiceImpl implements DuelService {

    private final ScheduledExecutorService miniGameScheduler = Executors.newScheduledThreadPool(4);

    /**
     * Registry möglicher Mini-Spiele (Factory-Ansatz, damit immer neue Instanzen
     * entstehen).
     */
    private final List<Supplier<MiniGame>> gameFactories = new ArrayList<>();

    /**
     * Zufallsquelle für Spielauswahl.
     */
    private final Random random = new Random();

    /**
     * In-Memory-Speicher aller aktuell laufenden Duelle.
     * <p>
     * Key: Duel-ID<br>
     * Value: Duel (inkl. zugewiesenem Mini-Game)
     */
    private final Map<UUID, Duel> duels = new ConcurrentHashMap<>();

    private final LobbyManager lobbyManager;
    private final FrontendMessagingService messaging;
    private final DuelResolutionService duelResolutionService;

    /**
     * Die Timeouts aus den Spring application properties werden hier
     * gesammelt und gesetzt.
     */
    @Value("${minigame.dicegame.timeout}")
    private int diceGameTimeout;

    /**
     * Timeout für BalloonGame aus application.properties.
     */
    @Value("${minigame.balloongame.timeout}")
    private int balloonGameTimeout;

    @Value("${minigame.quiz.timeout}")
    private int quizGameTimeout;

    @Value("${minigame.einarmigerBanditGame.timeout}")
    private int einarmigerBanditGameTimeout;

    @Value("${minigame.rock.paper.scissors.timeout}")
    private int rockPaperScissorsGameTimeout;

    // Timeout fuer ColorbrainGame
    @Value("${minigame.colorbrain.timeout}")
    private int colorbrainGameTimeout;

    public DuelServiceImpl(LobbyManager lobbyManager, FrontendMessagingService messaging,
           
            DuelResolutionService duelResolutionService) {
        gameFactories.add(() -> new DiceGame(diceGameTimeout + 1));
        gameFactories.add(() -> new BalloonGame(balloonGameTimeout));
        gameFactories.add(() -> new EinarmigerBanditGame(einarmigerBanditGameTimeout + 1)); 
        gameFactories.add(() -> new ColorbrainGame(colorbrainGameTimeout + 1));
        gameFactories.add(() -> new QuizGame(quizGameTimeout));
        gameFactories.add(() -> new RockPaperScissorsGame(rockPaperScissorsGameTimeout + 1));
        this.lobbyManager = lobbyManager;
        this.messaging = messaging;
        this.duelResolutionService = duelResolutionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiniGame randomGame() {
        if (gameFactories.isEmpty()) {
            throw new IllegalStateException("No mini games registered.");
        }

        int index = random.nextInt(gameFactories.size());

        return gameFactories.get(index).get(); // immer neue Instanz
    }

    @Override
    public boolean isMeepleInDuel(UUID meepleId) {
        return duels.values().stream()
                // nur Duelle berücksichtigen, die noch ein aktives Mini-Game haben
                .filter(duel -> duel.getMiniGame() != null && !duel.getMiniGame().isFinished())
                .anyMatch(duel -> duel.getFirstMeeple().equals(meepleId)
                        || duel.getSecondMeeple().equals(meepleId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MiniGame> getGames() {
        return gameFactories.stream()
                .map(Supplier::get)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiniGame assignRandomGameToDuel(UUID duelId) {
        Duel duel = duels.get(duelId);

        if (duel == null) {
            throw new IllegalStateException("Duel not found: " + duelId);
        }

        MiniGame game = randomGame();
        duel.setMiniGame(game);

        game.setOnFinished(() -> handleMiniGameFinished(duel));

        miniGameScheduler.schedule(
                game::forceMissingActions,
                game.getTimeOut(),
                TimeUnit.SECONDS);

        return game;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiniGame getMiniGame(UUID duelId) {
        Duel duel = duels.get(duelId);

        if (duel == null) {
            throw new IllegalStateException("Duel not found: " + duelId);
        }

        return duel.getMiniGame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duel createDuel(UUID p1, UUID p2, UUID m1, UUID m2) {
        UUID duelId = UUID.randomUUID();

        Duel duel = new Duel(duelId, p1, p2, m1, m2);
        duels.put(duelId, duel);

        return duel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duel getDuel(UUID duelId) {
        var duel = duels.get(duelId);

        if (duel == null) {
            throw new IllegalArgumentException(
                    "Duel with id " + duelId + " not found");
        }

        return duel;
    }

    public void initColorBrain(Duel duel, Lobby lobby, ColorbrainGame game) {
        var event = new FrontendColorbrainGameUpdateEvent(
                duel.getId(),
                game.getPlayer1(),
                game.getPlayer2(),
                game.getPlayer1Pick(),
                game.getPlayer2Pick(),
                game.getSelectedColorNames(), // hier sind die Farben
                null, // noch kein Gewinner
                false // noch nicht fertig
        );

        messaging.sendEvent(new LobbyMessage(lobby, event));
    }

    /**
     * Wird automatisch aufgerufen, wenn ein Mini-Game beendet ist.
     * <p>
     * Diese Methode:
     * <ul>
     * <li>sendet das finale Update-Event an alle Clients</li>
     * <li>setzt Verlierer-Meeples zurück zur Startposition</li>
     * </ul>
     *
     * <p>
     * Unterstützte Mini-Games:
     * <ul>
     * <li>{@link DiceGame} - Würfelspiel</li>
     * <li>{@link BalloonGame} - Ballon-Klickspiel</li>
     * </ul>
     *
     * <p>
     * Der Callback wird durch {@link MiniGame#setOnFinished(Runnable)} registriert
     * und automatisch bei Spielende (Timeout oder Gewinner) ausgelöst.
     *
     * @param duel das beendete Duell
     */
    private void handleMiniGameFinished(Duel duel) {

        MiniGame game = duel.getMiniGame();
        Lobby lobby = lobbyManager.getLobbyFromPlayerUUID(duel.getPlayer1());

        if (game instanceof DiceGame dice) {
            var update = new FrontendDiceGameUpdateEvent(
                    duel.getId(),
                    dice.getP1(),
                    dice.getP2(),
                    dice.getRollP1(),
                    dice.getRollP2(),
                    dice.getWinner(),
                    dice.isFinished());

            messaging.sendEvent(new LobbyMessage(lobby, update));
            duelResolutionService.sendLoserHome(lobby, duel, dice);

        }

        else if (game instanceof BalloonGame balloon) {
            var update = new FrontendBalloonGameUpdateEvent(
                    duel.getId(),
                    balloon.getPlayer1(),
                    balloon.getPlayer2(),
                    balloon.getPhasePlayer1(),
                    balloon.getPhasePlayer2(),
                    balloon.getWinner(),
                    balloon.isFinished());

            messaging.sendEvent(new LobbyMessage(lobby, update));
            duelResolutionService.sendLoserHome(lobby, duel, balloon);
        }

        else if (game instanceof ColorbrainGame colorbrainGame) {
            var update = new FrontendColorbrainGameUpdateEvent(
                    duel.getId(),
                    colorbrainGame.getPlayer1(),
                    colorbrainGame.getPlayer2(),
                    colorbrainGame.getPlayer1Pick(),
                    colorbrainGame.getPlayer2Pick(),
                    colorbrainGame.getSelectedColorNames(),
                    colorbrainGame.getWinner(),
                    colorbrainGame.isFinished());

            messaging.sendEvent(new LobbyMessage(lobby, update));
            duelResolutionService.sendLoserHome(lobby, duel, colorbrainGame);

        }

        else if (game instanceof EinarmigerBanditGame einarmigerBandit) {
            Integer energy = null;
            if (game.getWinner() != null) {
                energy = lobby.getPlayer(game.getWinner()).getEnergy();
            }
            var update = new FrontendEinarmigerBanditGameUpdateEvent(
                    duel.getId(),
                    einarmigerBandit.getP1(),
                    einarmigerBandit.getP2(),
                    einarmigerBandit.getResultP1(),
                    einarmigerBandit.getResultP2(),
                    einarmigerBandit.getResultComp(),
                    einarmigerBandit.getWinner(),
                    einarmigerBandit.isJackpot(),
                    energy != null ? energy : 0,
                    einarmigerBandit.isFinished());

            messaging.sendEvent(new LobbyMessage(lobby, update));
            duelResolutionService.sendLoserHome(lobby, duel, einarmigerBandit);

        }

        if (game instanceof QuizGame quiz) {
            var update = new FrontendQuizGameUpdateEvent(duel.getId(), quiz.getPlayer1(), quiz.getPlayer2(),
                    quiz.getQuestionDTO(), quiz.getWinner(),
                    quiz.isFinished());
            messaging.sendEvent(new LobbyMessage(lobby, update));
            duelResolutionService.sendLoserHome(lobby, duel, quiz);
        } else if (game instanceof RockPaperScissorsGame rockPaperScissorsGame) {
            var update = new FrontendRockPaperScissorsGameUpdateEvent(
                    duel.getId(),
                    rockPaperScissorsGame.getP1(),
                    rockPaperScissorsGame.getP2(),
                    rockPaperScissorsGame.getMoveP1(),
                    rockPaperScissorsGame.getMoveP2(),
                    rockPaperScissorsGame.getWinner(),
                    rockPaperScissorsGame.isFinished());
            messaging.sendEvent(new LobbyMessage(lobby, update));
            duelResolutionService.sendLoserHome(lobby, duel, rockPaperScissorsGame);

        }
        duels.remove(duel.getId());
    }

    @PreDestroy
    public void shutdownScheduler() {
        miniGameScheduler.shutdownNow();
    }
}
