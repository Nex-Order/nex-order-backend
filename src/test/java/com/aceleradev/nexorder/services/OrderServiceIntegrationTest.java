package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.commons.enums.ProductCategory;
import com.aceleradev.nexorder.commons.enums.Role;
import com.aceleradev.nexorder.dtos.order.OrderDTO;
import com.aceleradev.nexorder.dtos.orderitem.OrderItemDto;
import com.aceleradev.nexorder.entities.*;
import com.aceleradev.nexorder.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:nexorder-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer customer;
    private Employee employee;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        employeeRepository.deleteAll();

        customer = new Customer(
                null, "Test Customer", "password", true,
                "21999990000", "customer-test@example.com",
                "CUSTOMER-TEST", "Customer for integration test",
                Instant.now()
        );
        customer = customerRepository.save(customer);

        employee = new Employee(
                null, "Test Employee", "password", true,
                "21999990001", "employee-test@example.com",
                "EMPLOYEE-TEST", "Employee for integration test",
                Instant.now(), Role.values()[0], 3000.0
        );
        employee = employeeRepository.save(employee);

        product1 = createProduct("Product A", 10.0);
        product2 = createProduct("Product B", 20.0);
        product3 = createProduct("Product C", 30.0);
    }

    @Test
    void shouldCreateOrderWithThreeItems() {
        OrderDTO request = createOrderRequest(
                product1.getId(),
                product2.getId(),
                product3.getId()
        );

        Order createdOrder = orderService.createOrder(request);

        List<OrderItem> savedItems =
                orderItemRepository.findAllByOrder(createdOrder);

        assertNotNull(createdOrder.getId());
        assertEquals(3, savedItems.size());
        assertEquals(1, orderRepository.count());
        assertEquals(3, orderItemRepository.count());
    }

    @Test
    void shouldRollbackOrderWhenProductDoesNotExist() {
        OrderDTO request = createOrderRequest(
                product1.getId(),
                999999L
        );

        assertThrows(
                EntityNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(0, orderRepository.count());
        assertEquals(0, orderItemRepository.count());
    }

    private Product createProduct(String name, double price) {
        Product product = new Product(
                null, name, price, ProductCategory.values()[0]
        );
        return productRepository.save(product);
    }

    private OrderDTO createOrderRequest(Long... productIds) {
        OrderDTO request = new OrderDTO();
        request.setCustomerId(customer.getId());
        request.setEmployeeId(employee.getId());

        for (Long productId : productIds) {
            OrderItemDto item = new OrderItemDto();
            item.setProductId(productId);
            item.setQuantity(1);
            request.getItems().add(item);
        }

        return request;
    }
}