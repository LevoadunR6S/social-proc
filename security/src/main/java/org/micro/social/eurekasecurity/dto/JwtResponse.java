package org.micro.social.eurekasecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

//Клас для збереження пари токенів користувача
@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
}
