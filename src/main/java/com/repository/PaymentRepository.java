package com.repository;


import com.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment,String> {

    List<Payment> findByOrderId(String orderId);

    List<Payment> findByUserId(String userId);

    List<Payment> findByStatus(String status);

    List<Payment> findByUserIdAndTimestampBetween(Integer userId, Date startDate, Date endDate);

    List<Payment> findByTimestampBetween(Date startDate, Date endDate);

}
