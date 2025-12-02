// GameServiceTest.java
package de.hs_rm.de.milefiz.game.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;


// Mockito erstellt echten GameService mit Fake-Abhängigkeiten:
// - diceService: Mock der immer das zurückgibt was wir mit when().thenReturn() definieren
// - publisher: Mock der alle publishEvent() Aufrufe aufzeichnet für spätere verify() Prüfungen
// GameService wird isoliert getestet ohne echte DiceService/Publisher Implementierungen
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private DiceServiceImpl diceService; // Mock wird automatisch erstellt

    @Mock
    private ApplicationEventPublisher publisher; // Mock wird automatisch erstellt

    private GameService gameService;

    @BeforeEach
    void setUp() throws StreamReadException, DatabindException, IOException {
        gameService = new GameServiceImpl(diceService, publisher);

    }

    @Test
    @DisplayName("Sollte Exception vom DiceService weitersleiten ohne eigene Exception-Behandlung")
    void rollDice_shouldPropagateExceptionFromDiceService() {
        // Arrange: DiceService wirft Exception
        RuntimeException expectedException = new RuntimeException("Würfel ist kaputt");
        when(diceService.roll()).thenThrow(expectedException);

        // Act & Assert: Exception wird weitergeleitet
        RuntimeException actualException = assertThrows(RuntimeException.class, () -> {
            gameService.rollDice();
        });

        assertEquals("Würfel ist kaputt", actualException.getMessage());
        verify(diceService, times(1)).roll();
    }

    @Test
    @DisplayName("Sollte unverändert das Ergebnis von DiceService zurückgeben ohne Manipulation")
    void rollDice_shouldReturnUnmodifiedDiceResult() {

        int[] testValues = { 1, 2, 3, 4, 5, 6, 100, -5 }; // Auch ungültige Werte testen

        for (int testValue : testValues) {
            // Arrange
            reset(diceService);
            when(diceService.roll()).thenReturn(testValue);

            // Act
            int result = gameService.rollDice();

            // Assert: Exakt das gleiche Ergebnis (keine Manipulation)
            assertEquals(testValue, result, "GameService sollte Wert unverändert zurückgeben");
        }
    }
}