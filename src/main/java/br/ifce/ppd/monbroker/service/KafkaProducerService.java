package br.ifce.ppd.monbroker.service;

import br.ifce.ppd.monbroker.dto.MessageDTO;
import br.ifce.ppd.monbroker.service.strings.StringsConstants;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {


    private final KafkaTemplate<String, MessageDTO> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, MessageDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(MessageDTO message) {
        String topicName;
        if (message.getType() == MessageDTO.MessageType.DIRECT) {
            topicName = StringsConstants.USER_INBOX + message.getRecipient();
        } else {
            topicName = message.getRecipient();
        }
        System.out.println("Enviando mensagem para o tópico Kafka: " + topicName);
        kafkaTemplate.send(topicName, message);
    }
}
