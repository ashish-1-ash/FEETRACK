package com.feetrack.service;

import com.feetrack.exception.BadRequestException;
import com.feetrack.exception.ResourceNotFoundException;
import com.feetrack.model.*;
import com.feetrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FeeAllocationRepository feeAllocationRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;

    public AdminService(UserRepository userRepository,
                        StudentRepository studentRepository,
                        FeeAllocationRepository feeAllocationRepository,
                        PaymentRepository paymentRepository,
                        ReceiptRepository receiptRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.feeAllocationRepository = feeAllocationRepository;
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
    }

    // --- Students ---
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    @Transactional
    public Student approveStudentAccount(String studentId) {
        Student student = studentRepository.findByStudentIdNumber(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        student.setStatus("ACTIVE");
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        if (student.getLinkedUserId() != null) {
            userRepository.findById(student.getLinkedUserId()).ifPresent(u -> {
                u.setStatus("ACTIVE");
                u.setUpdatedAt(LocalDateTime.now());
                userRepository.save(u);
            });
        }
        return student;
    }

    @Transactional
    public Student updateStudent(String id, Student updatedData) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        if (updatedData.getFullName() != null) student.setFullName(updatedData.getFullName());
        if (updatedData.getCourse() != null) student.setCourse(updatedData.getCourse());
        if (updatedData.getAcademicYear() != null) student.setAcademicYear(updatedData.getAcademicYear());
        if (updatedData.getCurrentSemester() != null) student.setCurrentSemester(updatedData.getCurrentSemester());
        if (updatedData.getStatus() != null) student.setStatus(updatedData.getStatus());
        if (updatedData.getPhone() != null) student.setPhone(updatedData.getPhone());
        student.setUpdatedAt(LocalDateTime.now());
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(String id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
        studentRepository.deleteById(id);
    }

    // --- Fee Allocations ---
    public List<FeeAllocation> getAllFeeAllocations() {
        return feeAllocationRepository.findAll();
    }

    public List<FeeAllocation> getFeeAllocationsForStudent(String studentIdNumber) {
        return feeAllocationRepository.findByStudentIdNumber(studentIdNumber);
    }

    @Transactional
    public FeeAllocation createFeeAllocation(FeeAllocation allocation) {
        if (allocation.getStudentIdNumber() == null || allocation.getStudentIdNumber().isBlank()) {
            throw new BadRequestException("Student ID is required for fee allocation");
        }
        if (!studentRepository.existsByStudentIdNumber(allocation.getStudentIdNumber())) {
            throw new ResourceNotFoundException("No student found with ID: " + allocation.getStudentIdNumber());
        }

        if (allocation.getOriginalAmount() == null) allocation.setOriginalAmount(BigDecimal.ZERO);
        if (allocation.getDiscountAmount() == null) allocation.setDiscountAmount(BigDecimal.ZERO);
        BigDecimal net = allocation.getOriginalAmount().subtract(allocation.getDiscountAmount()).max(BigDecimal.ZERO);
        allocation.setNetObligation(net);
        allocation.setAmountPaid(BigDecimal.ZERO);
        allocation.setRemainingBalance(net);
        allocation.setStatus("PENDING");
        allocation.setCreatedAt(LocalDateTime.now());
        allocation.setUpdatedAt(LocalDateTime.now());

        return feeAllocationRepository.save(allocation);
    }

    @Transactional
    public FeeAllocation updateFeeAllocation(String id, FeeAllocation updated) {
        FeeAllocation existing = feeAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee allocation not found: " + id));

        if (updated.getFeeCategory() != null) existing.setFeeCategory(updated.getFeeCategory());
        if (updated.getOriginalAmount() != null) existing.setOriginalAmount(updated.getOriginalAmount());
        if (updated.getDiscountAmount() != null) existing.setDiscountAmount(updated.getDiscountAmount());
        if (updated.getDueDate() != null) existing.setDueDate(updated.getDueDate());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getInstallments() != null) existing.setInstallments(updated.getInstallments());
        if (updated.getAcademicYear() != null) existing.setAcademicYear(updated.getAcademicYear());

        // Recalculate net and balance
        BigDecimal net = existing.getOriginalAmount().subtract(existing.getDiscountAmount()).max(BigDecimal.ZERO);
        existing.setNetObligation(net);
        existing.setRemainingBalance(net.subtract(existing.getAmountPaid()).max(BigDecimal.ZERO));
        existing.setUpdatedAt(LocalDateTime.now());

        return feeAllocationRepository.save(existing);
    }

    @Transactional
    public void deleteFeeAllocation(String id) {
        if (!feeAllocationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fee allocation not found: " + id);
        }
        feeAllocationRepository.deleteById(id);
    }

    // --- Payments ---
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsForStudent(String studentIdNumber) {
        return paymentRepository.findByStudentIdNumberOrderByPaymentDateDesc(studentIdNumber);
    }

    @Transactional
    public Payment recordPayment(Payment payment) {
        if (payment.getTransactionReference() != null
                && paymentRepository.existsByTransactionReference(payment.getTransactionReference())) {
            throw new BadRequestException("Duplicate transaction reference: " + payment.getTransactionReference());
        }
        if (payment.getTransactionReference() == null || payment.getTransactionReference().isBlank()) {
            payment.setTransactionReference("ADM-TXN-" + System.currentTimeMillis() % 100000000);
        }
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        return paymentRepository.save(payment);
    }

    @Transactional
    public void deletePayment(String id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found: " + id);
        }
        paymentRepository.deleteById(id);
    }

    // --- Dashboard Stats ---
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalAllocations", feeAllocationRepository.count());
        stats.put("totalPayments", paymentRepository.count());

        List<Payment> allPayments = paymentRepository.findAll();
        BigDecimal totalCollected = allPayments.stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalFeesCollected", totalCollected);

        List<FeeAllocation> allAllocations = feeAllocationRepository.findAll();
        BigDecimal totalOutstanding = allAllocations.stream()
                .map(f -> f.getRemainingBalance() != null ? f.getRemainingBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalOutstanding", totalOutstanding);

        long pendingApprovals = studentRepository.findAll().stream()
                .filter(s -> "PENDING_VERIFICATION".equalsIgnoreCase(s.getStatus()))
                .count();
        stats.put("pendingEnrollmentApprovals", pendingApprovals);

        return stats;
    }
}
