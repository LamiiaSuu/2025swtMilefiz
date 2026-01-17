package de.hs_rm.de.milefiz.game.model.minigames.monkeyTypeGame;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.service.MonkeyTypeWordService;

public class MonkeyTypeGame extends MiniGame {

    private static final Logger logger = LoggerFactory.getLogger(MonkeyTypeGame.class);

    private final MonkeyTypeWordService wordsService;

    private UUID player1;
    private UUID player2;

    private String targetWord;
    private String player1Input = "";
    private String player2Input = "";

    private boolean[] correctLettersPlayer1;
    private boolean[] correctLettersPlayer2;

    private ScheduledExecutorService scheduler;




    public MonkeyTypeGame(int timeOut, MonkeyTypeWordService wordsService) {
        super(8, "Monkey Type Game", timeOut);
        this.wordsService = wordsService;
    }

    public void initPlayers(UUID p1, UUID p2, Lobby lobby) {
        logger.info("Inititializing Monkey Type game for players {} and {}", p1, p2);

        player1 = p1;
        player2 = p2;
        this.targetWord = wordsService.getRandomWord();

        correctLettersPlayer1 = new boolean[targetWord.length()];
        correctLettersPlayer2 = new boolean[targetWord.length()];

        player1Input = "";
        player2Input = "";

        startTimer();

    }

    public void processInput(UUID playerId, char typedChar, int position){
        if (isFinished()) return;

        if (position < 0 || position >= targetWord.length()) return;

        if (playerId.equals(player1)) {
            if (position != player1Input.length()){
                return;
            }

            boolean correct = typedChar == targetWord.charAt(position);
            player1Input += typedChar;

            if (correctLettersPlayer1 != null && position < correctLettersPlayer1.length) {
                correctLettersPlayer1[position] = correct;
            }

            if (player1Input.equals(targetWord)){
                setWinner(player1);
                setFinished(true);
                notifyFinished();
            }
        } else {
            if (position != player2Input.length()){
                return;
            }

            boolean correct = typedChar == targetWord.charAt(position);
            player2Input += typedChar;

            if (correctLettersPlayer2 != null && position < correctLettersPlayer2.length) {
                correctLettersPlayer2[position] = correct;
            }

            if (player2Input.equals(targetWord)){
                setWinner(player2);
                setFinished(true);
                notifyFinished();
            }
        }
    }


    private void startTimer(){
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            if(!isFinished()){
                setFinished(true);
                setWinner(null);
                notifyFinished();
            }
        }, getTimeOut(), TimeUnit.SECONDS);
    }

    @Override
    public void forceMissingActions() {
        if(!isFinished()) {
            setFinished(true);
            setWinner(null);
            notifyFinished();
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public void setPlayer1(UUID player1) {
        this.player1 = player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public void setPlayer2(UUID player2) {
        this.player2 = player2;
    }

    public String getTargetWord() {
        return targetWord;
    }

    public void setTargetWord(String targetWord) {
        this.targetWord = targetWord;
    }

    public String getPlayer1Input() {
        return player1Input;
    }

    public void setPlayer1Input(String player1Input) {
        this.player1Input = player1Input;
    }

    public String getPlayer2Input() {
        return player2Input;
    }

    public void setPlayer2Input(String player2Input) {
        this.player2Input = player2Input;
    }

    public boolean[] getCorrectLettersPlayer1() {
        return correctLettersPlayer1;
    }

    public void setCorrectLettersPlayer1(boolean[] correctLettersPlayer1) {
        this.correctLettersPlayer1 = correctLettersPlayer1;
    }

    public boolean[] getCorrectLettersPlayer2() {
        return correctLettersPlayer2;
    }

    public void setCorrectLettersPlayer2(boolean[] correctLettersPlayer2) {
        this.correctLettersPlayer2 = correctLettersPlayer2;
    }


}
