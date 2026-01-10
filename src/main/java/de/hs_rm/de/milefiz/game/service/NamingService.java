package de.hs_rm.de.milefiz.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NamingService {
    private static final int MAX_LENGTH = 16;

    private static final List<String> ADJECTIVES = List.of(
        "Wilder",
        "Kühner",
        "Listiger",
        "Flinker",
        "Stiller",
        "Fieser",
        "Mutiger",
        "Dreister",
        "Tückischer",
        "Kluger",
        "Tapferer",
        "Zäher",
        "Rasender",
        "Finsterer",
        "Frecher",
        "Eifriger",
        "Starker",
        "Rauher",
        "Flotter",
        "Böser",
        "Magischer",
        "Dunkler",
        "Blutiger",
        "Gieriger",
        "Tollkühner",
        "Gesegneter"
    );


    private static final List<String> NOUNS = List.of(
        "Meeple",
        "Held",
        "Würfel",
        "Goblin",
        "Spieler",
        "Dieb",
        "Krieger",
        "Magier",
        "Fuchs",
        "Barde",
        "Ritter",
        "Narr",
        "Schurke",
        "Drache",
        "Ork",
        "Elf",
        "Zwerg",
        "Hexer",
        "Mönch",
        "Paladin",
        "Söldner",
        "Jäger",
        "Assassine",
        "Wächter",
        "Beschwörer"
    );


    private static final Random RANDOM = new Random();

    private NamingService() {}

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
}
