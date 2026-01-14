package de.hs_rm.de.milefiz.game.lobby;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Meeple;
import de.hs_rm.de.milefiz.game.model.Player;

public class LobbyTest {

    Lobby lobby;
    Player blue = new Player(Color.BLUE);
    Player red = new Player(Color.RED);
    Player green = new Player(Color.GREEN);
    Player yellow = new Player(Color.YELLOW);
    Player glogomir = new Player(Color.BLUE);

    @Test
    void testAddPlayers() throws LobbyNotFoundException, LobbyJoinException {
        assertTrue(lobby.join(blue));
        assertTrue(lobby.join(red));
        assertTrue(lobby.join(green));
        assertTrue(lobby.join(yellow));
    }

    @Test
    void testLobbyIsFull() throws LobbyNotFoundException, LobbyJoinException {
        assertTrue(lobby.join(blue));
        assertTrue(lobby.join(red));
        assertTrue(lobby.join(green));
        assertTrue(lobby.join(yellow));
        assertThrows(LobbyJoinException.class, () -> lobby.join(yellow));
    }

    @Test
    void testAvailableColorInLobby() throws LobbyJoinException {
        for (int j = 0; j < Color.values().length; j++) {
            assertTrue(lobby.isJoinable());
            assertTrue(lobby.join(new Player(lobby.getAvailableColor())));
        }
        assertFalse(lobby.isJoinable());
        assertThrows(LobbyJoinException.class, () -> lobby.join(new Player(lobby.getAvailableColor())));
    }

    @BeforeEach
    void resetLobby() {
        lobby = new Lobby();
    }

    @Test
    void leaderLeaves_existingLeader_isTransferred() throws LobbyJoinException {
        lobby.join(blue);
        lobby.join(red);

        blue.setLeader(true);

        lobby.leave(blue);

        assertTrue(red.isLeader());
        assertEquals(red, lobby.getLeader());
    }

    @Test
    void leave_withoutLeader_doesNotAssignLeader() throws LobbyJoinException {
        lobby.join(blue);
        lobby.join(red);

        lobby.leave(blue);

        assertNull(lobby.getLeader());
    }


    @Test
    void leave_removesPlayer() throws LobbyJoinException {
        lobby.join(blue);

        assertFalse(lobby.isEmpty());

        lobby.leave(blue);

        assertTrue(lobby.isEmpty());
    }


    @Test
    void join_notAllowed_whenGameStarted() {
        lobby.setGameStarted(true);

        assertThrows(LobbyJoinException.class,
                () -> lobby.join(blue));
    }


    @Test
    void getPlayer_returnsCorrectPlayer() throws LobbyJoinException {
        lobby.join(blue);

        Player found = lobby.getPlayer(blue.getId());

        assertEquals(blue, found);
    }

    @Test
    void getPlayer_unknownId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> lobby.getPlayer(UUID.randomUUID()));
    }


    @Test
    void getMeepleById_returnsCorrectMeeple() throws LobbyJoinException {
        lobby.join(blue);

        Meeple meeple = blue.getMeeples()[0];
        Meeple found = lobby.getMeepleById(meeple.getId());

        assertEquals(meeple, found);
    }

    @Test
    void getMeepleById_unknownMeeple_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> lobby.getMeepleById(UUID.randomUUID()));
    }


    @Test
    void setBoard_null_doesNotCrash() {
        assertDoesNotThrow(() -> lobby.setBoard(null));
    }
}
