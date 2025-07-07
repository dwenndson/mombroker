package br.ifce.ppd.monbroker.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        // Configura o endereço do servidor Kafka para o Admin
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    // O Spring usa este método para criar um tópico inicial, se ele não existir.
    // É útil para garantir que tópicos essenciais estejam sempre disponíveis.
    // Para o nosso gerenciador, a criação será dinâmica, mas é bom ter este exemplo.
    @Bean
    public NewTopic initialTopic() {
        return TopicBuilder.name("general-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
