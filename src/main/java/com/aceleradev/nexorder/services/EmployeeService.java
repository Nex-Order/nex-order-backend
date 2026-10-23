package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.dtos.employee.EmployeeCreateRequest;
import com.aceleradev.nexorder.dtos.employee.EmployeeResponse;
import com.aceleradev.nexorder.dtos.employee.EmployeeUpdateRequest;
import com.aceleradev.nexorder.entities.Employee;
import com.aceleradev.nexorder.exception.NotFoundException;
import com.aceleradev.nexorder.mapper.EmployeeMapper;
import com.aceleradev.nexorder.repositories.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmployeeMapper employeeMapper;

    public List<EmployeeResponse> findAll() {
        return employeeMapper.toResponseList(repository.findAll());
    }

    public EmployeeResponse findById(Long id) {
        Employee employee = repository.findById(id).
                orElseThrow(() -> new NotFoundException("Employee not found."));

        return employeeMapper.toResponse(employee);
    }

    public EmployeeResponse create(EmployeeCreateRequest request) {
        Employee employee = employeeMapper.toEntity(request);
        employee.setPassword(encoder.encode(request.password()));

        repository.save(employee);

        return employeeMapper.toResponse(employee);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)){
            throw new NotFoundException("Employee not found.");
        }

        repository.deleteById(id);
    }

    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found."));

        employeeMapper.updateEmployeeFromDto(request, employee);

        repository.save(employee);

        return employeeMapper.toResponse(employee);

    }
}
