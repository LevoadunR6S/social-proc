package org.micro.chatserver.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;

//Клас для циклічного отримання даних зі стороннього API
public class ChemDataScheduler {

    private final PubChemService pubChemService;
    private final SimpMessagingTemplate messagingTemplate;  // Шаблон для надсилання повідомлень через WebSocket

    // Конструктор для ін'єкції сервісу PubChemService і SimpMessagingTemplate
    public ChemDataScheduler(PubChemService pubChemService, SimpMessagingTemplate messagingTemplate) {
        this.pubChemService = pubChemService;
        this.messagingTemplate = messagingTemplate;
    }

    // Метод, що виконується кожні 100000 мс (100 секунд) завдяки анотації @Scheduled
    @Scheduled(fixedRate = 100000)
    public void sendPubChemData() {
        /*
          Вода CID: 962
          Етанол CID: 702
          Кофеїн CID: 2519
          Глюкоза CID: 5793
          Аспірин CID: 2244
          Парацетамол CID: 1983
          Метан CID: 297
          Бензол CID: 241
          Аскорбінова кислота CID: 54670067
          Натрій хлорид CID: 5234
          */
        // Приклад Chemical Identifier (CID) для аспірину
        String cid = "2244";
        // Отримує дані про хімічну сполуку за вказаним CID
        String data = pubChemService.getCompoundDataByCID(cid);
        // Надсилає отримані дані до WebSocket клієнтів на канал "/topic/updates"
        messagingTemplate.convertAndSend("/topic/updates", data);
    }
}
