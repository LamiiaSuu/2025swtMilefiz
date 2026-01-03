package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.game.model.minigames.DummyGame;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class DuelServiceImpl implements DuelService {

    private final List<Supplier<MiniGame>> gameFactories = new ArrayList<>();
    private final Random random = new Random();

    public DuelServiceImpl() {

        gameFactories.add(DiceGame::new);

        gameFactories.add(() -> new DummyGame(2, "Dummy Game #2"));

        gameFactories.add(() -> new DummyGame(3, "Dummy Game #3"));
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

        // Erstellt jedes mal ein neues Objekt, damit die Werte darin gesetzt werden können.
        return gameFactories.get(index).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MiniGame> getGames() {
        // Gibt schreibgeschützte variante zurück.
        return gameFactories.stream().map(Supplier::get).toList();
    }
}
