package org.micro.social.eurekasecurity.dto;

import lombok.Data;
import java.time.LocalDate;

//Клас, який представляє користувача на етапі реєстрації
@Data
public class RegistrationUserDto {
    protected String username;
    private String email;
    private String password;
    private String confirmPassword;
    private LocalDate birthDate;
}
