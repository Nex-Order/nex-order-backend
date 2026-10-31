package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.entities.Employee;
import com.aceleradev.nexorder.repositories.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository repository;

    private final PasswordEncoder encoder;

    public Employee authenticate(String email, String password) {

        Employee user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }
}