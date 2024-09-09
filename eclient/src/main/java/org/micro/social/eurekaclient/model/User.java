package org.micro.social.eurekaclient.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.micro.shareable.model.Post;
import org.micro.shareable.model.Role;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ігнорує специфічні для Hibernate властивості при серіалізації в JSON
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotBlank
    protected String username;

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = false, unique = true)
    private String password;

    @NotNull
    @DateTimeFormat(pattern = "YYYY-MM-dd")
    @Past(message = "invalid date") // Перевіряє, що дата є в минулому
    private LocalDate birthDate;


    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER) //Дані завантажуються відразу
    @JoinTable(
            name = "user_roles", // Назва таблиці зв'язку
            joinColumns = @JoinColumn(name = "user_id"), // Стовпець, що посилається на цей клас
            inverseJoinColumns = @JoinColumn(name = "role_id") // Стовпець, що посилається на інший клас
    )
    private Collection<Role> roles;

    // Конструктор для ініціалізації користувача без ідентифікатора
    public User(String username, String email, String password, LocalDate birthDate, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.roles = roles;
    }
}
