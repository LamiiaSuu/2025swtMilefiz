package de.hs_rm.de.milefiz.game.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class MonkeyTypeWordService {
    private List<String> words;

    @PostConstruct
    public void init(){
        try{
            ObjectMapper objectMapper = new ObjectMapper();

            InputStream inputStream = new ClassPathResource("minigame/monkeyType/words.json").getInputStream();
            
            var jsonNode = objectMapper.readTree(inputStream);
            
            var wordsArray = jsonNode.get("words");
            
            if(wordsArray != null && wordsArray.isArray()) {
                words = objectMapper.convertValue(wordsArray, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));  
            } else {
                words = List.of("MILEFIZ", "TOASTER", "WANDERPOKAL");
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            words = List.of("FEHLER", "ERROR");
        }
    }

    public String getRandomWord() {
        if (words == null || words.isEmpty()) {
            return "404 Wort nicht gefunden.";
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(words.size());
        String word = words.get(randomIndex);
        return word;
    }
}