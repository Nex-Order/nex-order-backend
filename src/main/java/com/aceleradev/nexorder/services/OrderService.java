package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.dtos.order.OrderDTO;
import com.aceleradev.nexorder.dtos.order.OrderResponse;
import com.aceleradev.nexorder.dtos.orderitem.OrderItemDTO;
import com.aceleradev.nexorder.commons.enums.OrderStatus;
import com.aceleradev.nexorder.dtos.orderitem.OrderItemResponse;
import com.aceleradev.nexorder.entities.*;
import com.aceleradev.nexorder.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final CustomerRepository customerRepository;

    private final EmployeeRepository employeeRepository;

    private final ProductRepository productRepository;

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

        for (OrderItemDTO dto : orderDTO.getItems()) {
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

        List<OrderItemDTO> itemsList = orderItems.stream().
                map(item -> {
                    OrderItemDTO dto = new OrderItemDTO();
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

    public OrderResponse getOrderResponseById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Order not found"));

        List<OrderItem> items = orderItemRepository.findAllByOrder(order);

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice() * item.getQuantity()
                ))
                .toList();

        double total = itemResponses.stream()
                .mapToDouble(OrderItemResponse::subtotal)
                .sum();

        return new OrderResponse(
                order.getId(),
                order.getOrderStatus().name(),
                order.getMoment(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getEmployee().getId(),
                order.getEmployee().getName(),
                itemResponses,
                total
        );
    }

    public OrderResponse createOrderResponse(OrderDTO orderDTO) {
        Order orderEntity = createOrder(orderDTO);
        return getOrderResponseById(orderEntity.getId());
    }
}