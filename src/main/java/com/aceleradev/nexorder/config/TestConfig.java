package com.aceleradev.nexorder.config;

import com.aceleradev.nexorder.commons.enums.OrderStatus;
import com.aceleradev.nexorder.commons.enums.PaymentMethod;
import com.aceleradev.nexorder.commons.enums.ProductCategory;
import com.aceleradev.nexorder.entities.*;
import com.aceleradev.nexorder.commons.enums.Role;
import com.aceleradev.nexorder.repositories.*;
import com.aceleradev.nexorder.entities.Order;
import com.aceleradev.nexorder.entities.OrderItem;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;


@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestConfig implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception{
        Customer c1 = new Customer(null, "PC The One", "senha123", true, "12982228898",
                "pctheone@email.com", "00000000001", "O Melhorzin que tÃ¡ tendo", null);

        Employee e1 = new Employee(null, "Martins Luis", passwordEncoder.encode("senha123"), true, "61982456789",
                "martinsluis@email.com","00000000001", "Garoto Prodigio", null,
                Role.MANAGER, 6500.00);

        Product mouse = new Product(
                null,
                "Mouse Logitech MX",
                299.90,
                ProductCategory.GAMES
        );

        Order o1 = new Order(null, ZonedDateTime.now(), OrderStatus.PAID, c1, e1);

        OrderItem oi1 = new OrderItem(o1, mouse, 2, mouse.getPrice());

        customerRepository.save(c1);
        employeeRepository.save(e1);
        productRepository.save(mouse);
        orderRepository.save(o1);

        Payment pay1 = new Payment(null, ZonedDateTime.now(), 299.90, PaymentMethod.PIX, null);

        o1.addPayment(pay1);

        orderRepository.save(o1);

    }
}
