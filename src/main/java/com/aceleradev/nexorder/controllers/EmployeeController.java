package com.aceleradev.nexorder.controllers;

import com.aceleradev.nexorder.dtos.employee.EmployeeCreateRequest;
import com.aceleradev.nexorder.dtos.employee.EmployeeResponse;
import com.aceleradev.nexorder.dtos.employee.EmployeeUpdateRequest;
import com.aceleradev.nexorder.entities.Employee;
import com.aceleradev.nexorder.mapper.EmployeeMapper;
import com.aceleradev.nexorder.services.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    private final EmployeeMapper mapper;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        List<EmployeeResponse> employees = service.findAll();
        return ResponseEntity.ok().body(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        EmployeeResponse employee = service.findById(id);
        return ResponseEntity.ok().body(employee);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = service.update(id, request);
        return ResponseEntity.ok(response);
    }
}
