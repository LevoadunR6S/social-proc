package org.micro.social.eurekaclient.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.micro.shareable.model.Role;
import org.micro.social.eurekaclient.model.User;
import org.micro.social.eurekaclient.repository.UserRepository;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = UserService.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void findByUsername() {
        User user = new User("bob",
                "bob@gmail.com",
                "bob",
                LocalDate.of(2000, 10, 10),
                Set.of(new Role("USER")));
        when(userRepository.getByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User result = userService.findByUsername(user.getUsername()).get();
        assertEquals(user, result);
        verify(userRepository, times(1)).getByUsername(user.getUsername());
    }

    @Test
    void createNewUser() {
        User user = new User("bob",
                "bob@gmail.com",
                "bob",
                LocalDate.of(2000, 10, 10),
                Set.of(new Role("USER")));

        when(userRepository.save(user)).thenReturn(user);
        User result = userService.createNewUser(user);
        assertEquals(user, result);
        verify(userRepository, times(1)).save(user);
    }





}
