package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendDiceGameUpdateEvent;
import de.hs_rm.de.milefiz.messaging.events.FrontendMoveEvent;

@Service
public class DuelServiceImpl implements DuelService {

    /**
     * Registry möglicher Mini-Spiele (Factory-Ansatz, damit immer neue Instanzen entstehen).
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

    /**
     * Die Timeouts aus den Spring application properties werden hier
     * gesammelt und gesetzt.
     */
    @Value("${minigame.dicegame.timeout}")
    private int diceGameTimeout;

    public DuelServiceImpl(LobbyManager lobbyManager, FrontendMessagingService messaging) {
        gameFactories.add(() -> new DiceGame(diceGameTimeout+1));
        //gameFactories.add(() -> new DummyGame(2, "Dummy Game #2"));
        //gameFactories.add(() -> new DummyGame(3, "Dummy Game #3"));
        this.lobbyManager = lobbyManager;
        this.messaging = messaging;
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
                .anyMatch(duel ->
                        duel.getFirstMeeple().equals(meepleId)
                    || duel.getSecondMeeple().equals(meepleId)
                );
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
                    "Duel with id " + duelId + " not found"
            );
        }

        return duel;
    }

    private void handleMiniGameFinished(Duel duel) {

        MiniGame game = duel.getMiniGame();

        if (!(game instanceof DiceGame dice)) {
            return;
        }

        // Lobby holen
        Lobby lobby = lobbyManager.getLobbyFromPlayerUUID(duel.getPlayer1());

        // Dice Update senden 
        var update = new FrontendDiceGameUpdateEvent(
                duel.getId(),
                dice.getP1(),
                dice.getP2(),
                dice.getRollP1(),
                dice.getRollP2(),
                dice.getWinner(),
                dice.isFinished()
        );

        messaging.sendEvent(new LobbyMessage(lobby, update));

        // Verlierer heimschicken
        sendLoserHome(lobby, duel, dice);
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
    private void sendLoserHome(Lobby lobby, Duel duel, MiniGame game) {

        var winner = game.getWinner();

        var p1 = duel.getPlayer1();
        var p2 = duel.getPlayer2();

        var m1 = lobby.getMeepleById(duel.getFirstMeeple());
        var m2 = lobby.getMeepleById(duel.getSecondMeeple());

        var start1 = lobby.getBoard().getStartField(
                lobby.getPlayer(p1).getColor()
        );

        var start2 = lobby.getBoard().getStartField(
                lobby.getPlayer(p2).getColor()
        );

        if (winner == null || !winner.equals(p1)) {
            lobby.getPlayer(p1).setMoved(false);
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

        if (winner == null || !winner.equals(p2)) {
            lobby.getPlayer(p2).setMoved(false);
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


}
