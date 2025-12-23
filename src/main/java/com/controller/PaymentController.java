package com.controller;


import com.dto.PaymentDto;
import com.entity.Payment;
import com.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public List<PaymentDto> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @PostMapping
    public PaymentDto createPayment(@Valid @RequestBody PaymentDto paymentDto) {
        return paymentService.createPayment(paymentDto);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentDto> getPaymentsByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentDto> getPaymentsByUserId(@PathVariable Long userId) {
        return paymentService.getPaymentsByUserId(userId);
    }

    @GetMapping("/status/{status}")
    public List<PaymentDto> getPaymentsByStatus(@PathVariable String status) {
        return paymentService.getPaymentsByStatus(status);
    }

    @GetMapping("/user-range")
    public ResponseEntity<List<Payment>> getPaymentsForCurrentUserInDateRange(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        List<Payment> payments = paymentService.getPaymentsForCurrentUserInDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(payments);
    }


    @GetMapping("/all-users-range")
    public ResponseEntity<List<Payment>> getPaymentsForAllUsersInDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        List<Payment> payments = paymentService.getPaymentsForAllUsersInDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }
}
