package org.micro.social.eurekasecurity.kafka;

import org.micro.shareable.dto.UserDto;
import org.micro.shareable.kafka.KafkaMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

import java.util.concurrent.*;

@Component
public class KafkaUserClient {

    private KafkaTemplate<String, Object> kafkaTemplate;

    private ConcurrentMap<String, CompletableFuture<UserDto>> mapForUsers = new ConcurrentHashMap<>();

    private ConcurrentMap<String, CompletableFuture<String>> mapForStrings = new ConcurrentHashMap<>();


    public KafkaUserClient(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }





    public Optional<UserDto> getUserByUsername(String username) {
        return handleKafkaRequest(username, "user-security-requests", mapForUsers);
    }

    public Optional<String> createUser(UserDto userDto) {
        return handleKafkaRequest(userDto, "from-security-to-user-save", mapForStrings);
    }

    private <T> Optional<T> handleKafkaRequest(Object key, String topic, ConcurrentMap<String, CompletableFuture<T>> map) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (key instanceof UserDto) {
            UserDto user = (UserDto) key;
            map.put(user.getUsername(), future);
            kafkaTemplate.send(topic, new KafkaMessage(user));
        } else if (key instanceof String) {
            map.put((String) key, future);
            kafkaTemplate.send(topic, new KafkaMessage((String)key));
        }

        try {
            T result = future.get();
            return Optional.ofNullable(result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            map.remove(key);
        }
    }


    @KafkaListener(topics = "from-user-to-security-save", groupId = "security-group")
    public void getResultFromUserService(KafkaMessage response) {
        UserDto userDto = response.getUserDto();
        if (userDto != null) {
            mapForStrings.get(userDto.getUsername()).complete(response.getInfo());
        }
    }

    //отримує user (викликається автоматично)
    @KafkaListener(topics = "user-security-response", groupId = "request-user")
    public void listenUserResponse(KafkaMessage response) {
        UserDto userDto = response.getUserDto();
        mapForUsers.get(userDto.getUsername()).complete(userDto);
    }


}
