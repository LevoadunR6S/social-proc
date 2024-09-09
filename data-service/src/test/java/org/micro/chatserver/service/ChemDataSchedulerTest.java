package org.micro.chatserver.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChemDataSchedulerTest {

    @Mock
    private PubChemService pubChemService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChemDataScheduler chemDataScheduler;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void setup() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void testSendPubChemData() {

        String mockData = "Mocked chemical data for Aspirin";
        when(pubChemService.getCompoundDataByCID("2244")).thenReturn(mockData);

        // Виклик планованого методу напряму
        chemDataScheduler.sendPubChemData();

        // Перевірка, що сервіс був викликаний із правильним CID
        verify(pubChemService,atMostOnce()).getCompoundDataByCID("2244");

        // Перевірка, що повідомлення було відправлено на WebSocket канал
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/updates"), messageCaptor.capture());

        // Перевірка, що дані відправлені через WebSocket є коректними
        assertEquals(mockData, messageCaptor.getValue());
    }
}
