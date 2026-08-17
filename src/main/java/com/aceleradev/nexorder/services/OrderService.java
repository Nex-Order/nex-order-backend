package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.dtos.order.OrderDTO;
import com.aceleradev.nexorder.dtos.orderitem.OrderItemDto;
import com.aceleradev.nexorder.commons.enums.OrderStatus;
import com.aceleradev.nexorder.entities.*;
import com.aceleradev.nexorder.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    public EmployeeRepository employeeRepository;

    @Autowired
    public ProductRepository productRepository;

    @Transactional
    public Order createOrder(OrderDTO orderDTO) {

        Employee employee = employeeRepository.findById(orderDTO.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Not Found"));

        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Not Found"));

        Order orderEntity = new Order();
        orderEntity.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        orderEntity.setEmployee(employee);
        orderEntity.setCustomer(customer);
        orderEntity.setMoment(ZonedDateTime.now());

        orderRepository.save(orderEntity);

        for (OrderItemDto dto : orderDTO.getItems()) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            OrderItem item = new OrderItem(
                    orderEntity,
                    product,
                    dto.getQuantity(),
                    product.getPrice()
            );

            orderItemRepository.save(item);
        }

        return orderEntity;
    }

    public OrderDTO getOrderById(Long id) {
        Order findOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not Found"));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(findOrder);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderStatus(findOrder.getOrderStatus().name());
        orderDTO.setCustomerId(findOrder.getCustomer().getId());
        orderDTO.setEmployeeId(findOrder.getEmployee().getId());
        orderDTO.setTotal(Math.round(getTotalValue(findOrder.getId()) * 100.00) / 100.00);

        List<OrderItemDto> itemsList = orderItems.stream().
                map(item -> {
                    OrderItemDto dto = new OrderItemDto();
                    dto.setName(item.getProduct().getName());
                    dto.setQuantity(item.getQuantity());
                    dto.setProductId(item.getProduct().getId());

                    return dto;
                })
                .toList();

        orderDTO.setItems(itemsList);

        return orderDTO;
    }

    private double getTotalValue(long orderId) {
        Order findOrder = orderRepository.findById(orderId).
                orElseThrow(() -> new EntityNotFoundException("Order not found"));

        List<OrderItem> item = orderItemRepository.findAllByOrder(findOrder);

        double totalValue = 0.0;

        for (OrderItem orderItem : item) {
            totalValue += orderItem.getPrice() * orderItem.getQuantity();
        }
        return totalValue;
    }
}