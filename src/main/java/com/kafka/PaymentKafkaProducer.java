package com.kafka;


import com.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentKafkaProducer.class);

    private final KafkaTemplate<String, Payment> kafkaTemplate;

    public void sendPaymentEvent(Payment paymentEvent) {
        kafkaTemplate.send("create-payment", paymentEvent);
        logger.info("Payment event sent for payment: {}", paymentEvent);
    }


}
