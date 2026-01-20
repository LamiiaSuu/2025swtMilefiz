package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.model.Duel;
import de.hs_rm.de.milefiz.game.model.Lobby;
import de.hs_rm.de.milefiz.game.model.MiniGame;
import de.hs_rm.de.milefiz.game.model.minigames.DiceGame;
import de.hs_rm.de.milefiz.messaging.FrontendMessagingService;

@ExtendWith(MockitoExtension.class)
class DuelServiceImplTest {

    private LobbyManager lobbyManager;
    private FrontendMessagingService messaging;
    private DuelServiceImpl service;
    private DuelResolutionService duelResolutionService;
    private MonkeyTypeWordService monkeyTypeWordService;

    @BeforeEach
    void setup() {
        lobbyManager = mock(LobbyManager.class);
        messaging = mock(FrontendMessagingService.class);
        duelResolutionService = mock(DuelResolutionService.class);
        monkeyTypeWordService = mock(MonkeyTypeWordService.class);

        service = new DuelServiceImpl(lobbyManager, messaging, duelResolutionService, monkeyTypeWordService);

        ReflectionTestUtils.setField(service, "diceGameTimeout", 5);
        ReflectionTestUtils.setField(service, "balloonGameTimeout", 5);
        ReflectionTestUtils.setField(service, "slotMachineGameTimeout", 5);
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
    void assignGameToDuel_setsMiniGame() {
        Duel duel = service.createDuel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());

        MiniGame game = service.assignGameToDuel(duel.getId());

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

        MiniGame assigned = service.assignGameToDuel(duel.getId());

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
        assertEquals(7, games.size());
    }

    @Test
    void assignGameToDuel_unknownDuel_throws() {
        assertThrows(IllegalStateException.class,
                () -> service.assignGameToDuel(UUID.randomUUID()));
    }

    @Test
    void getMiniGame_unknownDuel_throws() {
        assertThrows(IllegalStateException.class,
                () -> service.getMiniGame(UUID.randomUUID()));
    }

    @Test
    void randomGame_noFactories_throws() {
        DuelServiceImpl emptyService = new DuelServiceImpl(lobbyManager, messaging, duelResolutionService, monkeyTypeWordService);

        ReflectionTestUtils.setField(emptyService, "gameFactories", new ArrayList<>());

        assertThrows(IllegalStateException.class,
                emptyService::randomGame);
    }

    @Test
    void isMeepleInDuel_finishedGame_isIgnored() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();

        Duel duel = service.createDuel(p1, p2, m1, m2);

        DiceGame game = mock(DiceGame.class);
        when(game.isFinished()).thenReturn(true);
        duel.setMiniGame(game);

        assertFalse(service.isMeepleInDuel(m1));
        assertFalse(service.isMeepleInDuel(m2));
    }

    @Test
    void isMeepleInDuel_noMiniGame_isIgnored() {
        Duel duel = service.createDuel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());

        // MiniGame bewusst NICHT gesetzt

        assertFalse(service.isMeepleInDuel(duel.getFirstMeeple()));
    }

    @Test
    void assignGameToDuel_registersOnFinishedCallback() {
        Duel duel = service.createDuel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());

        MiniGame game = service.assignGameToDuel(duel.getId());

        assertNotNull(game);

        Object callback = ReflectionTestUtils.getField(game, "onFinished");
        assertNotNull(callback);
    }

    @Test
    void upToFourMiniGames_canRunInParallel() {
        List<Duel> duels = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Duel duel = service.createDuel(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID());
            duels.add(duel);

            service.assignGameToDuel(duel.getId());
        }

        long assignedGames = duels.stream()
                .map(Duel::getMiniGame)
                .filter(g -> g != null)
                .count();

        assertEquals(4, assignedGames);
    }

    @Test
    void duelIsRemovedAfterMiniGameFinished() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();

        Duel duel = service.createDuel(p1, p2, m1, m2);

        Lobby lobby = mock(Lobby.class);
        when(lobbyManager.getLobbyFromPlayerUUID(p1)).thenReturn(lobby);

        List<Supplier<MiniGame>> factories = List.of(() -> new DiceGame(1));
        ReflectionTestUtils.setField(service, "gameFactories", factories);

        MiniGame game = service.assignGameToDuel(duel.getId());

        game.forceMissingActions();

        assertThrows(IllegalArgumentException.class,
                () -> service.getDuel(duel.getId()));
    }

    @Test
    void schedulerIsShutdownOnPreDestroy() {
        DuelServiceImpl localService = new DuelServiceImpl(lobbyManager, messaging, duelResolutionService, monkeyTypeWordService);

        ScheduledExecutorService scheduler = (ScheduledExecutorService) ReflectionTestUtils
                .getField(localService, "miniGameScheduler");

        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());

        localService.shutdownScheduler();

        assertTrue(scheduler.isShutdown());
    }

    @Test
    void forceMissingActions_finishesGame() {

        Lobby lobby = mock(Lobby.class);
        when(lobbyManager.getLobbyFromPlayerUUID(any())).thenReturn(lobby);

        List<java.util.function.Supplier<MiniGame>> factories = List.of(() -> new DiceGame(1));

        ReflectionTestUtils.setField(service, "gameFactories", factories);

        Duel duel = service.createDuel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());

        MiniGame game = service.assignGameToDuel(duel.getId());

        assertNotNull(game);
        assertFalse(game.isFinished());

        game.forceMissingActions();

        assertTrue(game.isFinished());
    }

}
