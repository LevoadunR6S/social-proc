package org.micro.shareable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.micro.shareable.model.Role;
import java.time.LocalDate;
import java.util.Set;


//Клас для передачі спрощеної моделі користувача між елементами додатку
@AllArgsConstructor
@Data
public class UserDto {
    protected String username;
    private String password;
    private String email;
    private LocalDate birthDate;
    private Set<Role> roles;


    public UserDto() {
    }


}
