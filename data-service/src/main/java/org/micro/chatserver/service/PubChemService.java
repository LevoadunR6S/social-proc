package org.micro.chatserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

//Клас для роботи з PubChem API
@Service
public class PubChemService {

    private final RestTemplate restTemplate;

    public PubChemService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Метод для отримання даних про сполуку за її CID
    public String getCompoundDataByCID(String cid) {
        // Формуємо URL для отримання властивостей сполуки за її CID
        String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + cid +
                //Перелік даних, які хочемо отримати
                "/property/Title,MolecularWeight,MolecularFormula,IsomericSMILES,ExactMass,Complexity,Charge,CovalentUnitCount/JSON";

        // Виконуємо GET-запит до PubChem і отримуємо дані у форматі JSON
        return restTemplate.getForObject(url, String.class);
    }
}
