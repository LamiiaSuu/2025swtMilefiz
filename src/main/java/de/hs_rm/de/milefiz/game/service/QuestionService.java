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

/**
 * Service zur Verwaltung und Bereitstellung von Quiz-Fragen.
 *
 * <p>
 * Liest beim ersten Zugriff die Fragebögen aus JSON-Ressourcen (de,en,nl),
 * erstellt interne DTOs und speichert die Indizes der korrekten Antworten.
 * Die Klasse folgt dem Singleton-Pattern und stellt die Daten für die
 * Mini-Games bereit.
 * </p>
 */
public class QuestionService {

    /** Singleton-Instanz. */
    private static QuestionService questionService = null;

    /** Liste aller geladenen Fragen als DTOs. */
    private List<MinigameQuestionDTO> questions = new ArrayList<>();

    /** Map von Frage-ID -> Index der korrekten Antwort. */
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

    /**
     * Liefert die Singleton-Instanz des {@code QuestionService}.
     *
     * <p>
     * Beim ersten Aufruf werden die Frage-Dateien geladen und die internen
     * Strukturen initialisiert. Die Methode ist synchronisiert, um die
     * sichere Initialisierung in Mehrthread-Umgebungen zu gewährleisten.
     * </p>
     *
     * @return die initialisierte {@code QuestionService}-Instanz
     * @throws StreamReadException bei JSON-Lesefehlern
     * @throws DatabindException   bei Mapping-Fehlern
     * @throws IOException         bei I/O-Problemen (z. B. fehlende Ressourcen)
     */
    public static synchronized QuestionService getQuestionService()
            throws StreamReadException, DatabindException, IOException {
        if (questionService == null) {
            questionService = new QuestionService();
        }
        return questionService;
    }

    /**
     * Liefert eine zufällige Frage aus dem internen Fragen-Pool.
     *
     * @return zufälliges {@link MinigameQuestionDTO}
     */
    public MinigameQuestionDTO randomQuestion() {
        var list = this.questions;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Liefert eine zufällige Frage aus dem Pool, schließt dabei jedoch eine
     * Menge von Frage-IDs aus.
     *
     * @param excludedIds Menge von Frage-IDs, die nicht berücksichtigt werden
     * @return Optional mit einer ausgewählten Frage oder {@code Optional.empty()},
     *         falls keine passende Frage existiert
     */
    public Optional<MinigameQuestionDTO> randomQuestionExcluding(Set<Integer> excludedIds) {
        var candidates = questions.stream()
                .filter(q -> !excludedIds.contains(q.getId()))
                .toList();
        if (candidates.isEmpty())
            return Optional.empty();
        return Optional.of(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
    }

    /**
     * Prüft, ob die übergebene Antwort für die Frage mit der angegebenen
     * ID korrekt ist.
     *
     * @param questionId  ID der Frage
     * @param answerIndex Index der Antwort (0-basierter Index)
     * @return {@code true}, wenn die Antwort korrekt ist, sonst {@code false}
     */
    public boolean checkAnswer(int questionId, int answerIndex) {
        return correctAnswers.get(questionId) == answerIndex;
    }

    /**
     * Liefert den Index der korrekten Antwort für eine Frage.
     *
     * @param questionId ID der Frage
     * @return Index der korrekten Antwort (0-basiert)
     */
    public int getCorrectAnswer(int questionId) {
        return correctAnswers.get(questionId);
    }
}
