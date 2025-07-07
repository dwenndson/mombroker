package br.ifce.ppd.monbroker.controller;

import br.ifce.ppd.monbroker.dto.MessageDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    private final SimpMessagingTemplate messagingTemplate;

    public KafkaConsumerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Este listener usa uma expressão regular para "ouvir" todos os tópicos que começam com "user-inbox-".
    // Ele também ouve todos os outros tópicos que não começam com "user-inbox-".
    // O groupId garante que, se houver múltiplas instâncias do servidor, a mensagem seja processada apenas uma vez.
    @KafkaListener(topicPattern = ".*", groupId = "mon-broker-group")
    public void consumer(MessageDTO message) {
        System.out.println("Mensagem recebida do Kafka: " + message.getContent());

        if (message.getType() == MessageDTO.MessageType.DIRECT) {
            // Mensagem direta: envia para a fila pessoal do usuário no WebSocket.
            // Ex: /queue/messages para o usuário "luigi"
            String destination = "/queue/messages";
            System.out.println("Enviando mensagem direta para o usuário '" + message.getRecipient() + "' no destino WebSocket: " + destination);
            messagingTemplate.convertAndSendToUser(message.getRecipient(), destination, message);

        } else if (message.getType() == MessageDTO.MessageType.TOPIC) {
            // Mensagem de tópico: envia para o tópico público no WebSocket.
            // Ex: /topic/noticias
            String destination = "/topic/" + message.getRecipient();
            System.out.println("Enviando mensagem de tópico para '" + message.getRecipient() + "' no destino WebSocket: " + destination);
            messagingTemplate.convertAndSend(destination, message);
        }
    }
}
