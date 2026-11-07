package com.aceleradev.nexorder.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Setter
@Getter
@MappedSuperclass
public abstract class User {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;
    private String name;
    private String password;
    private Boolean active;
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;
    private String identifier;
    private String description;
    private LocalDate createdAt;
    private Instant lastLogin;

    protected User(){}

    public User(Long id, String name, String password, Boolean active, String phoneNumber, String email, String identifier, String description, Instant lastLogin) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.active = active;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.identifier = identifier;
        this.description = description;
        this.lastLogin = lastLogin;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
