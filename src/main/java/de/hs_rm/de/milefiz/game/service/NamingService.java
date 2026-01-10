package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service-Klasse zur Erzeugung zufälliger Namen
 * für Spieler und Lobbys.
 *
 * <p>
 * Ein Name besteht jeweils aus einem Adjektiv und einem Nomen,
 * getrennt durch ein Leerzeichen. Alle erzeugten Namen sind garantiert
 * maximal {@value #MAX_LENGTH} Zeichen lang (inklusive Leerzeichen).
 * </p>
 *
 * <p>
 * Die Klasse stellt zwei getrennte Generatoren bereit:
 * </p>
 * <ul>
 *   <li>{@link #generateRandomName()} für Spielernamen</li>
 *   <li>{@link #generateRandomLobbyName()} für Lobby- bzw. Kartennamen</li>
 * </ul>
 *
 * <p>
 * Diese Klasse ist als Utility-Klasse konzipiert und darf nicht
 * instanziiert werden.
 * </p>
 */
public class NamingService {
    /**
     * Maximale erlaubte Länge eines generierten Namens
     * inklusive Leerzeichen.
     */
    private static final int MAX_LENGTH = 16;

    /**
     * Adjektive zur Erzeugung von Spielernamen.
     *
     * <p>
     * Die Adjektive sind thematisch auf Fantasy- und Videospiel-Kontexte
     * abgestimmt und können mit allen Einträgen aus {@link #NOUNS}
     * kombiniert werden.
     * </p>
     */
    private static final List<String> ADJECTIVES = List.of(
        "Wild",
        "Bold",
        "Cunning",
        "Swift",
        "Silent",
        "Vile",
        "Brave",
        "Brazen",
        "Devious",
        "Clever",
        "Valiant",
        "Tough",
        "Frenzied",
        "Grim",
        "Cheeky",
        "Eager",
        "Strong",
        "Rough",
        "Nimble",
        "Evil",
        "Arcane",
        "Dark",
        "Bloody",
        "Greedy",
        "Daring",
        "Blessed"
    );

    /**
     * Nomen zur Erzeugung von Spielernamen.
     *
     * <p>
     * Die Nomen repräsentieren Rollen, Kreaturen oder Archetypen,
     * die typisch für Brettspiele, Fantasy- oder Videospiele sind.
     * </p>
     */
    private static final List<String> NOUNS = List.of(
        "Meeple",
        "Hero",
        "Goblin",
        "Player",
        "Thief",
        "Warrior",
        "Mage",
        "Fox",
        "Bard",
        "Knight",
        "Jester",
        "Rogue",
        "Dragon",
        "Orc",
        "Elf",
        "Dwarf",
        "Hexer",
        "Monk",
        "Paladin",
        "Hunter",
        "Assassin",
        "Warden",
        "Summoner"
    );

    /**
     * Adjektive zur Erzeugung von Lobby- oder Kartennamen.
     *
     * <p>
     * Diese Adjektive beschreiben Umgebungen, Orte oder Stimmungen
     * und sind speziell für Lobbys oder Spielkarten vorgesehen.
     * </p>
     */
    private static final List<String> LOBBY_ADJECTIVES = List.of(
        "Wild",
        "Dark",
        "Lost",
        "Ancient",
        "Cursed",
        "Sacred",
        "Hidden",
        "Silent",
        "Bloody",
        "Feral",
        "Magic",
        "Grim",
        "Rough",
        "Forsaken",
        "Shadow"
    );

    /**
     * Nomen zur Erzeugung von Lobby- oder Kartennamen.
     *
     * <p>
     * Die Nomen stehen für Orte, Bauwerke oder Biome,
     * die typischerweise als Spielumgebungen dienen.
     * </p>
     */
    private static final List<String> LOBBY_NOUNS = List.of(
        "Jungle",
        "Forest",
        "Ruins",
        "Temple",
        "Dungeon",
        "Tower",
        "Cave",
        "Swamp",
        "Abyss",
        "Valley",
        "Labyrinth",
        "Shrine",
        "Fortress",
        "Pit",
        "Path"
    );

    /**
     * Zufallszahlengenerator zur Auswahl eines gültigen Namens.
     */
    private static final Random RANDOM = new Random();

    /**
     * Privater Konstruktor zur Verhinderung der Instanziierung
     * dieser Utility-Klasse.
     */
    private NamingService() {}

    /**
     * Erzeugt einen zufälligen Spielernamen.
     *
     * <p>
     * Der Name setzt sich aus einem Adjektiv aus {@link #ADJECTIVES}
     * und einem Nomen aus {@link #NOUNS} zusammen. Es werden ausschließlich
     * Kombinationen berücksichtigt, deren Gesamtlänge
     * {@value #MAX_LENGTH} Zeichen nicht überschreitet.
     * </p>
     *
     * @return ein zufällig generierter Spielername oder {@code "Spieler"},
     *         falls keine gültige Kombination existiert
     */
    public static String generateRandomName() {
        List<String> validNames = new ArrayList<>();

        for (String adj : ADJECTIVES) {
            for (String noun : NOUNS) {
                String name = adj + " " + noun;
                if (name.length() <= MAX_LENGTH) {
                    validNames.add(name);
                }
            }
        }

        return validNames.isEmpty()
                ? "Spieler"
                : validNames.get(RANDOM.nextInt(validNames.size()));
    }

    /**
     * Erzeugt einen zufälligen Lobby- oder Kartennamen.
     *
     * <p>
     * Der Name setzt sich aus einem Adjektiv aus {@link #LOBBY_ADJECTIVES}
     * und einem Nomen aus {@link #LOBBY_NOUNS} zusammen. Es werden ausschließlich
     * Kombinationen berücksichtigt, deren Gesamtlänge
     * {@value #MAX_LENGTH} Zeichen nicht überschreitet.
     * </p>
     *
     * @return ein zufällig generierter Lobbyname oder {@code "Lobby"},
     *         falls keine gültige Kombination existiert
     */
    public static String generateRandomLobbyName() {
        List<String> validNames = new ArrayList<>();

        for (String adj : LOBBY_ADJECTIVES) {
            for (String noun : LOBBY_NOUNS) {
                String name = adj + " " + noun;
                if (name.length() <= MAX_LENGTH) {
                    validNames.add(name);
                }
            }
        }

        return validNames.isEmpty()
                ? "Lobby"
                : validNames.get(RANDOM.nextInt(validNames.size()));
    }

}
