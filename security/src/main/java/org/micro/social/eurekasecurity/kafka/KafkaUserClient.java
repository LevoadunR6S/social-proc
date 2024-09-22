package org.micro.social.eurekasecurity.kafka;

import org.micro.shareable.kafka.KafkaMessage;
import org.micro.shareable.dto.UserDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.concurrent.*;

@Component
public class KafkaUserClient {

    private KafkaTemplate<String, Object> kafkaTemplate;

    //Мапи для запису запитів
    private ConcurrentMap<String, CompletableFuture<UserDto>> mapForUsers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, CompletableFuture<String>> mapForStrings = new ConcurrentHashMap<>();

    public KafkaUserClient(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Метод для отримання UserDto за допомогою username
    public Optional<UserDto> getUserByUsername(String username) {
        return handleKafkaRequest(username, "user-security-requests", mapForUsers);
    }

    // Метод для створення нового користувача
    public Optional<String> createUser(UserDto userDto) {
        return handleKafkaRequest(userDto, "from-security-to-user-save", mapForStrings);
    }

    // Загальний метод для обробки запитів через Kafka
    private <T> Optional<T> handleKafkaRequest(Object key, String topic, ConcurrentMap<String, CompletableFuture<T>> map) {
        CompletableFuture<T> future = new CompletableFuture<>();
        //Якщо key належить класу UserDto, то нам потрібно зберегти користувача
        if (key instanceof UserDto) {
            UserDto user = (UserDto) key;
            map.put(user.getUsername(), future);
            //Надсилаємо повідомлення в UserService з UserDto
            kafkaTemplate.send(topic, new KafkaMessage(user));
        }
        //Якщо key належить класу String, то нам потрібно отримати користувача
        else if (key instanceof String) {
            String id = (String) key;
            map.put(id, future);
            //Надсилаємо повідомлення в UserService з ім'ям користувача для пошуку
            kafkaTemplate.send(topic, new KafkaMessage(id));
        }

        try {
            T result = future.get(); // Очікування результату запиту
            return Optional.ofNullable(result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            // Видалення запису з мапи після отримання результату або виникнення помилки
            map.remove(key);
        }
    }

    // Обробка відповіді від UserService для збереження даних
    @KafkaListener(topics = "from-user-to-security-save", groupId = "security-group")
    public void getResultFromUserService(KafkaMessage response) {
        //Отримуємо результат збереження користувача
        UserDto userDto = response.getUserDto();
        if (userDto != null) {
            //Позначаємо завдання як виконане
            mapForStrings.get(userDto.getUsername()).complete(response.getInfo());
        }
    }

    // Обробка відповіді від сервісу безпеки
    @KafkaListener(topics = "user-security-response", groupId = "request-user")
    public void listenUserResponse(KafkaMessage response) {
        //Отримуємо результат пошуку користувача
        UserDto userDto = response.getUserDto();
        String id = userDto.getUsername();
        //Позначаємо завдання як виконане
        mapForUsers.get(id).complete(userDto);
    }
}
