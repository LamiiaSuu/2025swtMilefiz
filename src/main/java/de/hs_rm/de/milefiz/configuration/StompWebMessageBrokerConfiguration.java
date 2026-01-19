package de.hs_rm.de.milefiz.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import de.hs_rm.de.milefiz.messaging.PlayerTokenInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebMessageBrokerConfiguration implements WebSocketMessageBrokerConfigurer {

    private final PlayerTokenInterceptor playerTokenInterceptor;

    public StompWebMessageBrokerConfiguration(PlayerTokenInterceptor playerTokenInterceptor) {
        this.playerTokenInterceptor = playerTokenInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000}) // Alle 10 Sekunden incoming/outgoing Heartbeats für keepalive
                .setTaskScheduler(heartbeatScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(playerTokenInterceptor);
    }

    /**
     * Scheduler welcher die Heartbeat-Pings verwaltet. Verhindert, dass bei Inaktivität die Verbindung automatisch geschlossen wird
     * @return TaskScheduler mit ThreadPool (standard 1 Thread)
     * 
     */
    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        return scheduler;
    }
}
