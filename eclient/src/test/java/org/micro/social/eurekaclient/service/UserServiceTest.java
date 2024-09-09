package org.micro.social.eurekaclient.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.micro.shareable.model.Role;
import org.micro.social.eurekaclient.model.User;
import org.micro.social.eurekaclient.repository.UserRepository;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @InjectMocks
    private UserService userService;


    @Mock
    private UserRepository userRepository;

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