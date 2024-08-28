package org.micro.social.eurekaclient.service;


import org.micro.shareable.dto.UserDto;
import org.micro.shareable.kafka.KafkaMessage;
import org.micro.social.eurekaclient.model.User;
import org.micro.social.eurekaclient.repository.RoleRepository;
import org.micro.social.eurekaclient.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserService(RoleRepository roleRepository, UserRepository userRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }


    public Optional<User> findByUsername(String username) {
        return userRepository.getByUsername(username);
    }


    public User getUserById(Integer id) {
        return userRepository.getById(id);
    }

    public User createNewUser(User user) {
        user.setPassword(hashPassword(user.getPassword()));
        return userRepository.save(user);
    }


    private String hashPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    @Transactional
    @KafkaListener(topics = "user-security-requests", groupId = "user-service-group")
    public void handleUserRequest(KafkaMessage message) {
        processUserRequest(message, "user-security-response");
    }

    @KafkaListener(topics = "from-security-to-user-save", groupId = "user-requests")
    public void saveUser(KafkaMessage message) {
        processUserRequest(message, "from-user-to-security-save");
    }

    private void processUserRequest(KafkaMessage message, String responseTopic) {
        //save user
        if ("from-user-to-security-save".equals(responseTopic)) {
            UserDto userDto = message.getUserDto();

            //user exists
            if (userRepository.getByUsername(userDto.getUsername()).isPresent() |
                    userRepository.getByPassword(userDto.getPassword()).isPresent()) {
                kafkaTemplate.send(responseTopic, new KafkaMessage(userDto,"Username or password exists"));
            }

            //create user
            else {
            User newUser = new User(userDto.getUsername(), userDto.getEmail(), userDto.getPassword(), userDto.getBirthDate(),
                    Set.of(roleRepository.findByName("USER").get()));
            createNewUser(newUser);
            kafkaTemplate.send(responseTopic, new KafkaMessage(userDto, "Created"));
            }
        }

        //user requests
        else if ("user-security-response".equals(responseTopic)) {
            Optional<User> user = findByUsername(message.getUsername());

            //(user exists)
            if (user.isPresent()) {
                UserDto userDto = new UserDto(user.get().getUsername(),
                        user.get().getEmail(),
                        user.get().getPassword(),
                        user.get().getBirthDate(),
                        user.get().getRoles().stream().collect(Collectors.toSet()));
                kafkaTemplate.send(responseTopic, new KafkaMessage(userDto));
            }

            //(user doesn't exists)
            else {
                kafkaTemplate.send(responseTopic, new KafkaMessage(null, message.getUsername()));
            }
        }
    }

}
