package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.dtos.customer.CustomerCreateRequest;
import com.aceleradev.nexorder.dtos.customer.CustomerResponse;
import com.aceleradev.nexorder.dtos.customer.CustomerUpdateRequest;
import com.aceleradev.nexorder.entities.Customer;
import com.aceleradev.nexorder.exception.NotFoundException;
import com.aceleradev.nexorder.mapper.CustomerMapper;
import com.aceleradev.nexorder.repositories.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    public List<CustomerResponse> findAll() {
        // Converte uma lista de "Customer" em "CustomerResponse".
        return customerMapper.toResponseList(customerRepository.findAll());
    }

    public CustomerResponse findById(Long id) {

        // Verifica se o customer existe, se não, lança exceção.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found."));

        return customerMapper.toResponse(customer);
    }

    public Customer create(CustomerCreateRequest request) {

        // 1. Converte o DTO "request" para a entidade "customer" com o mapper.
        Customer customer = customerMapper.toEntity(request);

        // 2. Salva a entidade no banco.
        return customerRepository.save(customer);
    }

    public Customer update(Long id, CustomerUpdateRequest request) {

        // 1. Verifica se o id passado existe no banco.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found."));

        // 2. Se existir, atualiza com o mapper, se não, lança uma exceção.
        customerMapper.updateCustomerFromDto(request, customer);

        // 3. Salva a entidade atualizada.
        return customerRepository.save(customer);
    }

    public void delete(Long id) {

        if (!customerRepository.existsById(id)){
            throw new NotFoundException("Customer not found.");
        }

        customerRepository.deleteById(id);
    }

}
