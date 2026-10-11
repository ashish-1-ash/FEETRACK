package com.feetrack.repository;

import com.feetrack.model.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends MongoRepository<Receipt, String> {
    List<Receipt> findByStudentIdNumberOrderByIssueDateDesc(String studentIdNumber);
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
    Optional<Receipt> findByPaymentId(String paymentId);
}
