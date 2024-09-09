package org.micro.social.eurekasecurity.dto;

import lombok.Data;


//Клас який представляє собою об'єкт, необхідний для входу користувача
@Data
public class JwtRequest {
    private String username;
    private String password;
}
