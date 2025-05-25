package org.micro.chatserver.controller;

import org.micro.chatserver.service.PubChemService;
import org.micro.chatserver.service.TraccarService;
import org.micro.shareable.response.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class PubChemController {

    @Autowired
    private PubChemService pubChemService;

    @Autowired
    private TraccarService traccarService;

    private final String jsessionId= "node0fwmjmw833c7f1px5yrj5udqq02460.node0";

    // Обробляє GET-запити за URL "/data/pubchem/data".
    // Використовується для отримання даних про хімічну сполуку за її CID (Chemical Identifier)
    @GetMapping("/pubchem/properties")
    public ResponseEntity<?> getPubChemData(@RequestParam String cid) {
        // Викликає сервісний метод для отримання даних із PubChem API за вказаним CID
        return ResponseHandler.responseBuilder(HttpStatus.OK,pubChemService.getCompoundDataByCID(cid),"data");
    }

    @GetMapping("/pubchem/image")
    public ResponseEntity<?> getPubChemImage(@RequestParam String cid) {
        // Викликає сервісний метод для отримання даних із PubChem API за вказаним CID
        return ResponseHandler.responseBuilder(HttpStatus.OK,pubChemService.getCompoundImageByCID(cid),"data");
    }

    @GetMapping("/pubchem/similarity")
    public ResponseEntity<?> getPubChemSim() {
        // Викликає сервісний метод для отримання даних із PubChem API за вказаним CID
        return ResponseHandler.responseBuilder(HttpStatus.OK,pubChemService.getCompoundSimilarity(),"data");
    }

    @GetMapping("/pubchem/bio")
    public ResponseEntity<?> getPubChemBio() {
        // Викликає сервісний метод для отримання даних із PubChem API за вказаним CID
        return ResponseHandler.responseBuilder(HttpStatus.OK,pubChemService.getBioactivityByCID(),"data");
    }

    @GetMapping("/devices")
    public ResponseEntity<?> getDevices() {
        return traccarService.getDevices(jsessionId);
    }

    @GetMapping("/devices/events")
    public ResponseEntity<?> getEvents() {
        return traccarService.getEvents(jsessionId);
    }

    @GetMapping("/devices/positions")
    public ResponseEntity<?> positions() {
        return traccarService.getPositions(jsessionId);
    }

    @PutMapping("/devices/changeData")
    public ResponseEntity<?> change() {
        return traccarService.putDeviceInfo(jsessionId);
    }


}
