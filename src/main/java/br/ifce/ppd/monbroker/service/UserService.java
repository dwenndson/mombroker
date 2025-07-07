package br.ifce.ppd.monbroker.service;

import br.ifce.ppd.monbroker.service.strings.NomeclaturaComponent;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final  KafkaAdminService kafkaAdminService;
    private NomeclaturaComponent nomeclaturaComponent;
    private final Set<String> users = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public UserService(KafkaAdminService kafkaAdminService){
        this.kafkaAdminService = kafkaAdminService;
    }

    public boolean registerUser(String username) {
        if(users.add(username)){
            String personalTopic = nomeclaturaComponent.USER_INBOX + username;
            try {
                kafkaAdminService.createTopic(personalTopic);
                System.out.println("Tópico pessoal criado para " + username + ": " + personalTopic);
                return true;
            } catch (Exception e){
                users.remove(username);
                System.err.println("Falha ao criar tópico para " + username + ". Registro desfeito.");
                throw new RuntimeException("Falha ao criar tópico pessoal.", e);
            }
        }
        return false;
    }

    public Set<String> listUsers(){
        return Collections.unmodifiableSet(users);
    }
}
