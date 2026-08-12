package com.aceleradev.nexorder.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tb_costumer")
public class Customer extends User {

    public Customer(){
        super();
    }

    public Customer(
            Long id,
            String name,
            String password,
            Boolean active,
            String phoneNumber,
            String email,
            String identifier,
            String description,
            Instant lastLogin
    ){
        super(id, name, password, active, phoneNumber, email, identifier, description, lastLogin);
    }

}
