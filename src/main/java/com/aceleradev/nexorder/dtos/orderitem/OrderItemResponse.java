package com.aceleradev.nexorder.dtos.order;

public record OrderItemResponseDTO(
        Long productId,
        String productName,
        Integer quantity,
        Double price,
        Double subtotal
) {}