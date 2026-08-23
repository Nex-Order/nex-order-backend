package com.aceleradev.nexorder.dtos.orderitem;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemDTO {
    private Long productId;
    private int quantity;
    private String name;

}
