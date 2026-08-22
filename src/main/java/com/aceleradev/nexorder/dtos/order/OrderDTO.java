package com.aceleradev.nexorder.dtos.order;

import com.aceleradev.nexorder.dtos.orderitem.OrderItemDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderDTO {

    private String orderStatus;
    private Long customerId;
    private Long employeeId;
    private List<OrderItemDTO> items = new ArrayList<>();
    private Double total;
}