package com.aceleradev.nexorder.dtos.orderitem;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemDto {
    private Long productId;
    private int quantity;
    private String name;

}
