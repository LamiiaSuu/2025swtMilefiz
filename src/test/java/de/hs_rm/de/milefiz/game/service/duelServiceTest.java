package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;

@ExtendWith(MockitoExtension.class)
class DuelServiceImplTest {

    private LobbyManager lobbyManager;
    private FrontendMessagingService messaging;
    private DuelServiceImpl service;

    @BeforeEach
    void setup() {
        lobbyManager = mock(LobbyManager.class);
        messaging = mock(FrontendMessagingService.class);

        service = new DuelServiceImpl(lobbyManager, messaging);

        // Inject @Value fields manually
        ReflectionTestUtils.setField(service, "diceGameTimeout", 5);
        ReflectionTestUtils.setField(service, "balloonGameTimeout", 5);
        ReflectionTestUtils.setField(service, "einarmigerBanditGameTimeout", 5);
    }

    @Test
    void randomGame_returnsNewInstance() {
        MiniGame g1 = service.randomGame();
        MiniGame g2 = service.randomGame();

        assertNotNull(g1);
        assertNotNull(g2);
        assertNotSame(g1, g2);
    }

    @Test
    void createAndGetDuel_works() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();

        Duel duel = service.createDuel(p1, p2, m1, m2);

        Duel fetched = service.getDuel(duel.getId());

        assertEquals(duel, fetched);
    }

    @Test
    void getDuel_unknownId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getDuel(UUID.randomUUID()));
    }

    @Test
    void assignRandomGameToDuel_setsMiniGame() {
        Duel duel = service.createDuel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());

        MiniGame game = service.assignRandomGameToDuel(duel.getId());

        assertNotNull(game);
        assertEquals(game, duel.getMiniGame());
    }

    @Test
    void getMiniGame_returnsAssignedGame() {
        Duel duel = service.createDuel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());

        MiniGame assigned = service.assignRandomGameToDuel(duel.getId());

        MiniGame fetched = service.getMiniGame(duel.getId());

        assertEquals(assigned, fetched);
    }

    @Test
    void isMeepleInDuel_detectsActiveMeeple() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();

        Duel duel = service.createDuel(p1, p2, m1, m2);

        DiceGame game = new DiceGame(5);
        duel.setMiniGame(game);

        assertTrue(service.isMeepleInDuel(m1));
        assertTrue(service.isMeepleInDuel(m2));
        assertFalse(service.isMeepleInDuel(UUID.randomUUID()));
    }

    @Test
    void getGames_returnsAllMiniGames() {
        var games = service.getGames();

        assertNotNull(games);
        assertEquals(3, games.size());
    }

    @Test
    void assignRandomGameToDuel_unknownDuel_throws() {
        assertThrows(IllegalStateException.class,
            () -> service.assignRandomGameToDuel(UUID.randomUUID()));
    }

    @Test
    void getMiniGame_unknownDuel_throws() {
        assertThrows(IllegalStateException.class,
            () -> service.getMiniGame(UUID.randomUUID()));
    }

    @Test
    void randomGame_noFactories_throws() {
        DuelServiceImpl emptyService =
            new DuelServiceImpl(lobbyManager, messaging);

        ReflectionTestUtils.setField(emptyService, "gameFactories", new ArrayList<>());

        assertThrows(IllegalStateException.class,
            emptyService::randomGame);
    }




}

