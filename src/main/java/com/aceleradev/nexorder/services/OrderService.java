package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.dtos.order.OrderDTO;
import com.aceleradev.nexorder.dtos.orderitem.OrderItemDto;
import com.aceleradev.nexorder.commons.enums.OrderStatus;
import com.aceleradev.nexorder.entities.*;
import com.aceleradev.nexorder.exception.NotFoundException;
import com.aceleradev.nexorder.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Order createOrder(OrderDTO orderDTO) {
        Order orderEntity = new Order();
        OrderItem orderItemEntity = new OrderItem();

        Employee employee = employeeRepository.findById(orderDTO.getEmployee())
                .orElseThrow(() -> new NotFoundException("Not Found"));

        Customer customer = customerRepository.findById(orderDTO.getCustomer())
                .orElseThrow(() -> new NotFoundException("Not Found"));

        orderEntity.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        orderEntity.setEmployeeId(employee);
        orderEntity.setCustomerId(customer);

        orderRepository.save(orderEntity);

        for (OrderItemDto dto : orderDTO.getItems()) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new NotFoundException("Not Found"));

            orderItemEntity.setOrderId(orderEntity);
            orderItemEntity.setProduct(product);
            orderItemEntity.setQuantity(dto.getQuantity());
            orderItemEntity.setPrice(product.getPrice());
            orderItemRepository.save(orderItemEntity);
        }

        return orderEntity;
    }

    public OrderDTO getOrderById(Long id) {
        Order findOrder = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found"));
        OrderDTO orderDTO = new OrderDTO();
        OrderItemDto orderItemDto = new OrderItemDto();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(findOrder);

        orderDTO.setOrderStatus(findOrder.getOrderStatus().name());
        orderDTO.setCustomerId(findOrder.getCustomerId().getId());
        orderDTO.setEmployeeId(findOrder.getEmployeeId().getId());
        orderDTO.setTotal(Math.round(getTotalValue(findOrder.getId()) * 100.00) / 100.00);
        List<OrderItemDto> itemsList = orderItems.stream().map(item ->
                {
                    orderItemDto.setName(item.getProduct().getName());
                    orderItemDto.setQuantity(item.getQuantity());
                    orderItemDto.setProductId(item.getProduct().getId());

                    return orderItemDto;
                }
        ).toList();
        orderDTO.setItems(itemsList);

        return orderDTO;
    }

    private Double getTotalValue(long orderId) {
        Order findOrder = orderRepository.findById((long) orderId).orElseThrow(() -> new NotFoundException("Not Found"));
        List<OrderItem> item = orderItemRepository.findAllByOrder(findOrder);
        Double totalValue = 0.0;
        for (int i = 0; i < item.size(); i++) {
            int quantity  = item.get(i).getQuantity();
            totalValue += item.get(i).getPrice()*quantity;
        }
        return totalValue;
    }

}
