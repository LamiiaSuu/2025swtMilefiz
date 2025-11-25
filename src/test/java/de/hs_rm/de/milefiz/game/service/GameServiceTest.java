// GameServiceTest.java
package de.hs_rm.de.milefiz.game.service;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.Mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;
import de.hs_rm.de.milefiz.messaging.events.FrontendRollDiceEvent;

// Mockito erstellt echten GameService mit Fake-Abhängigkeiten:
// - diceService: Mock der immer das zurückgibt was wir mit when().thenReturn() definieren
// - publisher: Mock der alle publishEvent() Aufrufe aufzeichnet für spätere verify() Prüfungen
// GameService wird isoliert getestet ohne echte DiceService/Publisher Implementierungen
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private DiceServiceImpl diceService;        //  Mock wird automatisch erstellt
    
    @Mock 
    private ApplicationEventPublisher publisher;  //  Mock wird automatisch erstellt
    
    private GameService gameService;
    
    @BeforeEach
    void setUp() {
        gameService = new GameService(diceService, publisher);
    }
    
    @Test
    @DisplayName("Sollte DiceService.roll() aufrufen, Ergebnis in FrontendRollDiceEvent verpacken und über Publisher an Lobby senden")
    void rollDice_shouldRollAndPublishEvent() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Lobby lobby = new Lobby();
        
        when(diceService.roll()).thenReturn(4);
        
        // Act
        gameService.rollDice(lobby, playerId);
        
        // Assert
        verify(diceService, times(1)).roll();  //  DiceService prüfen
        
        //  Nur EIN verify für Publisher mit ArgumentCaptor (fängt die Parameter ab, die eigentlich als Übergabeparameter genutzt werden, um diese zu überprüfen)
        ArgumentCaptor<LobbyMessage> captor = ArgumentCaptor.forClass(LobbyMessage.class);
        verify(publisher, times(1)).publishEvent(captor.capture());
        
        // Event-Inhalt prüfen
        LobbyMessage publishedMessage = captor.getValue();
        assertEquals(lobby, publishedMessage.lobby());
        
        FrontendRollDiceEvent event = (FrontendRollDiceEvent) publishedMessage.event();
        assertEquals(playerId, event.playerId());
        assertEquals(4, event.number());
    }
    
    @Test
    @DisplayName("Sollte alle gültigen Würfelzahlen 1-6 vom DiceService entgegennehmen und korrekt im Event weitergeben")
    void rollDice_shouldHandleDifferentNumbers() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Lobby lobby = new Lobby();
        
        int[] testNumbers = {1, 2, 3, 4, 5, 6};
        
        for (int number : testNumbers) {
            // Arrange
            reset(diceService, publisher);
            when(diceService.roll()).thenReturn(number);
            
            // Act
            gameService.rollDice(lobby, playerId);
            
            // Assert
            ArgumentCaptor<LobbyMessage> captor = ArgumentCaptor.forClass(LobbyMessage.class);
            verify(publisher).publishEvent(captor.capture());
            
            FrontendRollDiceEvent event = (FrontendRollDiceEvent) captor.getValue().event();
            assertEquals(number, event.number(), "Should roll " + number);
        }
    }
    
    @Test
    @DisplayName("Sollte für verschiedene Spieler-UUIDs jeweils separate Events mit korrekter playerId erstellen")
    void rollDice_shouldWorkWithDifferentPlayers() {
        // Arrange
        Lobby lobby = new Lobby();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        when(diceService.roll()).thenReturn(5).thenReturn(2);  //  Mehrere Rückgabewerte
        
        // Act
        gameService.rollDice(lobby, player1);
        gameService.rollDice(lobby, player2);
        
        // Assert
        verify(diceService, times(2)).roll();  //  Zwei mal aufgerufen
        
        ArgumentCaptor<LobbyMessage> captor = ArgumentCaptor.forClass(LobbyMessage.class);
        verify(publisher, times(2)).publishEvent(captor.capture());
        
        List<LobbyMessage> messages = captor.getAllValues();  //  Alle aufgefangenen Werte
        
        FrontendRollDiceEvent event1 = (FrontendRollDiceEvent) messages.get(0).event();
        FrontendRollDiceEvent event2 = (FrontendRollDiceEvent) messages.get(1).event();
        
        assertEquals(player1, event1.playerId());
        assertEquals(5, event1.number());
        
        assertEquals(player2, event2.playerId());
        assertEquals(2, event2.number());
    }
    
    @Test
    @DisplayName("Sollte RuntimeException vom DiceService weiterleiten und dabei KEIN Event publishen (Rollback-Verhalten)")
    void rollDice_whenDiceServiceThrowsException_shouldNotPublishEvent() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Lobby lobby = new Lobby();
        
        when(diceService.roll()).thenThrow(new RuntimeException("Dice broken"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            gameService.rollDice(lobby, playerId);
        });
        
        verify(diceService).roll();                    //  roll() wurde aufgerufen
        verifyNoInteractions(publisher);               //  publisher wurde NICHT aufgerufen
    }
    
    @Test
    @DisplayName("Sollte DiceService.roll() nicht in Schleife oder mehrfach pro rollDice() Aufruf verwenden")
    void rollDice_shouldNotCallDiceServiceMultipleTimes() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Lobby lobby = new Lobby();
        
        when(diceService.roll()).thenReturn(3);
        
        // Act
        gameService.rollDice(lobby, playerId);
        
        // Assert
        verify(diceService, times(1)).roll();          //  Genau einmal
        verifyNoMoreInteractions(diceService);         //  Keine weiteren Aufrufe
    }
    
    @Test
    @DisplayName("Sollte LobbyMessage mit FrontendRollDiceEvent (nicht anderer Event-Typ) an Publisher senden")
    void rollDice_shouldPublishCorrectEventType() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Lobby lobby = new Lobby();
        
        when(diceService.roll()).thenReturn(6);
        
        // Act
        gameService.rollDice(lobby, playerId);
        
        // Assert - Spezifischer Event-Typ prüfen
        // Prüft jeden Parameter der an publishEvent() übergeben wird
        verify(publisher).publishEvent(argThat((Object event) -> {
            if (!(event instanceof LobbyMessage)) return false;
            
            LobbyMessage msg = (LobbyMessage) event;
            return msg.event() instanceof FrontendRollDiceEvent;
        }));
    }
}