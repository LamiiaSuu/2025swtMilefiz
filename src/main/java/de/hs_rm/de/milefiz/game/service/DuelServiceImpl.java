package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.DummyGame;
import org.springframework.stereotype.Service;

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

    public DuelServiceImpl() {
        gameFactories.add(DiceGame::new);
        //gameFactories.add(() -> new DummyGame(2, "Dummy Game #2"));
        //gameFactories.add(() -> new DummyGame(3, "Dummy Game #3"));
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
}
