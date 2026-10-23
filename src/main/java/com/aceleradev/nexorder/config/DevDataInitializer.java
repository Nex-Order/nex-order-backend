package com.aceleradev.nexorder.config;

import com.aceleradev.nexorder.commons.enums.Role;
import com.aceleradev.nexorder.entities.Employee;
import com.aceleradev.nexorder.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private final EmployeeRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repository.findByEmail("dev@nexorder.com").isEmpty()) {
            Employee employee = new Employee();

            employee.setName("Dev NexOrder");
            employee.setEmail("dev@nexorder.com");
            employee.setPassword(encoder.encode("Dev@123456"));
            employee.setRole(Role.MANAGER);

            repository.save(employee);
        }
    }
}