package br.ifce.ppd.monbroker.config;

import br.ifce.ppd.monbroker.dto.UserPrincipal;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
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
        config.setUserDestinationPrefix("/user");
        config.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // Registra o endpoint "/ws" para a conexão WebSocket.
        // withSockJS() oferece uma alternativa caso o WebSocket não seja suportado.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Verifica se é um frame de conexão (CONNECT)
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Busca o nome de usuário do cabeçalho 'login' enviado pelo client.js
                    try {
                        String username = accessor.getFirstNativeHeader("login");
                        if (!username.trim().isEmpty()) {
                            // Cria nosso Principal customizado e o associa à sessão
                            UserPrincipal principal = new UserPrincipal(username.trim());
                            accessor.setUser(principal);
                            System.out.println("### WebSocket Principal set for user: " + username);

                        } else {
                            System.err.println("### AVISO: Conexão WebSocket recebida sem o cabeçalho 'login'.");
                        }
                    } catch (Exception e) {
                        System.err.println("!!! EXCEÇÃO CRÍTICA NO INTERCEPTOR DO WEBSOCKET !!!" + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return message;
            }
        });
    }
}
