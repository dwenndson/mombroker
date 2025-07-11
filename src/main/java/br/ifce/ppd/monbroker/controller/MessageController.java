package br.ifce.ppd.monbroker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.ifce.ppd.monbroker.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ifce.ppd.monbroker.service.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages/")
@Tag(name = "Envio de Mensagens", description = "Endpoint para enviar mensagens para o Kafka")
public class MessageController {
    private final KafkaProducerService kafkaProducerService;

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    public MessageController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Operation(summary = "Envia uma mensagem direta ou para um tópico")
    @PostMapping("send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO message) {
        try {
            kafkaProducerService.sendMessage(message);
            return ResponseEntity.ok("Mensagem enviada com sucesso.");
        } catch (Exception e) {
            logger.error("Falha ao enviar mensagem para a API.", e);
            return ResponseEntity.status(500).body("Erro ao enviar mensagem: "+ e.getMessage());
        }
    }
}
