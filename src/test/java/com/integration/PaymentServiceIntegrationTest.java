package com.integration;


import com.dto.PaymentDto;
import com.entity.Payment;
import com.repository.PaymentRepository;
import com.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


class PaymentServiceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private PaymentServiceImpl paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanup() {
        paymentRepository.deleteAll();
    }

    @Test
    void createPayment() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setOrderId(1L);
        paymentDto.setUserId(1L);
        paymentDto.setPaymentAmount(100.0);
        paymentDto.setTimestamp(Date.valueOf("2025-02-02"));

        PaymentDto result = paymentService.createPayment(paymentDto);

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        Payment savedPayment = payments.get(0);
        assertThat(savedPayment.getOrderId()).isEqualTo(1L);
        assertThat(savedPayment.getUserId()).isEqualTo(1L);
        assertThat(savedPayment.getStatus()).isEqualTo("PENDING");

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Payment updatedPayment = paymentRepository.findById(String.valueOf(result.getId())).orElseThrow();
            assertThat(updatedPayment.getStatus()).isIn("SUCCESS", "FAILED");
        });
    }

    @Test
    void getAllPayments() {
        Payment payment1 = new Payment();
        payment1.setOrderId(1L);
        payment1.setUserId(1L);
        payment1.setPaymentAmount(100.0);
        payment1.setTimestamp(Date.valueOf("2025-02-02"));
        payment1.setStatus("SUCCESS");

        Payment payment2 = new Payment();
        payment2.setOrderId(2L);
        payment2.setUserId(2L);
        payment2.setPaymentAmount(200.0);
        payment2.setTimestamp(Date.valueOf("2025-02-02"));
        payment2.setStatus("FAILED");

        paymentRepository.saveAll(List.of(payment1, payment2));

        List<PaymentDto> result = paymentService.getAllPayments();

        assertThat(result).hasSize(2);
    }

    @Test
    void getPaymentsByOrderId() {
        Payment payment1 = new Payment();
        payment1.setOrderId(1L);
        payment1.setUserId(1L);
        payment1.setPaymentAmount(100.0);
        payment1.setTimestamp(Date.valueOf("2025-02-02"));
        payment1.setStatus("SUCCESS");

        Payment payment2 = new Payment();
        payment2.setOrderId(2L);
        payment2.setUserId(1L);
        payment2.setPaymentAmount(200.0);
        payment2.setTimestamp(Date.valueOf("2025-02-02"));
        payment2.setStatus("SUCCESS");

        paymentRepository.saveAll(List.of(payment1, payment2));

        List<PaymentDto> result = paymentService.getPaymentsByOrderId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
    }

    @Test
    void getPaymentsByStatus_ShouldReturnFilteredPayments() {
        Payment payment1 = new Payment();
        payment1.setOrderId(1L);
        payment1.setUserId(1L);
        payment1.setPaymentAmount(100.0);
        payment1.setTimestamp(Date.valueOf("2025-02-02"));
        payment1.setStatus("SUCCESS");

        Payment payment2 = new Payment();
        payment2.setOrderId(2L);
        payment2.setUserId(2L);
        payment2.setPaymentAmount(200.0);
        payment2.setTimestamp(Date.valueOf("2025-02-02"));
        payment2.setStatus("FAILED");

        paymentRepository.saveAll(List.of(payment1, payment2));

        List<PaymentDto> successPayments = paymentService.getPaymentsByStatus("SUCCESS");
        List<PaymentDto> failedPayments = paymentService.getPaymentsByStatus("FAILED");

        assertThat(successPayments).hasSize(1);
        assertThat(failedPayments).hasSize(1);
        assertThat(successPayments.get(0).getStatus()).isEqualTo("SUCCESS");
        assertThat(failedPayments.get(0).getStatus()).isEqualTo("FAILED");
    }
}