package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class ColorbrainGame extends MiniGame {

    
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    private boolean timeoutStarted = false;

    private UUID player1;
    private UUID player2;
    
    public ColorbrainGame(int timeOut) {
        super(1, "Colorbrain-Spiel", timeOut);
    }
    
    public void initPlayers(UUID player1, UUID player2) {

    }



    
}
