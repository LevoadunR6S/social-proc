package org.micro.shareable.model;

import jakarta.persistence.*;
import lombok.Data;


//Клас, який є моделлю ролі користувача в межах додатку
@Entity
@Data
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    @Column
    private String name;

    public Role(String name) {
        this.name = name;
    }

    public Role() {
    }
}
