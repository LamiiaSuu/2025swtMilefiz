package de.hs_rm.de.milefiz.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import de.hs_rm.de.milefiz.game.lobby.LobbyManager;
import de.hs_rm.de.milefiz.game.lobby.PlayerNotFoundException;
import de.hs_rm.de.milefiz.game.model.Player;

@Component
public class PlayerTokenInterceptor implements ChannelInterceptor {

    @Autowired
    private LobbyManager lobbyManager;

    private final Logger logger = LoggerFactory.getLogger(PlayerTokenInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = org.springframework.messaging.support.MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("player-token");

            // Falls kein Header, versuche Query-Parameter
            if (token == null) {
                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                if (sessionAttrs != null) {
                    token = (String) sessionAttrs.get("player-token");
                }
            }

            try {
                Player player = lobbyManager.getPlayerByTokenFromLobbies(token);
                if (player == null) {
                    throw new IllegalArgumentException("Invalid player token");
                }

                accessor.setLeaveMutable(true);
                accessor.setUser(player);
                accessor.getSessionAttributes().put("player-token", player.getPlayerToken());
                accessor.getSessionAttributes().put("player", player);
            } catch (PlayerNotFoundException | IllegalArgumentException e) {
                logger.error("Player not found", e);
                // Verbindung ablehnen bei ungültigem Token
                return null;
            }
        }

        // Für MESSAGE und SEND Commands: Token aus Session-Attributen oder Header holen
        if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.MESSAGE.equals(accessor.getCommand())) {
            String token = (String) accessor.getSessionAttributes().get("player-token");

            // Falls nicht in Session, versuche aus Header zu lesen
            if (token == null) {
                token = accessor.getFirstNativeHeader("player-token");
            }

            // Versuche Player aus Session zu holen
            Player player = (Player) accessor.getSessionAttributes().get("player");

            if (player == null && token != null) {
                try {
                    player = lobbyManager.getPlayerByTokenFromLobbies(token);
                } catch (PlayerNotFoundException e) {
                    logger.error("Player not found", e);
                }
            }

            if (player != null) {
                accessor.setLeaveMutable(true);
                accessor.setUser(player);
            } else {
                accessor.setLeaveMutable(true);
            }

            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        return message;
    }
}
