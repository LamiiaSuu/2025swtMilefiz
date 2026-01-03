package de.hs_rm.de.milefiz.game.service;

import java.util.List;

import de.hs_rm.de.milefiz.game.model.MiniGame;

public interface DuelService {

    /**
     * Wählt ein zufälliges Mini-Spiel aus der aktuellen Spieleliste aus.
     * <p>
     * Die Methode verwendet eine Zufallsauswahl
     * <p>
     * Typische Verwendung:
     * <ul>
     *   <li>Auswahl des Mini-Spiels beim Start eines Duells</li>
     *   <li>Testen der Duell-Mechanik mit Dummy-Spielen</li>
     * </ul>
     *
     * @return das zufällig ausgewählte {@link MiniGame}-Objekt.
     *
     * @throws IllegalStateException
     *         wenn keine Mini-Spiele registriert sind und somit keine Auswahl
     *         getroffen werden kann.
     *
     * @implNote
     * Diese Methode verändert die zugrunde liegende Spieleliste nicht.
     * Sie verwendet ausschließlich die aktuellen Einträge.
     */
    MiniGame randomGame();

    /**
     * Gibt die aktuellen Mini-Spiele zurück.
     * <p>
     * Die zurückgegebene Liste repräsentiert den aktuellen Zustand der
     * Mini-Game-Registry und kann u. a. dafür genutzt werden:
     * <ul>
     *   <li>Debugging / Logging</li>
     *   <li>UI-Darstellung (z. B. verfügbare Mini-Spiele anzeigen)</li>
     *   <li>Tests (z. B. prüfen, ob Dummy-Spiele korrekt registriert sind)</li>
     * </ul>
     *
     * @return eine Lesesicht (ist schreibgeschützt, da Copy) auf die registrierten
     *         {@link MiniGame}-Instanzen. Änderungen an der zurückgegebenen
     *         Liste wirken sich nicht auf die interne Verwaltung aus.
     *
     */
    List<MiniGame> getGames();
}

