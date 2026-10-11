package com.feetrack.repository;

import com.feetrack.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByStudentIdNumberOrderByPaymentDateDesc(String studentIdNumber);
    List<Payment> findByStudentIdNumberAndAcademicYearOrderByPaymentDateDesc(String studentIdNumber, String academicYear);
    Optional<Payment> findByTransactionReference(String transactionReference);
    boolean existsByTransactionReference(String transactionReference);
}
