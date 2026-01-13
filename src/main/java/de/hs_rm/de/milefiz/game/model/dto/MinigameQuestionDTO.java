package de.hs_rm.de.milefiz.game.model.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinigameQuestionDTO {
    private int id;
    private Map<String, String> question = new HashMap<>();
    private Map<String, List<String>> answers = new HashMap<>();

    public MinigameQuestionDTO(int id) {
        this.id = id;
    }

    public Map<String, String> getQuestion() {
        return question;
    }

    public Map<String, List<String>> getAnswers() {
        return answers;
    }

    public void setDEQuestion(String question, List<String> answers) {
        setQuestion(question, answers, "de");
    }

    public void setENQuestion(String question, List<String> answers) {
        setQuestion(question, answers, "en");
    }

    public void setNLQuestion(String question, List<String> answers) {
        setQuestion(question, answers, "nl");
    }

    private void setQuestion(String question, List<String> answers, String langCode) {
        if (answers.size() != 4) {
            throw new IllegalArgumentException("Es müssen exakt 4 Antwortmöglichkeiten angegeben werden.");
        }

        this.question.put(langCode, question);
        this.answers.put(langCode, answers);
    }

    public int getId() {
        return id;
    }

}
