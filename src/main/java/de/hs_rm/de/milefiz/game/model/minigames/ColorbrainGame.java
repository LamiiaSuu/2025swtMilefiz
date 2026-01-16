package de.hs_rm.de.milefiz.game.model.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendColorbrainGameUpdateEvent;

public class ColorbrainGame extends MiniGame {

    private UUID player1; // Spieler 1
    private UUID player2; // Spieler 2

    private ColorbrainColor player1Pick;
    private ColorbrainColor player2Pick;

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
     * - Beiden Spieler, die am Duell teilnehmen anhand ihrer eindeutigen Spieler
     * Ids,
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

    }

    /**
     * Wird aufgerufen, wenn der Timeout abläuft.
     * Falls das Spiel noch nicht beendet ist, verlieren beide Spieler.
     */
    @Override
    public void forceMissingActions() {
        if (!isFinished()) {
            setWinner(null); // Beide verlieren
            setFinished(true);
            notifyFinished(); // Triggert Callback in DuelService
        }
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
     * Speichert beim ersten Klick eines Spielers die geklickte Farbe.
     * 
     * @param player       Id des klickenden Spielers
     * @param clickedColor Farbe, die der Spieler anklickt
     */
    public synchronized void handlePlayerClick(UUID player, ColorbrainColor clickedColor) {
        if (player.equals(player1) && player1Pick == null) {
            player1Pick = clickedColor;
        } else if (player.equals(player2) && player2Pick == null) {
            player2Pick = clickedColor;
        } else {
            return;
        }

        checkWinCondition();
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
        if (getWinner() == null) {
            boolean player1Correct = isCorrectColor(player1Pick);
            boolean player2Correct = isCorrectColor(player2Pick);

            if (player1Correct && !player2Correct) { // Spieler 1 richtig
                setWinner(player1);
            } else if (!player1Correct && player2Correct) { // Spieler 2 richtig
                setWinner(player2);
            } else { // beide falsch
                setWinner(null);
            }

            setFinished(true);
            notifyFinished();
        }
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
