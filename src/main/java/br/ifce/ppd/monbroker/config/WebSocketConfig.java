package br.ifce.ppd.monbroker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig  implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        // /topic: Destino para mensagens pub/sub (broadcast para todos os inscritos).
        // /queue: Destino para mensagens ponto a ponto (direcionadas a um usuário específico).
        config.enableSimpleBroker("/topic", "queue");
        // Define o prefixo para endpoints de aplicação
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // Registra o endpoint "/ws" para a conexão WebSocket.
        // withSockJS() oferece uma alternativa caso o WebSocket não seja suportado.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
