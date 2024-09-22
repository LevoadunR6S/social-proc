/*
package org.micro.social.eurekasecurity.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.micro.shareable.dto.UserDto;
import org.micro.shareable.model.Role;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.*;
import org.micro.shareable.kafka.KafkaMessage;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;

@EmbeddedKafka(partitions = 1, topics = { "user-security-requests", "user-security-response","from-security-to-user-save" }, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" })
class KafkaUserClientTest {

    @InjectMocks
    private KafkaUserClient kafkaUserClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ConcurrentMap<String, CompletableFuture<UserDto>> mapForUsers;

    @Mock
    private ConcurrentMap<String, CompletableFuture<String>> mapForStrings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getUserByUsername(){
        String username = "bob";
        UserDto userDto = new UserDto("bob",
                "bob",
                "bob",
                LocalDate.of(2000,10,10),
                Set.of(new Role("USER")));
        CompletableFuture<UserDto> future = new CompletableFuture<>();
        future.complete(userDto);
        when(mapForUsers.get(username)).thenReturn(future);

        // Мокуємо метод send() для перевірки
        when(kafkaTemplate.send(anyString(), any(KafkaMessage.class))).thenReturn(null);

        Optional<UserDto> result = kafkaUserClient.getUserByUsername(username);

        verify(kafkaTemplate).send(eq("user-security-requests"), any(KafkaMessage.class));
        assertTrue(result.isPresent());
        assertEquals(userDto, result.get());
    }

    @Test
    void createUser() throws Exception {
        UserDto userDto = new UserDto("bob",
                "bob",
                "bob",
                LocalDate.of(2000,10,10),
                Set.of(new Role("USER")));
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("Created");
        when(mapForStrings.get(userDto.getUsername())).thenReturn(future);

        // Мокуємо метод send() для перевірки
        when(kafkaTemplate.send(anyString(), any(KafkaMessage.class))).thenReturn(null);

        Optional<String> result = kafkaUserClient.createUser(userDto);

        verify(kafkaTemplate).send(eq("from-security-to-user-save"), any(KafkaMessage.class));
        assertTrue(result.isPresent());
        assertEquals("Created", result.get());
    }

}*/
