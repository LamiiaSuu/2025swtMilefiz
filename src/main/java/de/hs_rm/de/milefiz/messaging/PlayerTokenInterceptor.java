package de.hs_rm.de.milefiz.messaging;

import java.security.Principal;

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

    // @Override
    // public Message<?> preSend(Message<?> message, MessageChannel channel) {
    // StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

    // if (StompCommand.CONNECT.equals(sha.getCommand())) {
    // String token = sha.getFirstNativeHeader("player-token");
    // System.out.println("Mapping to player: " + token);
    // try {
    // Player player = lobbyManager.getPlayerBySessionIdFromLobbies(token);
    // if (player == null) {
    // throw new IllegalArgumentException("Invalid player token");
    // }

    // sha.setUser(() -> String.valueOf(player.getId()));
    // sha.setLeaveMutable(true);

    // System.out.println("User gefunden! " + player.getColor().name());
    // } catch (PlayerNotFoundException e) {
    // e.printStackTrace();
    // }
    // }
    // return MessageBuilder.createMessage(message.getPayload(),
    // sha.getMessageHeaders());
    // }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = org.springframework.messaging.support.MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("player-token");
            System.out.println("Mapping to player: " + token);
            try {
                Player player = lobbyManager.getPlayerBySessionIdFromLobbies(token);
                if (player == null) {
                    throw new IllegalArgumentException("Invalid player token");
                }

                accessor.setUser(() -> String.valueOf(player.getId()));
                accessor.setLeaveMutable(true);

                System.out.println("User gefunden! " + player.getColor().name());
            } catch (PlayerNotFoundException e) {
                e.printStackTrace();
            }
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

    }
}
