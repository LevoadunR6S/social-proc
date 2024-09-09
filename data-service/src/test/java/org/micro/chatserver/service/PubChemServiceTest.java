package org.micro.chatserver.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PubChemServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PubChemService pubChemService;

    private AutoCloseable autoClosable;

    @BeforeEach
    void setUp() {
        autoClosable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoClosable.close();
    }

    @Test
    public void testGetCompoundDataByCID() {
        String mockResponse = "{\"Title\": \"Aspirin\", \"MolecularWeight\": \"180.16\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        String result = pubChemService.getCompoundDataByCID("2244");

        // Перевіряємо, що RestTemplate викликав правильний URL
        verify(restTemplate).getForObject(contains("2244"), eq(String.class));

        // Перевіряємо, що результат відповідає очікуваним даним
        assertEquals(mockResponse, result);
    }
}