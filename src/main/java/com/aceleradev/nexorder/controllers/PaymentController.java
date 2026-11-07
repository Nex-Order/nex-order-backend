package com.aceleradev.nexorder.controllers;

import com.aceleradev.nexorder.dtos.payment.PaymentDto;
import com.aceleradev.nexorder.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ResponseEntity makePayment(
            @RequestHeader Long customerId,
            @RequestBody PaymentDto dto
    ) {
        service.makePayment(customerId, dto);
        return ResponseEntity.ok().build();
    }
}
