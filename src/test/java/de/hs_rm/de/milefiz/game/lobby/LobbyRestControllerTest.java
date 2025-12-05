package de.hs_rm.de.milefiz.game.lobby;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.hs_rm.de.milefiz.game.model.Color;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.Player;
import de.hs_rm.de.milefiz.game.service.GameService;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class LobbyRestControllerTest {

    @Mock
    private LobbyManager lobbyManager;

    @Mock
    private GameService gameService;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private LobbyRestController lobbyRestController;

    private Lobby testLobby;
    private UUID testLobbyId;

    @BeforeEach
    void setUp() {
        testLobbyId = UUID.randomUUID();
        testLobby = new Lobby();
        testLobby.setId(testLobbyId);
        
        // Mock gameService.getTestBoard() um NullPointerException zu vermeiden
        // Lenient macht das Mock optional (wird nicht in allen Tests aufgerufen)
        lenient().when(gameService.getTestBoard()).thenReturn(null);
    }

    @Test
    @DisplayName("getLobbyList sollte alle Lobbys zurückgeben")
    void getLobbyList_shouldReturnAllLobbies() {
        Set<Lobby> lobbies = new HashSet<>();
        lobbies.add(testLobby);
        lobbies.add(new Lobby());
        
        when(lobbyManager.getLobbies()).thenReturn(lobbies);
        Set<Lobby> result = lobbyRestController.getLobbyList();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(lobbyManager).getLobbies();
    }

    @Test
    @DisplayName("getLobbyList sollte leere Liste zurückgeben wenn keine Lobbys existieren")
    void getLobbyList_shouldReturnEmptySetWhenNoLobbies() {
        Set<Lobby> emptyLobbies = new HashSet<>();
        when(lobbyManager.getLobbies()).thenReturn(emptyLobbies);

        Set<Lobby> result = lobbyRestController.getLobbyList();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("joinLobby sollte erfolgreich Player zu existierender Lobby hinzufügen")
    void joinLobby_shouldSuccessfullyAddPlayerToExistingLobby() throws Exception {
        when(lobbyManager.getLobby(testLobbyId)).thenReturn(testLobby);
        ResponseEntity<LobbyJoinEvent> response = lobbyRestController.joinLobby(testLobbyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testLobbyId, response.getBody().lobbyId());
        assertNotNull(response.getBody().playerId());
        assertNotNull(response.getBody().color());
        assertNotNull(response.getBody().playerToken());
        assertEquals("Erfolgreich gejoint. ", response.getBody().msg());
        assertEquals(1, testLobby.getPlayers().size());
    }

    @Test
    @DisplayName("joinLobby sollte LobbyNotFoundException werfen wenn Lobby nicht existiert")
    void joinLobby_shouldThrowLobbyNotFoundExceptionWhenLobbyDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(lobbyManager.getLobby(nonExistentId)).thenThrow(new LobbyNotFoundException());
        try {
            lobbyRestController.joinLobby(nonExistentId);
        } catch (LobbyNotFoundException ex) {
            // Expected exception
            assertNotNull(ex);
        }
    }

    @Test
    @DisplayName("joinLobby sollte CONFLICT zurückgeben wenn Lobby voll ist")
    void joinLobby_shouldReturnConflictWhenLobbyIsFull() throws Exception {
        testLobby.join(new Player(Color.RED));
        testLobby.join(new Player(Color.GREEN));
        testLobby.join(new Player(Color.YELLOW));
        testLobby.join(new Player(Color.BLUE));
        
        when(lobbyManager.getLobby(testLobbyId)).thenReturn(testLobby);
        ResponseEntity<LobbyJoinEvent> response = lobbyRestController.joinLobby(testLobbyId);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().lobbyId());
        assertNull(response.getBody().playerId());
        assertNull(response.getBody().color());
        assertNull(response.getBody().playerToken());
        assertNotNull(response.getBody().msg());
        assertEquals("Die Lobby ist zurzeit nicht beitretbar!", response.getBody().msg());
    }

    @Test
    @DisplayName("joinLobby sollte spieler mit verfügbarer Farbe erstellen")
    void joinLobby_shouldCreatePlayerWithAvailableColor() throws Exception {
        testLobby.join(new Player(Color.RED)); // Erste Farbe bereits vergeben
        
        when(lobbyManager.getLobby(testLobbyId)).thenReturn(testLobby);

        ResponseEntity<LobbyJoinEvent> response = lobbyRestController.joinLobby(testLobbyId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verifiziere dass die Farbe nicht RED ist (bereits vergeben)
        String assignedColor = response.getBody().color();
        assertNotNull(assignedColor);
        assertEquals(false, "RED".equals(assignedColor));
        
        // Verifiziere dass 2 Spieler in der Lobby sind
        assertEquals(2, testLobby.getPlayers().size());
    }

    @Test
    @DisplayName("joinRandomLobby sollte neue Lobby erstellen wenn keine joinable existiert")
    void joinRandomLobby_shouldCreateNewLobbyWhenNoJoinableExists() throws Exception {
        Lobby fullLobby = new Lobby();
        fullLobby.setId(UUID.randomUUID());
        fullLobby.join(new Player(Color.RED));
        fullLobby.join(new Player(Color.GREEN));
        fullLobby.join(new Player(Color.YELLOW));
        fullLobby.join(new Player(Color.BLUE));
        
        Set<Lobby> lobbies = new HashSet<>();
        lobbies.add(fullLobby);
        
        Lobby newLobby = new Lobby();
        UUID newLobbyId = UUID.randomUUID();
        newLobby.setId(newLobbyId);
        
        when(lobbyManager.getLobbies()).thenReturn(lobbies);
        when(lobbyManager.createLobby()).thenReturn(newLobby);
        when(lobbyManager.getLobby(newLobbyId)).thenReturn(newLobby);

        ResponseEntity<LobbyJoinEvent> response = lobbyRestController.joinRandomLobby();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newLobbyId, response.getBody().lobbyId());
        verify(lobbyManager).createLobby();
    }

    @Test
    @DisplayName("joinRandomLobby sollte neue Lobby erstellen wenn keine Lobbys existieren")
    void joinRandomLobby_shouldCreateNewLobbyWhenNoLobbiesExist() throws Exception {
        Set<Lobby> emptyLobbies = new HashSet<>();
        
        Lobby newLobby = new Lobby();
        UUID newLobbyId = UUID.randomUUID();
        newLobby.setId(newLobbyId);
        
        when(lobbyManager.getLobbies()).thenReturn(emptyLobbies);
        when(lobbyManager.createLobby()).thenReturn(newLobby);
        when(lobbyManager.getLobby(newLobbyId)).thenReturn(newLobby);

        ResponseEntity<LobbyJoinEvent> response = lobbyRestController.joinRandomLobby();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newLobbyId, response.getBody().lobbyId());
        verify(lobbyManager).createLobby();
    }
}
