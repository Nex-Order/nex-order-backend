package com.aceleradev.nexorder.dtos.order;

import com.aceleradev.nexorder.dtos.orderitem.OrderItemResponse;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderStatus,
        ZonedDateTime moment,
        Long customerId,
        String customerName,
        Long employeeId,
        String employeeName,
        List<OrderItemResponse> items,
        Double total
) {}