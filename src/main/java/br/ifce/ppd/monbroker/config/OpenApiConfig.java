package br.ifce.ppd.monbroker.config;


import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API do Gerenciador MOM com Kafka")
                        .version("1.0")
                        .description("API para gerenciar tópicos e usuários, e para enviar mensagens através de um broker Kafka. " +
                                "Este projeto demonstra a integração de um Message-Oriented Middleware com uma aplicação web."));
    }
}