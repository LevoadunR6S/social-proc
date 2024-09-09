package org.micro.social.eurekaclient.service;

import org.micro.shareable.dto.UserDto;
import org.micro.shareable.kafka.KafkaMessage;
import org.micro.shareable.model.Role;
import org.micro.social.eurekaclient.model.User;
import org.micro.social.eurekaclient.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate; // Шаблон для відправки повідомлень до Kafka

    @Autowired
    public UserService(UserRepository userRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Метод для знаходження користувача за username
    public Optional<User> findByUsername(String username) {
        return userRepository.getByUsername(username);
    }

    // Метод для створення нового користувача в базі даних
    @Transactional
    public User createNewUser(User user) {
        return userRepository.save(user);
    }

    // Метод для обробки повідомлень про отримання користувача з бази даних
    @Transactional
    @KafkaListener(topics = "user-security-requests", groupId = "user-service-group")
    public void handleUserRequest(KafkaMessage message) {
        processUserRequest(message, "user-security-response");
    }

    // Метод для обробки повідомлень про збереження користувача в базу даних
    @KafkaListener(topics = "from-security-to-user-save", groupId = "user-requests")
    public void saveUser(KafkaMessage message) {
        processUserRequest(message, "from-user-to-security-save");
    }

    // Метод для обробки запитів користувачів і відправки відповідей
    private void processUserRequest(KafkaMessage message, String responseTopic) {

        // Якщо тема повідомлення довівнює "from-user-to-security-save",
        // потрібно спробувати зберегти користувача і повернути повідомлення з результатом спроби
        if ("from-user-to-security-save".equals(responseTopic)) {
            UserDto userDto = message.getUserDto();

            // Перевіряє, чи існує вже користувач з таким username або паролем
            if (userRepository.getByUsername(userDto.getUsername()).isPresent() ||
                    userRepository.getByPassword(userDto.getPassword()).isPresent()) {
                //Надсилаємо повідомлення на SecurityService із сповіщенням про те,
                //що ім'я або пароль користувача вже існує в базі даних і повертаємо UserDto цього користувача
                kafkaTemplate.send(responseTopic, new KafkaMessage(userDto, "Username or password exists"));
            }
            // Створює нового користувача, якщо його не існує
            else {
                User newUser = new User(userDto.getUsername(), userDto.getEmail(), userDto.getPassword(), userDto.getBirthDate(),
                        userDto.getRoles());
                createNewUser(newUser);
                //Надсилаємо повідомлення на SecurityService із сповіщенням про те,
                //що користувач успішно створений, а також UserDto цього користувача
                kafkaTemplate.send(responseTopic, new KafkaMessage(userDto, "Created"));
            }
        }

        // Якщо тема повідомлення дорівнює "user-security-response",
        // потрібно спробувати знайти користувача в базі даних та повернути його як результат
        else if ("user-security-response".equals(responseTopic)) {
            Optional<User> user = findByUsername(message.getUsername());
            Set<Role> roles = user.get().getRoles().stream().collect(Collectors.toSet());

            // Якщо користувач існує, відправляє його дані
            if (user.isPresent()) {
                UserDto userDto = new UserDto(user.get().getUsername(),
                        user.get().getPassword(),
                        user.get().getEmail(),
                        user.get().getBirthDate(), roles);
                //Надсилаємо повідомлення на SecurityService із знайденим користувачем
                kafkaTemplate.send(responseTopic, new KafkaMessage(userDto));
            }
            // Якщо користувач не існує, відправляє повідомлення з ім'ям користувача
            else {
                kafkaTemplate.send(responseTopic, new KafkaMessage(null, message.getUsername()));
            }
        }
    }
}
