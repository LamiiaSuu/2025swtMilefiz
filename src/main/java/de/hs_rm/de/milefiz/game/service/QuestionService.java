package de.hs_rm.de.milefiz.game.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.hs_rm.de.milefiz.game.model.dto.MinigameQuestionDTO;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.Question;
import de.hs_rm.de.milefiz.game.model.minigames.Quizgame.QuestionsFile;

public class QuestionService {

    private static QuestionService questionService = null;

    private List<MinigameQuestionDTO> questions = new ArrayList<>();

    private Map<Integer, Integer> correctAnswers = new HashMap<>();

    private QuestionService()
            throws StreamReadException, DatabindException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        QuestionsFile nlFile;
        QuestionsFile deFile;
        QuestionsFile enFile;

        InputStream inputStreamEN = getClass().getClassLoader()
                .getResourceAsStream("MinigameQuestions/en.json");
        InputStream inputStreamDE = getClass().getClassLoader()
                .getResourceAsStream("MinigameQuestions/de.json");
        InputStream inputStreamNL = getClass().getClassLoader()
                .getResourceAsStream("MinigameQuestions/nl.json");

        if (inputStreamNL == null || inputStreamDE == null || inputStreamEN == null) {
            throw new IOException("Question file not found");
        }

        try {
            deFile = objectMapper.readValue(inputStreamDE, QuestionsFile.class);
            nlFile = objectMapper.readValue(inputStreamNL, QuestionsFile.class);
            enFile = objectMapper.readValue(inputStreamEN, QuestionsFile.class);

            for (int i = 0; i < deFile.questions().size(); i++) {
                Question questionDE = deFile.questions().get(i);
                Question questionEN = enFile.questions().get(i);
                Question questionNL = nlFile.questions().get(i);

                // wenn die fragen in den Dateien nicht die gleiche Reihenfolge haben
                if (!(questionDE.id() == questionEN.id() && questionEN.id() == questionNL.id())) {
                    questionEN = enFile.questions().stream()
                            .filter(q -> q.id() == questionDE.id())
                            .findFirst()
                            .get();
                    questionNL = nlFile.questions().stream()
                            .filter(q -> q.id() == questionDE.id())
                            .findFirst()
                            .get();
                }

                correctAnswers.put(questionDE.id(), questionDE.correctAnswer());
                MinigameQuestionDTO questionDTO = new MinigameQuestionDTO(questionDE.id());
                questionDTO.setDEQuestion(questionDE.question(), questionDE.answers());
                questionDTO.setENQuestion(questionEN.question(), questionEN.answers());
                questionDTO.setNLQuestion(questionNL.question(), questionNL.answers());

                questions.add(questionDTO);
            }
        } finally {
            inputStreamDE.close();
            inputStreamEN.close();
            inputStreamNL.close();
        }
    }

    public static synchronized QuestionService getQuestionService()
            throws StreamReadException, DatabindException, IOException {
        if (questionService == null) {
            questionService = new QuestionService();
        }
        return questionService;
    }

    public MinigameQuestionDTO randomQuestion() {
        var list = this.questions;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public Optional<MinigameQuestionDTO> randomQuestionExcluding(Set<Integer> excludedIds) {
        var candidates = questions.stream()
                .filter(q -> !excludedIds.contains(q.getId()))
                .toList();
        if (candidates.isEmpty())
            return Optional.empty();
        return Optional.of(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
    }

    public boolean checkAnswer(int questionId, int answerIndex) {
        return correctAnswers.get(questionId) == answerIndex;
    }

    public int getCorrectAnswer(int questionId) {
        return correctAnswers.get(questionId);
    }
}
