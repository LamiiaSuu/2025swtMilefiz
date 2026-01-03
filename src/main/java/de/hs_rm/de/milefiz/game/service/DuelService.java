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
     * Prüft, ob ein bestimmter Meeple aktuell in einem Duell beteiligt ist.
     *
     * <p>
     * Diese Methode wird verwendet, um Spielzüge zu verhindern, bei denen
     * ein Meeple auf ein Feld ziehen möchte, auf dem bereits ein Meeple steht,
     * der sich momentan in einem aktiven Duell befindet.
     * </p>
     *
     * <p>
     * Hintergrund:
     * <ul>
     *     <li>Ein Meeple darf nicht in mehrere Duelle gleichzeitig verwickelt sein; ein Spieler schon.</li>
     *     <li>Andere Spieler dürfen nicht auf Meeples interagieren, die aktuell
     *         durch ein Duell "blockiert" sind.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Typische Verwendung:
     * <ul>
     *     <li>Validierung im Bewegungs- / Duell-Trigger-Code</li>
     *     <li>Anzeige im Frontend (z. B. "Warte, Duell läuft")</li>
     * </ul>
     * </p>
     *
     * @param meepleId
     *        die eindeutige ID des Meeples, der geprüft werden soll
     *
     * @return {@code true}, wenn der Meeple aktuell in einem aktiven Duell
     *         registriert ist, andernfalls {@code false}
     */
    boolean isMeepleInDuel(UUID meepleId);

    /**
     * Erstellt ein neues Duell zwischen zwei Spielern und registriert es.
     *
     * @param player1 erster Spieler
     * @param player2 zweiter Spieler
     * @param meeple1 Meeple des ersten Spielers
     * @param meeple2 Meeple des zweiten Spielers
     * @return das neu erstellte {@link Duel}
     */
    Duel createDuel(UUID player1, UUID player2, UUID meeple1, UUID meeple2);

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
