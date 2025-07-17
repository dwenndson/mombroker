package br.ifce.ppd.monbroker.service;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class KafkaAdminService {

    private final KafkaAdmin kafkaAdmin;

    public KafkaAdminService(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    public void createTopic(String topicName) {
        // Define o novo tópico com 1 partição e 1 réplica (padrão para desenvolvimento)
        NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);

        // Usa o método do KafkaAdmin para criar ou modificar o tópico.
        // Ele lida com a conexão e o AdminClient internamente.
        kafkaAdmin.createOrModifyTopics(newTopic);
    }

    /**
     * Lista todos os tópicos existentes no broker.
     * Para esta operação, ainda é mais direto usar um AdminClient temporário.
     * @return Um conjunto com os nomes dos tópicos.
     */
    public Set<String> listTopics() {
        // Criamos um AdminClient temporário apenas para esta operação de listagem.
        // O try-with-resources garante que ele seja fechado corretamente após o uso.
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            return adminClient.listTopics().names().get();
        } catch (InterruptedException | ExecutionException e) {
            // Adiciona o Thread.currentThread().interrupt() para boas práticas de tratamento de InterruptedException
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Falha ao listar os tópicos: " + e.getMessage(), e);
        }
    }

    /**
     * Remove um tópico do broker.
     * @param topicName O nome do tópico a ser removido.
     */
    public void deleteTopic(String topicName) {
        // Similar à listagem, usamos um AdminClient temporário para garantir o fechamento.
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            adminClient.deleteTopics(Collections.singleton(topicName)).all().get();
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Falha ao deletar o tópico: " + e.getMessage(), e);
        }
    }

    public long getTopicMessageCount(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            // Descreve o tópico para obter informações sobre suas partições
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topicName));
            Map<String, TopicDescription> topicDescriptionMap = describeTopicsResult.allTopicNames().get();

            if (topicDescriptionMap.isEmpty()) {
                return 0;
            }

            // Pega a lista de partições do tópico
            TopicDescription topicDescription = topicDescriptionMap.get(topicName);
            Set<TopicPartition> partitions = topicDescription.partitions().stream()
                    .map(p -> new TopicPartition(topicName, p.partition()))
                    .collect(Collectors.toSet());

            // Obtém os offsets de início e fim para as partições
            Map<TopicPartition, Long> startOffsets = adminClient.listOffsets(
                    partitions.stream().collect(Collectors.toMap(p -> p, p -> OffsetSpec.earliest()))
            ).all().get().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().offset()));

            Map<TopicPartition, Long> endOffsets = adminClient.listOffsets(
                    partitions.stream().collect(Collectors.toMap(p -> p, p -> OffsetSpec.latest()))
            ).all().get().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().offset()));

            // Calcula o total de mensagens somando (end - start) para cada partição
            long totalMessages = partitions.stream()
                    .mapToLong(p -> endOffsets.getOrDefault(p, 0L) - startOffsets.getOrDefault(p, 0L))
                    .sum();

            return totalMessages;

        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            // Retorna 0 em caso de erro (ex: tópico não encontrado)
            return 0;
        }
    }
}
