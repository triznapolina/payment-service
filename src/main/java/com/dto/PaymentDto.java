package com.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {

    private Long id;

    @NotNull(message = "Order ID cannot be blank")
    private Long orderId;

    @NotNull(message = "User ID cannot be blank")
    private Long userId;

    private String status;

    @NotNull(message = "Timestamp cannot be null")
    private Date timestamp;

    @NotNull(message = "Payment amount cannot be null")
    @Positive(message = "Payment amount must be positive")
    private Double paymentAmount;

}
