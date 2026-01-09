package server;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Sets up the configuration for WebSocket messages
     * @param config - the message broker which enables communication between client and server
     */
    @Override
    public void configureMessageBroker (MessageBrokerRegistry config) {
        // For messages from the server to the client - different topics will be created
        config.enableSimpleBroker("/topic");
        // For messages from the client to the server
        config.setApplicationDestinationPrefixes("/client_communication");
    }

    /**
     * Sets the destination for the initial "handshake" agreement
     * @param registry - sets the URL for the initial connection of the client
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The URL where the client connects initially
        registry.addEndpoint("/ws-updates").setAllowedOrigins("*");
        // .withSockJS();
    }

}