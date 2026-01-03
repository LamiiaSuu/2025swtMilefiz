package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class DuelServiceImpl implements DuelService {

    private final List<MiniGame> games = new ArrayList<>();
    private final Random random = new Random();

    public DuelServiceImpl() {
        // Initiale Spiele 
        games.add(new MiniGame(1, "Würfel-Spiel"));
        games.add(new MiniGame(2, "Dummy Game #2"));
        games.add(new MiniGame(3, "Dummy Game #3"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiniGame randomGame() {
        if (games.isEmpty()) {
            throw new IllegalStateException("No mini games registered.");
        }

        int index = random.nextInt(games.size());
        return games.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MiniGame> getGames() {
        return List.copyOf(games);
    }
}

