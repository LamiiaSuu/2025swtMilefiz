package de.hs_rm.de.milefiz.game.model.minigames.Quizgame;

import java.util.List;

public record Question(
        int id,
        String question,
        List<String> answers,
        int correctAnswer) {
}
