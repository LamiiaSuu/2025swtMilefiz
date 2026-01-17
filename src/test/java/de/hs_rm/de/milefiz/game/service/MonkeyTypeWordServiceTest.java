package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MonkeyTypeWordServiceTest {

    @Test
    void getRandomWord_returns404IfWordsNull() {
        MonkeyTypeWordService service = new MonkeyTypeWordService();

        // words absichtlich NICHT initialisiert
        String word = service.getRandomWord();

        assertEquals("404 Wort nicht gefunden.", word);
    }

    @Test
    void getRandomWord_returns404IfWordsEmpty() {
        MonkeyTypeWordService service = new MonkeyTypeWordService();

        ReflectionTestUtils.setField(service, "words", List.of());

        String word = service.getRandomWord();

        assertEquals("404 Wort nicht gefunden.", word);
    }

    @Test
    void getRandomWord_returnsWordFromList() {
        MonkeyTypeWordService service = new MonkeyTypeWordService();

        List<String> words = List.of("AAA", "BBB", "CCC");
        ReflectionTestUtils.setField(service, "words", words);

        String random = service.getRandomWord();

        assertTrue(words.contains(random));
    }

    @Test
    void getRandomWord_whenOnlyOneWordAlwaysReturnsThatWord() {
        MonkeyTypeWordService service = new MonkeyTypeWordService();

        ReflectionTestUtils.setField(service, "words", List.of("ONLY"));

        for (int i = 0; i < 20; i++) {
            assertEquals("ONLY", service.getRandomWord());
        }
    }

    @Test
    void init_loadsWordsList_orFallsBack() {
        MonkeyTypeWordService service = new MonkeyTypeWordService();

        // ruft Datei-Laden auf (falls words.json existiert)
        service.init();

        String word = service.getRandomWord();

        // egal ob Datei geladen oder fallback -> darf nicht 404 sein
        assertNotNull(word);
        assertNotEquals("404 Wort nicht gefunden.", word);
        assertFalse(word.isBlank());
    }
}
