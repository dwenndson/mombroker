package br.ifce.ppd.monbroker.service;

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


    @KafkaListener(topicPattern = ".*", groupId = "mom-broker-group")
    public void consumer(MessageDTO message) {
        System.out.println("Mensagem recebida do Kafka: " + message.getContent() + " para " + message.getRecipient());

        if (message.getType() == MessageDTO.MessageType.DIRECT) {
            // Mensagem direta: envia para a fila pessoal do usuário no WebSocket.
            // Ex: /queue/messages para o usuário "luigi"
            String destination = "/user/" + message.getRecipient() + "/queue/messages";
            System.out.println("Enviando mensagem direta para o usuário '" + message.getRecipient() + "' no destino WebSocket: " + destination);
            // O Spring usa o Principal do STOMP para rotear a mensagem para o usuário correto.
            messagingTemplate.convertAndSend(destination, message);

        } else if (message.getType() == MessageDTO.MessageType.TOPIC) {
            // Mensagem de tópico: envia para o tópico público no WebSocket.
            // Ex: /topic/noticias
            String destination = "/topic/" + message.getRecipient();
            System.out.println("Enviando mensagem de tópico para '" + message.getRecipient() + "' no destino WebSocket: " + destination);
            messagingTemplate.convertAndSend(destination, message);
        }
    }
}
