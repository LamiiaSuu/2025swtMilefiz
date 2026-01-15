package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public class ColorbrainGame extends MiniGame {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private boolean timeoutStarted = false;

    private UUID player1; // Spieler 1
    private UUID player2; // Spieler 2

    // Speichert welche Farbe beide Spieler jeweils klicken
    private final Map<UUID, ColorbrainColor> clickedColors = new HashMap<>();

    // Speichert zu welcher Zeit beide Spieler klicken
    private final Map<UUID, Long> clickTimestamps = new HashMap<>();

    // Liste aller moeglichen Farben
    public enum ColorbrainColor {
        PINK,
        RED,
        ORANGE,
        YELLOW,
        GREEN,
        BLUE,
        PURPLE,
        BLACK,
    }

    // Liste der 4 zufaelligen und eindeutigen Farben - keine doppelten
    private ColorbrainColor[] selectedColors = new ColorbrainColor[4];

    // Die richtige Farbe, welche die Spieler klicken müssen
    private ColorbrainColor correctColor;

    /**
     * 
     * @param timeOut
     */
    public ColorbrainGame(int timeOut) {
        super(7, "Colorbrain-Spiel", timeOut);
    }

    /**
     * Initialisiert das Minispiel mit:
     * - Beiden Spieler, die am Duell teilnehmen anhand ihrer eindeutigen Spieler Ids,
     * - 4 zufaelligen und eindeutigen Farben,
     * - der richtigen Antwortfarbe.
     * 
     * Startet dabei den Timeout, falls dieser noch nicht gestartet wurde.
     * 
     * @param p1 die eindeutige Id von Spieler 1
     * @param p2 die eindeutige Id von Spieler 2
     */
    public void initGame(UUID p1, UUID p2) {
        this.player1 = p1;
        this.player2 = p2;

        selectFourRandomDifferentColors();
        correctColor = selectedColors[1];

        // Starte den Timeout
        if (!timeoutStarted) {
            timeoutStarted = true;

            scheduler.schedule(this::handleTimeout, getTimeOut(), TimeUnit.SECONDS);
        }
    }

    /**
     * Wird aufgerufen, wenn der Timeout abläuft.
     * Falls das Spiel noch nicht beendet ist, verlieren beide Spieler.
     */
    private void handleTimeout() {
        if (!isFinished()) {
            setWinner(null); // Beide verlieren
            setFinished(true);
            notifyFinished(); // Triggert Callback in DuelService
        }
        scheduler.shutdown();
    }

    /**
     * Erstellt eine Liste aller moeglichen Farben, durchmischt diese zufaellig und
     * waehlt die erste 4 davon fuer das Minispiel.
     */
    private void selectFourRandomDifferentColors() {
        List<ColorbrainColor> colors = new ArrayList<>(Arrays.asList(ColorbrainColor.values()));

        Collections.shuffle(colors);

        for (int i = 0; i < 4; i++) {
            selectedColors[i] = colors.get(i);
        }
    }

    /**
     * Speichert bei ersten Klick eines Spielers die Farbe und den Zeitpunkt.
     * 
     * @param player       Id des klickenden Spielers
     * @param clickedColor Farbe, die der Spieler anklickt
     */
    public synchronized void handlePlayerClick(UUID player, ColorbrainColor clickedColor) {
        if (!clickedColors.containsKey(player)) {
            clickedColors.put(player, clickedColor);
            clickTimestamps.put(player, System.currentTimeMillis());
            checkWinCondition();
        }
    }

    /**
     * Prueft ob es sich bei einer Farbe um die richtige Antwortfarbe handelt.
     * 
     * @param clickedColor Die angeklickte Farbe
     * @return wahr, wenn die geklickte Farbe korrekt ist, false wenn nicht
     */
    private boolean isCorrectColor(ColorbrainColor clickedColor) {
        return clickedColor == correctColor;
    }

    /**
     * 
     */
    private void checkWinCondition() {
        if (clickedColors.size() < 2) {
            return;
        }

        UUID p1 = getPlayer1();
        UUID p2 = getPlayer2();

        ColorbrainColor color1 = clickedColors.get(p1);
        ColorbrainColor color2 = clickedColors.get(p2);

        long time1 = clickTimestamps.get(p1);
        long time2 = clickTimestamps.get(p2);

        boolean p1Correct = isCorrectColor(color1);
        boolean p2Correct = isCorrectColor(color2);

        if (p1Correct && !p2Correct) { // Spieler 1 richtig
            setWinner(p1);
        } else if (!p1Correct && p2Correct) { // Spieler 2 richtig
            setWinner(p2);
        } else if (p1Correct && p2Correct) { // beide richtig
            setWinner(time1 < time2 ? p1 : p2);
        } else { // beide falsch
            setWinner(null);
        }

        setFinished(true);
        notifyFinished();
        scheduler.shutdown();
    }

    /**
     * Gibt die eindeutige Id von Spieler 1 zurueck.
     * 
     * @return die UUID von Spieler 1
     */
    public UUID getPlayer1() {
        return player1;
    }

    /**
     * Gibt die eindeutige Id von Spieler 2 zurueck.
     *
     * @return die UUID von Spieler 2
     */
    public UUID getPlayer2() {
        return player2;
    }

    /**
     * Gibt eine Liste der Namen aller für das Minispiel ausgewaehlten Farben
     * zurueck.
     *
     * @return eine String Liste, die die Namen der ausgewaehlten Farben enthaelt
     */
    public List<String> getSelectedColorNames() {
        return Arrays.stream(selectedColors)
                .map(Enum::name)
                .toList();
    }

}
