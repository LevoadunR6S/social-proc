package org.micro.chatserver.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

@Service
public class TraccarService {

    //Отримуємо список пристроїв
    public ResponseEntity<?> getDevices(String jsessionid) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        // Встановлюємо cookie jsessionid для авторизації
        headers.add("Cookie", "JSESSIONID=" + jsessionid);
        // Вказуємо, що хочемо отримати JSON відповідь
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://demo3.traccar.org/api/devices",
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Помилка отримання даних");
        }
    }

    //Змінюємо параметри пристроя todo видалити
    public ResponseEntity<?> putDeviceInfo(String jsessionid) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + jsessionid);
        headers.setContentType(MediaType.APPLICATION_JSON); // додали Content-Type
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Створюємо JSON-тіло вручну або через ObjectMapper
        String jsonBody = """
        {
            "deviceId": 2823,
            "totalDistance": 764423,
            "hours": 3
        }
        """;

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        String url = "https://demo3.traccar.org/api/devices/2823/accumulators";

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Помилка отримання даних");
        }
    }

    //Отримуємо список подій пристроя
    public ResponseEntity<?> getEvents(String jsessionid) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + jsessionid);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://demo3.traccar.org/api/reports/events?deviceId=2823&from=2025-05-01T00:00:00Z&to=2025-05-25T23:59:59Z";

        ResponseEntity<?> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Помилка отримання даних");
        }
    }

    //Отримуємо місце розташування пристроя
    public ResponseEntity<?> getPositions(String jsessionid) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + jsessionid);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://demo3.traccar.org/api/positions";

        ResponseEntity<?> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Помилка отримання даних");
        }
    }
}