package de.hs_rm.de.milefiz.game.model.dto.minigames;


import java.util.UUID;
/**
 * Datenübertragungsobjekt (DTO) für Fortschrittsmeldungen
 * im MonkeyType-Minigame.
 * <p>
 * Dieses DTO wird vom Client an den Server gesendet, sobald ein Spieler
 * einen weiteren Buchstaben korrekt eingegeben hat.
 * <p>
 * Der {@code progress}-Wert gibt an, wie viele Zeichen des Zielworts
 * aktuell korrekt und zusammenhängend getippt wurden.
 * <p>
 * Die eigentliche Zeichenvalidierung erfolgt clientseitig;
 * der Server prüft lediglich die Fortschrittslogik
 * (z. B. keine Sprünge oder ungültigen Werte) und entscheidet
 * autoritativ über Spielende und Gewinner.
 *
 * @param playerId die eindeutige ID des Spielers
 * @param progress der neue Fortschrittswert (Anzahl korrekt getippter Zeichen)
 */
public record MonkeyTypeProgressDTO(UUID playerId, int progress) {
    
}
