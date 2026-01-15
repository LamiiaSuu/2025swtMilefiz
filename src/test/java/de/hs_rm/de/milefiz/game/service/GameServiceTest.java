// GameServiceTest.java
package de.hs_rm.de.milefiz.game.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import de.hs_rm.de.milefiz.game.model.Board;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.messaging.commands.MoveBarrierCommand;
import de.hs_rm.de.milefiz.messaging.commands.MovementCommand;
import de.hs_rm.de.milefiz.messaging.events.FrontendEvent;


// Mockito erstellt echten GameService mit Fake-Abhängigkeiten:
// - diceService: Mock der immer das zurückgibt was wir mit when().thenReturn() definieren
// - publisher: Mock der alle publishEvent() Aufrufe aufzeichnet für spätere verify() Prüfungen
// GameService wird isoliert getestet ohne echte DiceService/Publisher Implementierungen
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private DiceServiceImpl diceService; // Mock wird automatisch erstellt

    @Mock
    private CooldownServiceImpl cooldownService; // Mock wird automatisch erstellt

    @Mock
    private ApplicationEventPublisher publisher; // Mock wird automatisch erstellt

    @Mock
    private MovementServiceImpl movementService;

    private PlantingService plantingService;

    private GameService gameService;

    @BeforeEach

    void setUp() throws StreamReadException, DatabindException, IOException {
        plantingService = new PlantingServiceImpl();
        gameService = new GameServiceImpl(diceService, publisher, cooldownService, movementService, plantingService);

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

    @Test
    @DisplayName("getRollDiceCooldown delegiert an CooldownService")
    void getRollDiceCooldown_delegatesToCooldownService() {
        UUID playerId = UUID.randomUUID();
        when(cooldownService.getCooldown(playerId)).thenReturn(5);

        int result = gameService.getRollDiceCooldown(playerId);

        assertEquals(5, result);
        verify(cooldownService).getCooldown(playerId);
    }

    @Test
    @DisplayName("addRollDiceCooldown delegiert an CooldownService")
    void addRollDiceCooldown_delegatesToCooldownService() {
        UUID playerId = UUID.randomUUID();

        gameService.addRollDiceCooldown(playerId);

        verify(cooldownService).addCooldown(playerId);
    }

    @Test
    @DisplayName("moveMeeple delegiert an MovementService")
    void moveMeeple_delegatesToMovementService() {
        UUID lobbyId = UUID.randomUUID();
        MovementCommand cmd = mock(MovementCommand.class);
        Player player = mock(Player.class);
        FrontendEvent event = mock(FrontendEvent.class);

        when(movementService.moveMeeple(lobbyId, cmd, player))
                .thenReturn(event);

        FrontendEvent result = gameService.moveMeeple(lobbyId, cmd, player);

        assertSame(event, result);
        verify(movementService).moveMeeple(lobbyId, cmd, player);
    }

    @Test
    @DisplayName("moveBarrier delegiert an MovementService")
    void moveBarrier_delegatesToMovementService() {
        UUID lobbyId = UUID.randomUUID();
        MoveBarrierCommand cmd = mock(MoveBarrierCommand.class);
        Player player = mock(Player.class);
        FrontendEvent event = mock(FrontendEvent.class);

        when(movementService.moveBarrier(lobbyId, cmd, player))
                .thenReturn(event);

        FrontendEvent result = gameService.moveBarrier(lobbyId, cmd, player);

        assertSame(event, result);
        verify(movementService).moveBarrier(lobbyId, cmd, player);
    }

}