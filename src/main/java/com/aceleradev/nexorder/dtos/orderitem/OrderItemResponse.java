package com.aceleradev.nexorder.dtos.orderitem;

public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        Double price,
        Double subtotal
) {}