package de.hs_rm.de.milefiz.game.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_rm.de.milefiz.messaging.events.FrontendCooldownFinishedEvent;

@ExtendWith(MockitoExtension.class)
class CooldownServiceImplTest {

    @Mock
    private ApplicationEventPublisher publisher;

    private CooldownServiceImpl service;

    @BeforeEach
    void setup() {
        service = new CooldownServiceImpl(publisher);
        ReflectionTestUtils.setField(service, "defaultSeconds", 3);
    }

    // ------------------------------------------------------------
    // addCooldown / getCooldown
    // ------------------------------------------------------------

    @Test
    void addCooldown_setsDefaultSeconds() {
        UUID playerId = UUID.randomUUID();

        service.addCooldown(playerId);

        assertEquals(3, service.getCooldown(playerId));
    }

    @Test
    void getCooldown_unknownPlayer_returnsZero() {
        assertEquals(0, service.getCooldown(UUID.randomUUID()));
    }

    // ------------------------------------------------------------
    // tick
    // ------------------------------------------------------------

    @Test
    void tick_reducesCooldownByOne() {
        UUID playerId = UUID.randomUUID();

        service.addCooldown(playerId);
        service.tick();                

        assertEquals(2, service.getCooldown(playerId));
        verifyNoInteractions(publisher);
    }

    @Test
    void tick_whenCooldownReachesZero_removesEntryAndPublishesEvent() {
        UUID playerId = UUID.randomUUID();

        @SuppressWarnings("unchecked")
        Map<UUID, Integer> cooldowns =
            (Map<UUID, Integer>) ReflectionTestUtils.getField(service, "cooldowns");

        cooldowns.put(playerId, 1);

        service.tick();

        assertEquals(0, service.getCooldown(playerId));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());

        Object event = captor.getValue();
        assertTrue(event instanceof FrontendCooldownFinishedEvent);

        FrontendCooldownFinishedEvent finished =
            (FrontendCooldownFinishedEvent) event;

        assertEquals(playerId, finished.playerId());
    }


}
