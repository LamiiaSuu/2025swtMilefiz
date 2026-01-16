package de.hs_rm.de.milefiz.messaging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;
import de.hs_rm.de.milefiz.game.model.Player;

@ExtendWith(MockitoExtension.class)
class PlayerTokenInterceptorTest {

    private LobbyManager lobbyManager;
    private PlayerTokenInterceptor interceptor;
    private MessageChannel channel;

    @BeforeEach
    void setup() {
        lobbyManager = mock(LobbyManager.class);
        interceptor = new PlayerTokenInterceptor();

        ReflectionTestUtils.setField(interceptor, "lobbyManager", lobbyManager);
        channel = mock(MessageChannel.class);
    }

    private Message<?> buildMessage(StompHeaderAccessor accessor, Object payload) {
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }

    @Test
    void connect_withValidToken_setsUser() throws Exception {
        Player player = mock(Player.class);
        when(player.getPlayerToken()).thenReturn("token123");
        when(lobbyManager.getPlayerByTokenFromLobbies("token123")).thenReturn(player);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("player-token", "token123");
        accessor.setSessionAttributes(new HashMap<>());

        Message<?> message = buildMessage(accessor, new byte[0]);

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        assertEquals(player, accessor.getUser());
        assertEquals("token123", accessor.getSessionAttributes().get("player-token"));
        assertEquals(player, accessor.getSessionAttributes().get("player"));
    }

    @Test
    void connect_withInvalidToken_rejected() throws Exception {
        when(lobbyManager.getPlayerByTokenFromLobbies("bad"))
                .thenThrow(new PlayerNotFoundException("not found"));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("player-token", "bad");
        accessor.setSessionAttributes(new HashMap<>());

        Message<?> message = buildMessage(accessor, new byte[0]);

        Message<?> result = interceptor.preSend(message, channel);

        assertNull(result);
    }

    @Test
    void send_withPlayerInSession_setsUser() {
        Player player = mock(Player.class);

        Map<String, Object> session = new HashMap<>();
        session.put("player", player);
        session.put("player-token", "token123");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionAttributes(session);

        Message<?> message = buildMessage(accessor, "payload");

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        assertEquals(player, accessor.getUser());
    }

    @Test
    void send_withTokenButNoSessionPlayer_loadsPlayer() throws Exception {
        Player player = mock(Player.class);
        when(lobbyManager.getPlayerByTokenFromLobbies("token123")).thenReturn(player);

        Map<String, Object> session = new HashMap<>();
        session.put("player-token", "token123");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionAttributes(session);

        Message<?> message = buildMessage(accessor, "payload");

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        assertEquals(player, accessor.getUser());
    }

    @Test
    void send_withoutPlayer_allowsMessage() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        accessor.setSessionAttributes(new HashMap<>());

        Message<?> message = buildMessage(accessor, "payload");

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        assertNull(accessor.getUser());
    }
}
