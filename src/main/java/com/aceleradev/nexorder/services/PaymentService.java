package com.aceleradev.nexorder.services;

import com.aceleradev.nexorder.dtos.payment.PaymentDto;
import com.aceleradev.nexorder.commons.enums.OrderStatus;
import com.aceleradev.nexorder.entities.Customer;
import com.aceleradev.nexorder.entities.Order;
import com.aceleradev.nexorder.entities.OrderItem;
import com.aceleradev.nexorder.entities.Payment;
import com.aceleradev.nexorder.exception.BusinessRuleException;
import com.aceleradev.nexorder.exception.NotFoundException;
import com.aceleradev.nexorder.repositories.CustomerRepository;
import com.aceleradev.nexorder.repositories.OrderItemRepository;
import com.aceleradev.nexorder.repositories.OrderRepository;
import com.aceleradev.nexorder.repositories.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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
            throw new BusinessRuleException("Falha ao realizar pagamento");
        }
    }

    private Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Not Found"));
    }

    private Order getOrder(Long customerID) {
        List<Order> orderList = orderRepository.findAllByCustomer(getCustomer(customerID));
        return orderList.stream()
                .filter(it -> it.getOrderStatus() == OrderStatus.WAITING_PAYMENT)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Nenhum pedido em WAITING_PAYMENT"));
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
