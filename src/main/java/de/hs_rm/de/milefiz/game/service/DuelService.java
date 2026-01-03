package de.hs_rm.de.milefiz.game.service;

import java.util.List;
import java.util.UUID;

import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.MiniGame;

/**
 * Zentrale Schnittstelle zur Verwaltung von Duellen und den dazugehörigen Mini-Spielen.
 * <p>
 * Der {@code DuelService} ist verantwortlich für:
 * <ul>
 *     <li>Erstellen und Verwalten aktiver Duelle</li>
 *     <li>Zuweisen eines zufälligen Mini-Spiels zu einem Duell</li>
 *     <li>Bereitstellen der verfügbaren Mini-Spiele</li>
 *     <li>Zurückgeben des aktuell zugewiesenen Mini-Spiels eines Duells</li>
 * </ul>
 */
public interface DuelService {

    /**
     * Wählt ein zufälliges Mini-Spiel aus der aktuellen Spieleliste aus.
     * <p>
     * Die Methode verwendet eine Zufallsauswahl.
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
     * Gibt die aktuell verfügbaren Mini-Spiele zurück.
     * <p>
     * Die zurückgegebene Liste repräsentiert den aktuellen Zustand der
     * Mini-Game-Registry und kann u. a. dafür genutzt werden:
     * <ul>
     *   <li>Debugging / Logging</li>
     *   <li>UI-Darstellung (z. B. verfügbare Mini-Spiele anzeigen)</li>
     *   <li>Tests (z. B. prüfen, ob Dummy-Spiele korrekt registriert sind)</li>
     * </ul>
     *
     * @return eine Lesesicht auf die registrierten {@link MiniGame}-Instanzen.
     *         Änderungen an der zurückgegebenen Liste wirken sich nicht auf die interne Verwaltung aus.
     */
    List<MiniGame> getGames();

    /**
     * Erstellt ein neues Duell zwischen zwei Spielern und registriert es.
     *
     * @param player1 erster Spieler
     * @param player2 zweiter Spieler
     * @return das neu erstellte {@link Duel}
     */
    Duel createDuel(UUID player1, UUID player2);

    /**
     * Weist einem bestehenden Duell ein zufälliges Mini-Spiel zu.
     * <p>
     * Das gewählte Mini-Spiel bleibt während des gesamten Duells bestehen.
     *
     * @param duelId die ID des Duells
     * @return das zugewiesene {@link MiniGame}
     *
     * @throws IllegalStateException wenn das Duell nicht existiert
     */
    MiniGame assignRandomGameToDuel(UUID duelId);

    /**
     * Liefert das aktuell einem Duell zugewiesene Mini-Spiel zurück.
     *
     * @param duelId die ID des Duells
     * @return das Mini-Spiel dieses Duells
     *
     * @throws IllegalStateException wenn das Duell nicht existiert
     */
    MiniGame getMiniGame(UUID duelId);
}
