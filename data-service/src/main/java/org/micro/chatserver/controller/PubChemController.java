package org.micro.chatserver.controller;

import org.micro.chatserver.service.PubChemService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class PubChemController {

    private final PubChemService pubChemService;

    // Конструктор для ін'єкції залежності PubChemService
    public PubChemController(PubChemService pubChemService) {
        this.pubChemService = pubChemService;
    }

    // Обробляє GET-запити за URL "/data/pubchem/data".
    // Використовується для отримання даних про хімічну сполуку за її CID (Chemical Identifier)
    @GetMapping("/pubchem/data")
    public String getPubChemData(@RequestParam String cid) {
        // Викликає сервісний метод для отримання даних із PubChem API за вказаним CID
        return pubChemService.getCompoundDataByCID(cid);
    }
}
