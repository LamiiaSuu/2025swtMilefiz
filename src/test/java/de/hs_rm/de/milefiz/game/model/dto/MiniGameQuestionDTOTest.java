package de.hs_rm.de.milefiz.game.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class MinigameQuestionDTOTest {

    @Test
    void constructor_setsId() {
        MinigameQuestionDTO dto = new MinigameQuestionDTO(42);

        assertEquals(42, dto.getId());
        assertNotNull(dto.getQuestion());
        assertNotNull(dto.getAnswers());
    }

    @Test
    void setDEQuestion_storesQuestionAndAnswersUnderDe() {
        MinigameQuestionDTO dto = new MinigameQuestionDTO(1);

        dto.setDEQuestion("Wie heißt du?", List.of("A", "B", "C", "D"));

        assertEquals("Wie heißt du?", dto.getQuestion().get("de"));
        assertEquals(List.of("A", "B", "C", "D"), dto.getAnswers().get("de"));
    }

    @Test
    void setENQuestion_storesQuestionAndAnswersUnderEn() {
        MinigameQuestionDTO dto = new MinigameQuestionDTO(2);

        dto.setENQuestion("What is this?", List.of("A1", "B1", "C1", "D1"));

        assertEquals("What is this?", dto.getQuestion().get("en"));
        assertEquals(List.of("A1", "B1", "C1", "D1"), dto.getAnswers().get("en"));
    }

    @Test
    void setNLQuestion_storesQuestionAndAnswersUnderNl() {
        MinigameQuestionDTO dto = new MinigameQuestionDTO(3);

        dto.setNLQuestion("Wat is dit?", List.of("A2", "B2", "C2", "D2"));

        assertEquals("Wat is dit?", dto.getQuestion().get("nl"));
        assertEquals(List.of("A2", "B2", "C2", "D2"), dto.getAnswers().get("nl"));
    }

    @Test
    void setQuestion_withNotFourAnswers_throwsIllegalArgumentException() {
        MinigameQuestionDTO dto = new MinigameQuestionDTO(4);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> dto.setDEQuestion("Test", List.of("A", "B", "C")));

        assertTrue(ex.getMessage().contains("exakt 4"));
    }

    @Test
    void canStoreMultipleLanguagesIndependently() {
        MinigameQuestionDTO dto = new MinigameQuestionDTO(5);

        dto.setDEQuestion("DE Frage", List.of("A", "B", "C", "D"));
        dto.setENQuestion("EN Question", List.of("A1", "B1", "C1", "D1"));

        assertEquals("DE Frage", dto.getQuestion().get("de"));
        assertEquals("EN Question", dto.getQuestion().get("en"));

        assertEquals(List.of("A", "B", "C", "D"), dto.getAnswers().get("de"));
        assertEquals(List.of("A1", "B1", "C1", "D1"), dto.getAnswers().get("en"));
    }
}
