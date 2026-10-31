package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.dtos.payment.PaymentDto;
import com.aceleradev.nexorder.commons.enums.OrderStatus;
import com.aceleradev.nexorder.entities.Customer;
import com.aceleradev.nexorder.entities.Order;
import com.aceleradev.nexorder.entities.OrderItem;
import com.aceleradev.nexorder.entities.Payment;
import com.aceleradev.nexorder.repositories.CustomerRepository;
import com.aceleradev.nexorder.repositories.OrderItemRepository;
import com.aceleradev.nexorder.repositories.OrderRepository;
import com.aceleradev.nexorder.repositories.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;

    private final CustomerRepository customerRepository;

    private final OrderItemRepository orderItemRepository;

    private final PaymentRepository paymentRepository;

    public void makePayment(Long customerId, PaymentDto payment) {
        if (payment.getTotalValue().equals(getTotalValue(customerId))) {
            Payment paymentToSave = new Payment();

            Order order = getOrder(customerId);
            paymentToSave.setAmountPaid(payment.getTotalValue());
            paymentToSave.setPaymentMethod(payment.getPaymentMethod());
            paymentToSave.setOrder(order);

            order.setOrderStatus(OrderStatus.PAID);

            orderRepository.save(order);
            paymentRepository.save(paymentToSave);
        } else {
            new RuntimeException("Falha ao realizar pagamento");
        }
    }

    private Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Not Found"));
    }

    private Order getOrder(Long customerID) {
        List<Order> orderList = orderRepository.findAllByCustomer(getCustomer(customerID));
        return orderList.stream()
                .filter(it -> it.getOrderStatus() == OrderStatus.WAITING_PAYMENT)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nenhum pedido em WAITING_PAYMENT"));
    }

    private Double getTotalValue(Long customerID) {
        List<OrderItem> item = orderItemRepository.findAllByOrder(getOrder(customerID));
        Double totalValue = 0.0;
        for (int i = 0; i < item.size(); i++) {
            int quantity  = item.get(i).getQuantity();
            totalValue += item.get(i).getPrice()*quantity;
        }
        return totalValue;
    }
}
