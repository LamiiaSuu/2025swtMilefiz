package de.hs_rm.de.milefiz.game.lobby;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Lobby;
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
}
