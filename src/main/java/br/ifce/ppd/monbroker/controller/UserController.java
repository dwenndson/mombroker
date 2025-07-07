package br.ifce.ppd.monbroker.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.ifce.ppd.monbroker.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/users/")
@Tag(name = "Gerenciador de Usuários", description = "Endpoints para registrar e listar usuários")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Registra um novo usuário e cria seu tópico pessoal")
    @PostMapping(value = "register")
    public ResponseEntity<String> registerNewUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (username == null || username.trim().isEmpty()){
            return ResponseEntity.badRequest().body("O nome de usuário não pode ser vazio");
        }
        try {
            boolean success = userService.registerUser(username);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Usuário '" + username + "' registrado com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("O nome de usuário '" + username + "' já existe.");
            }
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Lista todos os usuários registrados")
    @GetMapping()
    public ResponseEntity<Set<String>> listAllUsers(){
        return ResponseEntity.ok(userService.listUsers());
    }
}
