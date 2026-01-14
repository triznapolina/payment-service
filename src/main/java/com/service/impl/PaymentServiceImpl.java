package com.service.impl;


import com.dto.PaymentDto;
import com.entity.Payment;
import com.kafka.PaymentKafkaProducer;
import com.mapper.PaymentMapper;
import com.repository.PaymentRepository;
import com.service.PaymentService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);


    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentKafkaProducer paymentProducer;

    private final WebClient webClient = WebClient.create();

    @Override
    public PaymentDto createPayment(PaymentDto paymentDto) {
        Payment payment = paymentMapper.toEntity(paymentDto);
        String status = generateStatusFromApi();
        payment.setStatus(status);
        payment.setTimestamp(new Date());
        Payment savedPayment = paymentRepository.save(payment);

        //paymentProducer.sendPaymentEvent(savedPayment);

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> getPaymentsForCurrentUserInDateRange(Integer userId, Date startDate, Date endDate) {
        return paymentRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate);
    }

    @Override
    public List<Payment> getPaymentsForAllUsersInDateRange(Date startDate, Date endDate) {
        return paymentRepository.findByTimestampBetween(startDate, endDate);
    }

    private String generateStatusFromApi() {
        try {
            String response = webClient.get()
                    .uri("https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            int randomNumber = Integer.parseInt(response.trim());
            logger.info("Generated random number: {}", randomNumber);
            return (randomNumber % 2 == 0) ? "SUCCESS" : "FAILED";
        } catch (Exception e) {
            logger.error("Error calling external API, defaulting to FAILED", e);
            return "FAILED";
        }
    }



}
