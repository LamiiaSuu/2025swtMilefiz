package de.hs_rm.de.milefiz.game.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class MonkeyTypeWordService {
    private List<String> words;

    @PostConstruct
    public void init(){
        System.out.println("🟢 MonkeyTypeWordService wird initialisiert...");
        try{
            ObjectMapper objectMapper = new ObjectMapper();

            InputStream inputStream = new ClassPathResource("minigame/monkeyType/words.json").getInputStream();
            System.out.println("🔍 JSON-Datei gefunden: " + inputStream.available() + " bytes");
            
            var jsonNode = objectMapper.readTree(inputStream);
            System.out.println("🔍 JSON parsed: " + jsonNode);
            
            var wordsArray = jsonNode.get("words");
            System.out.println("🔍 words Array gefunden: " + (wordsArray != null));
            
            if(wordsArray != null && wordsArray.isArray()) {
                words = objectMapper.convertValue(wordsArray, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                System.out.println("✅ " + words.size() + " Wörter geladen: " + words);
            } else {
                System.out.println("⚠️ Kein words Array gefunden, verwende Fallback");
                words = List.of("MILEFIZ", "TOASTER", "WANDERPOKAL");
            }
            inputStream.close();
        } catch (IOException e) {
            System.err.println("❌ Fehler beim Laden der Wörter: " + e.getMessage());
            e.printStackTrace();
            words = List.of("FEHLER", "ERROR");
        }
    }

    public String getRandomWord() {
        if (words == null || words.isEmpty()) {
            System.err.println("❌ Keine Wörter geladen!");
            return "404 Wort nicht gefunden.";
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(words.size());
        String word = words.get(randomIndex);
        System.out.println("🔍 Zufälliges Wort ausgewählt: " + word);
        return word;
    }
}