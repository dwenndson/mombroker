package br.ifce.ppd.monbroker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/health")
    public String check() {
        return "Servidor está no ar e configurado para Kafka!";
    }
}
