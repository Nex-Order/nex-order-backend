package com.aceleradev.nexorder.controllers;

import com.aceleradev.nexorder.dtos.customer.CustomerCreateRequest;
import com.aceleradev.nexorder.dtos.customer.CustomerResponse;
import com.aceleradev.nexorder.dtos.customer.CustomerUpdateRequest;
import com.aceleradev.nexorder.entities.Customer;
import com.aceleradev.nexorder.mapper.CustomerMapper;
import com.aceleradev.nexorder.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    private final CustomerMapper mapper;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerCreateRequest request) {
        Customer customer = service.create(request);
        CustomerResponse response = mapper.toResponse(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll(){
        List<CustomerResponse> customers = service.findAll();
        return ResponseEntity.ok().body(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id){
        CustomerResponse customer = service.findById(id);
        return ResponseEntity.ok().body(customer);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @RequestBody CustomerUpdateRequest request) {
        Customer customer = service.update(id, request);
        CustomerResponse response = mapper.toResponse(customer);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
