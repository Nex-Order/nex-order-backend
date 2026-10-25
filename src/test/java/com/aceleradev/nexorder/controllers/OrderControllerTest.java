package com.aceleradev.nexorder.controllers;

import com.aceleradev.nexorder.dtos.order.OrderDTO;
import com.aceleradev.nexorder.dtos.order.OrderResponse;
import com.aceleradev.nexorder.repositories.EmployeeRepository;
import com.aceleradev.nexorder.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.aceleradev.nexorder.infra.security.TokenService;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @WithMockUser(roles = "EMPLOYEE")
    @Test
    void shouldCreateOrderAndReturnSafeResponse() throws Exception {
        OrderDTO request = new OrderDTO();

        OrderResponse response = new OrderResponse(
                1L,
                "WAITING_PAYMENT",
                null,
                10L,
                "Customer Test",
                20L,
                "Employee Test",
                List.of(),
                100.0
        );

        when(orderService.createOrderResponse(any(OrderDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(10))
                .andExpect(jsonPath("$.customerName").value("Customer Test"))
                .andExpect(jsonPath("$.employeeId").value(20))
                .andExpect(jsonPath("$.employeeName").value("Employee Test"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.baseSalary").doesNotExist())
                .andExpect(jsonPath("$.customer.password").doesNotExist())
                .andExpect(jsonPath("$.employee.password").doesNotExist());
    }
}