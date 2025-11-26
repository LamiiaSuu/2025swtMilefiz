package de.hs_rm.de.milefiz.game.lobby;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;

class LobbyManagerTest {

    private LobbyManager lobbyManager;

    @BeforeEach
    void setUp() {
        lobbyManager = new LobbyManager();
    }

    @Test
    @DisplayName("getLobby sollte LobbyNotFoundException werfen bei nicht existierender UUID")
    void getLobby_shouldThrowLobbyNotFoundException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        assertThrows(LobbyNotFoundException.class, () -> {
            lobbyManager.getLobby(nonExistentId);
        });
    }

    @Test
    @DisplayName("createLobby sollte neue Lobby erstellen und zur Liste hinzufügen")
    void createLobby_shouldCreateAndAddNewLobby() {
        int initialSize = lobbyManager.getLobbies().size();
        Lobby newLobby = lobbyManager.createLobby();

        // Assert
        assertNotNull(newLobby);
        assertNotNull(newLobby.getId());
        assertEquals(initialSize + 1, lobbyManager.getLobbies().size());
        assertTrue(lobbyManager.getLobbies().contains(newLobby));
    }

    @Test
    @DisplayName("deleteLobby sollte existierende Lobby entfernen und true zurückgeben")
    void deleteLobby_shouldRemoveExistingLobby() throws LobbyNotFoundException {
        Lobby lobby = lobbyManager.createLobby();
        int sizeBeforeDelete = lobbyManager.getLobbies().size();
        boolean result = lobbyManager.deleteLobby(lobby);

        // Assert
        assertTrue(result);
        assertEquals(sizeBeforeDelete - 1, lobbyManager.getLobbies().size());
        assertFalse(lobbyManager.getLobbies().contains(lobby));
    }

    @Test
    @DisplayName("deleteLobby sollte false zurückgeben bei nicht existierender Lobby")
    void deleteLobby_shouldReturnFalseForNonExistentLobby() {
        Lobby nonExistentLobby = new Lobby();
        boolean result = lobbyManager.deleteLobby(nonExistentLobby);

        assertFalse(result);
    }

    @Test
    @DisplayName("getPlayerByTokenFromLobbies sollte Spieler mit übereinstimmendem Token finden")
    void getPlayerByTokenFromLobbies_shouldReturnPlayerWithMatchingToken() throws Exception {
        Lobby lobby = mock(Lobby.class);
        Player player = mock(Player.class);
        String testToken = "test-token-123";
        
        when(player.getPlayerToken()).thenReturn(testToken);
        List<Player> players = new ArrayList<>();
        players.add(player);
        when(lobby.getPlayers()).thenReturn(players);
        
        lobbyManager.getLobbies().add(lobby);

        Player foundPlayer = lobbyManager.getPlayerByTokenFromLobbies(testToken);

        assertNotNull(foundPlayer);
        assertEquals(testToken, foundPlayer.getPlayerToken());
    }

    @Test
    @DisplayName("getPlayerByTokenFromLobbies sollte PlayerNotFoundException werfen bei nicht existierendem Token")
    void getPlayerByTokenFromLobbies_shouldThrowPlayerNotFoundException() {
        String nonExistentToken = "non-existent-token";
        PlayerNotFoundException exception = assertThrows(PlayerNotFoundException.class, () -> {
            lobbyManager.getPlayerByTokenFromLobbies(nonExistentToken);
        });
        
        assertTrue(exception.getMessage().contains(nonExistentToken));
    }

    @Test
    @DisplayName("getLobbyFromPlayer sollte null zurückgeben wenn Spieler in keiner Lobby ist")
    void getLobbyFromPlayer_shouldReturnNullWhenPlayerNotInAnyLobby() {
        Player player = mock(Player.class);
        Lobby foundLobby = lobbyManager.getLobbyFromPlayer(player);

        assertNull(foundLobby);
    }
}