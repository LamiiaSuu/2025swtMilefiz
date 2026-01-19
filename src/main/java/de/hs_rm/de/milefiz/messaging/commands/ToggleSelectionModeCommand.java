package de.hs_rm.de.milefiz.messaging.commands;

/**
 * Command-Objekt zum Umschalten des Auswahlmodus für Minispiele.
 *
 * <p>
 * Dieses Command wird vom Frontend an den Spielserver gesendet,
 * um festzulegen, wie das nächste Minispiel ausgewählt werden soll.
 * </p>
 *
 * <p>
 * Bedeutung des Flags:
 * <ul>
 * <li>{@code true} → Minispiele werden zufällig ausgewählt (RANDOM)</li>
 * <li>{@code false} → Minispiele werden in fester Reihenfolge ausgewählt
 * (IN_ORDER)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Die eigentliche Umschaltung der Logik erfolgt im {@code DuelService}.
 * Dieses Record dient ausschließlich als Transportobjekt (DTO).
 * </p>
 *
 * @param selectRandomMinigame
 *                             Gibt an, ob die Minigame-Auswahl zufällig
 *                             erfolgen soll
 */
public record ToggleSelectionModeCommand(boolean selectRandomMinigame) {

}
