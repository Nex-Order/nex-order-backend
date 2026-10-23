package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.entities.Employee;
import com.aceleradev.nexorder.exception.InvalidCredentialsException;
import com.aceleradev.nexorder.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    public Employee authenticate(String email, String password) {

        Employee user = repository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user;
    }
}
