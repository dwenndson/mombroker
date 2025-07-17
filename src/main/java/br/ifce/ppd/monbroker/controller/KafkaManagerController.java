package br.ifce.ppd.monbroker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import br.ifce.ppd.monbroker.service.KafkaAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/kafka")
@Tag(name = "Gerenciador de Tópicos Kafka", description = "Endpoints para criar, listar e remover tópicos no Kafka")
public class KafkaManagerController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaManagerController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @Operation(summary = "Cria um novo tópico no Kafka")
    @PostMapping("/topic")
    public ResponseEntity<String> createTopic(@RequestBody Map<String, String> payload) {
        String topicName = payload.get("name");
        if (topicName == null || topicName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O nome do tópico não pode ser vazio.");
        }
        try {
            kafkaAdminService.createTopic(topicName);
            return ResponseEntity.ok("Tópico '" + topicName + "' criado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao criar tópico: " + e.getMessage());
        }
    }
    @Operation(summary = "Lista todos os tópicos existentes no broker")
    @GetMapping("/topics")
    public ResponseEntity<Set<String>> getTopics() {
        try {
            return ResponseEntity.ok(kafkaAdminService.listTopics());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @Operation(summary = "Remove um tópico específico do broker")
    @DeleteMapping("/topic/{topicName}")
    public ResponseEntity<String> deleteTopic(@PathVariable String topicName) {
        if (topicName == null || topicName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O nome do tópico não pode ser vazio.");
        }
        try {
            kafkaAdminService.deleteTopic(topicName);
            return ResponseEntity.ok("Tópico '" + topicName + "' deletado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao deletar tópico: " + e.getMessage());
        }
    }

    @Operation(summary = "Retorna a quantidade de mensagens em um tópico")
    @GetMapping("/topic/{topicName}/count")
    public ResponseEntity<Map<String, Long>> getTopicMessageCount(@PathVariable String topicName) {
        long count = kafkaAdminService.getTopicMessageCount(topicName);
        return ResponseEntity.ok(Map.of("messageCount", count));
    }
}
