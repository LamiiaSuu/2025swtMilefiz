package de.hs_rm.de.milefiz.game.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

/**
 * Service zur Verwaltung der Wortliste für das MonkeyType-Minigame.
 *
 * <p>
 * Dieser Service lädt beim Start der Anwendung eine Liste von Wörtern aus
 * einer JSON-Datei im Klassenpfad und stellt zufällige Wörter für das
 * {@code MonkeyTypeGame} bereit.
 * </p>
 *
 * <h2>Datenquelle</h2>
 * <ul>
 * <li>Pfad: {@code classpath:minigame/monkeyType/words.json}</li>
 * <li>Erwartetes Format:
 * 
 * <pre>
 *     {
 *       "words": ["WORT1", "WORT2", "WORT3"]
 *     }
 * </pre>
 * 
 * </li>
 * </ul>
 *
 * <h2>Fallback-Verhalten</h2>
 * <ul>
 * <li>Ist das JSON ungültig oder fehlt das Feld {@code words},
 * wird eine Default-Liste gesetzt.</li>
 * <li>Tritt ein {@link IOException} auf, wird eine Fehlerliste verwendet.</li>
 * <li>Ist die Wortliste leer oder {@code null}, liefert
 * {@link #getRandomWord()}
 * einen Platzhalter-String.</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * <p>
 * Die Wortliste wird einmalig beim Application-Startup initialisiert
 * ({@link PostConstruct}) und danach nur lesend verwendet. Die zufällige
 * Auswahl erfolgt über {@link ThreadLocalRandom} und ist thread-sicher.
 * </p>
 */
@Service
public class MonkeyTypeWordService {

    /** Enthält alle verfügbaren Wörter für das MonkeyType-Minigame. */
    private List<String> words;

    /**
     * Initialisiert die Wortliste nach dem Start des Spring-Kontexts.
     *
     * <p>
     * Lädt die Datei {@code words.json} aus dem Klassenpfad, liest das
     * {@code words}-Array und konvertiert es in eine {@link List} von
     * {@link String}s.
     * </p>
     *
     * <p>
     * Fallbacks:
     * </p>
     * <ul>
     * <li>Fehlendes oder ungültiges {@code words}-Array → Standardwörter</li>
     * <li>IOException beim Lesen → Fehlerwörter</li>
     * </ul>
     */
    @PostConstruct
    public void init() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            InputStream inputStream = new ClassPathResource("minigame/monkeyType/words.json").getInputStream();

            var jsonNode = objectMapper.readTree(inputStream);

            var wordsArray = jsonNode.get("words");

            if (wordsArray != null && wordsArray.isArray()) {
                words = objectMapper.convertValue(wordsArray,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } else {
                // Fallback bei fehlendem oder ungültigem JSON-Feld
                words = List.of("MILEFIZ", "TOASTER", "WANDERPOKAL");
            }
            inputStream.close();
        } catch (IOException e) {
            words = List.of("FEHLER", "ERROR");
        }
    }

    /**
     * Liefert ein zufälliges Wort aus der geladenen Wortliste.
     *
     * @return ein zufälliges Wort oder ein Platzhalter-String,
     *         falls keine gültige Wortliste verfügbar ist
     */
    public String getRandomWord() {
        if (words == null || words.isEmpty()) {
            return "404 Wort nicht gefunden.";
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(words.size());
        String word = words.get(randomIndex);
        return word;
    }
}