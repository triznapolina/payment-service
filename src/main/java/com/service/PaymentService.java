package com.service;

import com.dto.PaymentDto;
import com.entity.Payment;

import java.util.Date;
import java.util.List;

public interface PaymentService {


    PaymentDto createPayment(PaymentDto paymentDto);

    List<PaymentDto> getAllPayments();

    List<PaymentDto> getPaymentsByOrderId(String orderId) ;


    List<PaymentDto> getPaymentsByUserId(String userId);

    List<PaymentDto> getPaymentsByStatus(String status);


    List<Payment> getPaymentsForCurrentUserInDateRange(Integer userId, Date startDate, Date endDate);


    List<Payment> getPaymentsForAllUsersInDateRange(Date startDate, Date endDate) ;


}
