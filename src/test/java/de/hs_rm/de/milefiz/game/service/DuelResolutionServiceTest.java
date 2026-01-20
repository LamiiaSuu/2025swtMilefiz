package de.hs_rm.de.milefiz.game.service;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hs_rm.de.milefiz.game.model.*;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;
import de.hs_rm.de.milefiz.messaging.LobbyMessage;

@ExtendWith(MockitoExtension.class)
class DuelResolutionServiceTest {

    @Mock FrontendMessagingService messaging;
    @InjectMocks DuelResolutionService service;

    @Mock Lobby lobby;
    @Mock Duel duel;
    @Mock MiniGame game;
    @Mock Board board;

    @Mock Player p1;
    @Mock Player p2;

    @Mock Meeple m1;
    @Mock Meeple m2;

    @Mock Field start1;
    @Mock Field start2;

    UUID p1Id;
    UUID p2Id;
    UUID m1Id;
    UUID m2Id;

    @BeforeEach
    void setup() {
        p1Id = UUID.randomUUID();
        p2Id = UUID.randomUUID();
        m1Id = UUID.randomUUID();
        m2Id = UUID.randomUUID();

        when(duel.getPlayer1()).thenReturn(p1Id);
        when(duel.getPlayer2()).thenReturn(p2Id);
        when(duel.getFirstMeeple()).thenReturn(m1Id);
        when(duel.getSecondMeeple()).thenReturn(m2Id);

        when(lobby.getPlayer(p1Id)).thenReturn(p1);
        when(lobby.getPlayer(p2Id)).thenReturn(p2);

        when(lobby.getMeepleById(m1Id)).thenReturn(m1);
        when(lobby.getMeepleById(m2Id)).thenReturn(m2);

        when(lobby.getBoard()).thenReturn(board);
    }

    // -------------------------------------------------
    // loser has active meeple -> reset
    // -------------------------------------------------
    @Test
    void loserHasActiveMeeple_activeMeepleIsReset() {
        when(game.getWinner()).thenReturn(p2Id);

        when(p1.getActiveMeeple()).thenReturn(m1);
        when(p1.getColor()).thenReturn(Color.RED);
        when(board.getStartField(Color.RED)).thenReturn(start1);
        when(p1.getRemainingMoves()).thenReturn(2);
        when(p1.hasMoved()).thenReturn(false);

        service.sendLoserHome(lobby, duel, game);

        verify(p1).setRemainingMoves(0);
        verify(p1).setActiveMeeple(null);
        verify(m1).setCurrentField(start1);
        verify(m1).clearLastField();

        verify(messaging, times(1)).sendEvent(any(LobbyMessage.class));
    }

    // -------------------------------------------------
    // draw -> both lose
    // -------------------------------------------------
    @Test
    void draw_bothPlayersLose() {
        when(game.getWinner()).thenReturn(null);

        when(p1.getColor()).thenReturn(Color.RED);
        when(p2.getColor()).thenReturn(Color.GREEN);
        when(board.getStartField(Color.RED)).thenReturn(start1);
        when(board.getStartField(Color.GREEN)).thenReturn(start2);

        when(p1.getRemainingMoves()).thenReturn(1);
        when(p2.getRemainingMoves()).thenReturn(2);
        when(p1.hasMoved()).thenReturn(false);
        when(p2.hasMoved()).thenReturn(false);

        service.sendLoserHome(lobby, duel, game);

        verify(m1).setCurrentField(start1);
        verify(m2).setCurrentField(start2);
        verify(m1).clearLastField();
        verify(m2).clearLastField();

        verify(messaging, times(2)).sendEvent(any(LobbyMessage.class));
    }

    // -------------------------------------------------
    // loser without active meeple -> no reset
    // -------------------------------------------------
    @Test
    void loserWithoutActiveMeeple_doesNotResetMoves() {
        when(game.getWinner()).thenReturn(p2Id);

        when(p1.getActiveMeeple()).thenReturn(null);
        when(p1.getColor()).thenReturn(Color.RED);
        when(board.getStartField(Color.RED)).thenReturn(start1);
        when(p1.getRemainingMoves()).thenReturn(2);
        when(p1.hasMoved()).thenReturn(false);

        service.sendLoserHome(lobby, duel, game);

        verify(p1, never()).setRemainingMoves(0);
        verify(p1, never()).setActiveMeeple(any());
        verify(m1).setCurrentField(start1);

        verify(messaging, times(1)).sendEvent(any(LobbyMessage.class));
    }
}
