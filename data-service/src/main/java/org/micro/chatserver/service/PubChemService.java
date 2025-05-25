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

    public String getCompoundImageByCID(String cid) {
        // Формуємо URL для отримання властивостей сполуки за її CID
        String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + cid +
                "/JSON";

        // Виконуємо GET-запит до PubChem і отримуємо дані у форматі JSON
        return restTemplate.getForObject(url, String.class);
    }

    public String getBioactivityByCID() {
        // Формуємо URL для отримання властивостей сполуки за її CID
        //https://pubchem.ncbi.nlm.nih.gov/rest/pug/substance/sid/104234342/assaysummary/XML
        String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/assay/aid/1000/summary/XML";

        // Виконуємо GET-запит до PubChem і отримуємо дані у форматі JSON
        return restTemplate.getForObject(url, String.class);
    }

    public String getCompoundSimilarity() {
        /*
        Як визначають подібність за SMILES?
        Перетворення SMILES у "фінгерпрінти" — бінарні вектори, що кодують структурні елементи молекули (зв’язки, кільця, функціональні групи).
        Порівняння фінгерпрінтів — обчислення метрики подібності, наприклад, коефіцієнта Танімото (Tanimoto coefficient), який показує, наскільки багато спільних структурних елементів мають дві молекули.
        Якщо два SMILES-коди мають високий коефіцієнт подібності, значить, їхні молекули структурно близькі.
        */
        String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/fastsimilarity_2d/smiles/C1=NC2=C(N1)C(=O)N=C(N2)N/cids/XML?Threshold=95&MaxRecords=100";

        // Виконуємо GET-запит до PubChem і отримуємо дані у форматі JSON
        return restTemplate.getForObject(url, String.class);
    }


}
