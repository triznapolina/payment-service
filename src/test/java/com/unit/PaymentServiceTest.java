package com.unit;


import com.dto.PaymentDto;
import com.entity.Payment;
import com.kafka.PaymentKafkaProducer;
import com.mapper.PaymentMapper;
import com.repository.PaymentRepository;
import com.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentKafkaProducer paymentProducer;


    private PaymentDto paymentDto;
    private Payment payment;


    @BeforeEach
    void setUp() {
        paymentDto = new PaymentDto();
        paymentDto.setOrderId(1L);
        paymentDto.setUserId(1L);
        paymentDto.setPaymentAmount(100.0);
        paymentDto.setTimestamp(Date.valueOf("2025-02-02"));

        payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setPaymentAmount(100.0);
        payment.setTimestamp(Date.valueOf("2025-02-02"));
        payment.setStatus("PENDING");
    }

    @Test
    void createPayment() {

        PaymentDto inputDto = new PaymentDto(123L, 1L, 101L, "SUCCESS", null, 100.0);
        Payment paymentEntity = new Payment(123L, 1L, 101L, "SUCCESS", null, 100.0);
        Payment savedPaymentEntity = new Payment(123L, 1L,  201L, "SUCCESS", null, 100.0);
        PaymentDto outputDto = new PaymentDto(123L, 1L, 101L,  "SUCCESS", null, 100.0);

        when(paymentMapper.toEntity(inputDto)).thenReturn(paymentEntity);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPaymentEntity);
        when(paymentMapper.toDto(savedPaymentEntity)).thenReturn(outputDto);

        PaymentDto result = paymentService.createPayment(inputDto);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        verify(paymentMapper).toEntity(inputDto);
        verify(paymentRepository).save(paymentEntity);

        verify(paymentProducer).sendPaymentEvent(savedPaymentEntity);
        verify(paymentMapper).toDto(savedPaymentEntity);
    }


    @Test
    void getAllPayments() {
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findAll()).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        List<PaymentDto> result = paymentService.getAllPayments();

        assertThat(result).hasSize(1);
        verify(paymentRepository, times(1)).findAll();
        verify(paymentMapper, times(1)).toDto(payment);
    }

    @Test
    void getPaymentsByOrderId() {
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByOrderId(1L)).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        List<PaymentDto> result = paymentService.getPaymentsByOrderId(1L);

        assertThat(result).hasSize(1);
        verify(paymentRepository, times(1)).findByOrderId(1L);
    }

    @Test
    void getPaymentsByUserId() {
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByUserId(1L)).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        List<PaymentDto> result = paymentService.getPaymentsByUserId(1L);

        assertThat(result).hasSize(1);
        verify(paymentRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getPaymentsByStatus() {
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByStatus("PENDING")).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        List<PaymentDto> result = paymentService.getPaymentsByStatus("PENDING");

        assertThat(result).hasSize(1);
        verify(paymentRepository, times(1)).findByStatus("PENDING");
    }

    @Test
    void findPaymentById() {
        when(paymentRepository.findById("1L")).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentRepository.findById("1L");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }


}
