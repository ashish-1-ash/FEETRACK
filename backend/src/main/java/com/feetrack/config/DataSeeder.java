package com.feetrack.config;

import com.feetrack.model.*;
import com.feetrack.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds initial demo data: admin user, sample student, fee allocations, payments, receipts.
 * Only runs when collections are empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FeeAllocationRepository feeAllocationRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      StudentRepository studentRepository,
                      FeeAllocationRepository feeAllocationRepository,
                      PaymentRepository paymentRepository,
                      ReceiptRepository receiptRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.feeAllocationRepository = feeAllocationRepository;
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("[DataSeeder] Data already exists, skipping seeding.");
            return;
        }

        System.out.println("[DataSeeder] Seeding initial data...");

        // 1. Create Admin User
        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@feetrack.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setPhone("9000000001");
        adminUser.setRole(Role.ROLE_ADMIN);
        adminUser.setStatus("ACTIVE");
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(adminUser);
        System.out.println("[DataSeeder] Admin user created: admin@feetrack.com / admin123");

        // 2. Create Demo Student User
        User studentUser = new User();
        studentUser.setName("Ashish Kumar");
        studentUser.setEmail("ashish@student.edu");
        studentUser.setPassword(passwordEncoder.encode("student123"));
        studentUser.setPhone("9876543210");
        studentUser.setRole(Role.ROLE_STUDENT);
        studentUser.setStatus("ACTIVE");
        studentUser.setStudentProfileId("STU-2026-001");
        studentUser.setCreatedAt(LocalDateTime.now());
        studentUser.setUpdatedAt(LocalDateTime.now());
        User savedStudentUser = userRepository.save(studentUser);

        // 3. Create Demo Student Profile
        Student student = new Student();
        student.setStudentIdNumber("STU-2026-001");
        student.setFullName("Ashish Kumar");
        student.setEmail("ashish@student.edu");
        student.setPhone("9876543210");
        student.setCourse("B.Tech Computer Science & Engineering");
        student.setAcademicYear("2026-2027");
        student.setCurrentSemester("Semester 5");
        student.setStatus("ACTIVE");
        student.setLinkedUserId(savedStudentUser.getId());
        student.setEnrollmentDate(LocalDate.of(2024, 7, 15));
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);
        System.out.println("[DataSeeder] Demo student created: ashish@student.edu / student123");

        // 4. Create Fee Allocations
        // a) Tuition Fee
        FeeAllocation tuitionFee = new FeeAllocation();
        tuitionFee.setStudentIdNumber("STU-2026-001");
        tuitionFee.setAcademicYear("2026-2027");
        tuitionFee.setFeeCategory("Tuition Fee");
        tuitionFee.setOriginalAmount(new BigDecimal("120000"));
        tuitionFee.setDiscountAmount(new BigDecimal("10000"));
        tuitionFee.setNetObligation(new BigDecimal("110000"));
        tuitionFee.setAmountPaid(new BigDecimal("55000"));
        tuitionFee.setRemainingBalance(new BigDecimal("55000"));
        tuitionFee.setStatus("PARTIALLY_PAID");
        tuitionFee.setDueDate(LocalDate.of(2027, 1, 15));

        List<Installment> tuitionInstallments = new ArrayList<>();

        Installment i1 = new Installment();
        i1.setInstallmentNumber(1);
        i1.setAmount(new BigDecimal("55000"));
        i1.setDueDate(LocalDate.of(2026, 7, 31));
        i1.setStatus("PAID");
        i1.setAmountPaid(new BigDecimal("55000"));
        i1.setPaidDate(LocalDate.of(2026, 7, 25));
        tuitionInstallments.add(i1);

        Installment i2 = new Installment();
        i2.setInstallmentNumber(2);
        i2.setAmount(new BigDecimal("55000"));
        i2.setDueDate(LocalDate.of(2027, 1, 15));
        i2.setStatus("PENDING");
        i2.setAmountPaid(BigDecimal.ZERO);
        tuitionInstallments.add(i2);

        tuitionFee.setInstallments(tuitionInstallments);
        tuitionFee.setCreatedAt(LocalDateTime.now());
        tuitionFee.setUpdatedAt(LocalDateTime.now());
        feeAllocationRepository.save(tuitionFee);

        // b) Examination Fee
        FeeAllocation examFee = new FeeAllocation();
        examFee.setStudentIdNumber("STU-2026-001");
        examFee.setAcademicYear("2026-2027");
        examFee.setFeeCategory("Examination Fee");
        examFee.setOriginalAmount(new BigDecimal("5000"));
        examFee.setDiscountAmount(BigDecimal.ZERO);
        examFee.setNetObligation(new BigDecimal("5000"));
        examFee.setAmountPaid(new BigDecimal("5000"));
        examFee.setRemainingBalance(BigDecimal.ZERO);
        examFee.setStatus("PAID");
        examFee.setDueDate(LocalDate.of(2026, 8, 15));
        examFee.setCreatedAt(LocalDateTime.now());
        examFee.setUpdatedAt(LocalDateTime.now());
        feeAllocationRepository.save(examFee);

        // c) Library & Laboratory — OVERDUE
        FeeAllocation libFee = new FeeAllocation();
        libFee.setStudentIdNumber("STU-2026-001");
        libFee.setAcademicYear("2026-2027");
        libFee.setFeeCategory("Library & Laboratory");
        libFee.setOriginalAmount(new BigDecimal("8000"));
        libFee.setDiscountAmount(BigDecimal.ZERO);
        libFee.setNetObligation(new BigDecimal("8000"));
        libFee.setAmountPaid(BigDecimal.ZERO);
        libFee.setRemainingBalance(new BigDecimal("8000"));
        libFee.setStatus("OVERDUE");
        libFee.setDueDate(LocalDate.of(2026, 9, 30));
        libFee.setCreatedAt(LocalDateTime.now());
        libFee.setUpdatedAt(LocalDateTime.now());
        feeAllocationRepository.save(libFee);

        // d) Hostel Fee — upcoming
        FeeAllocation hostelFee = new FeeAllocation();
        hostelFee.setStudentIdNumber("STU-2026-001");
        hostelFee.setAcademicYear("2026-2027");
        hostelFee.setFeeCategory("Hostel Fee");
        hostelFee.setOriginalAmount(new BigDecimal("36000"));
        hostelFee.setDiscountAmount(new BigDecimal("2000"));
        hostelFee.setNetObligation(new BigDecimal("34000"));
        hostelFee.setAmountPaid(BigDecimal.ZERO);
        hostelFee.setRemainingBalance(new BigDecimal("34000"));
        hostelFee.setStatus("PENDING");
        hostelFee.setDueDate(LocalDate.now().plusDays(20));
        hostelFee.setCreatedAt(LocalDateTime.now());
        hostelFee.setUpdatedAt(LocalDateTime.now());
        feeAllocationRepository.save(hostelFee);

        // 5. Create Payment Records
        Payment p1 = new Payment();
        p1.setTransactionReference("TXN-20260725-001");
        p1.setStudentIdNumber("STU-2026-001");
        p1.setFeeCategory("Tuition Fee");
        p1.setAcademicYear("2026-2027");
        p1.setAmount(new BigDecimal("55000"));
        p1.setPaymentMethod("UPI");
        p1.setStatus("SUCCESS");
        p1.setPaymentDate(LocalDateTime.of(2026, 7, 25, 14, 30));
        p1.setReceiptReference("REC-10001");
        p1.setNotes("1st instalment tuition fee");
        paymentRepository.save(p1);

        Payment p2 = new Payment();
        p2.setTransactionReference("TXN-20260815-002");
        p2.setStudentIdNumber("STU-2026-001");
        p2.setFeeCategory("Examination Fee");
        p2.setAcademicYear("2026-2027");
        p2.setAmount(new BigDecimal("5000"));
        p2.setPaymentMethod("Net Banking");
        p2.setStatus("SUCCESS");
        p2.setPaymentDate(LocalDateTime.of(2026, 8, 12, 10, 15));
        p2.setReceiptReference("REC-10002");
        p2.setNotes("Examination fee full payment");
        paymentRepository.save(p2);

        // 6. Create Receipt Records
        Receipt r1 = new Receipt();
        r1.setReceiptNumber("REC-10001");
        r1.setPaymentId(p1.getId());
        r1.setTransactionReference("TXN-20260725-001");
        r1.setStudentIdNumber("STU-2026-001");
        r1.setStudentName("Ashish Kumar");
        r1.setAmount(new BigDecimal("55000"));
        r1.setPaymentMethod("UPI");
        r1.setFeeCategory("Tuition Fee");
        r1.setAcademicYear("2026-2027");
        r1.setIssueDate(LocalDateTime.of(2026, 7, 25, 14, 31));
        receiptRepository.save(r1);

        Receipt r2 = new Receipt();
        r2.setReceiptNumber("REC-10002");
        r2.setPaymentId(p2.getId());
        r2.setTransactionReference("TXN-20260815-002");
        r2.setStudentIdNumber("STU-2026-001");
        r2.setStudentName("Ashish Kumar");
        r2.setAmount(new BigDecimal("5000"));
        r2.setPaymentMethod("Net Banking");
        r2.setFeeCategory("Examination Fee");
        r2.setAcademicYear("2026-2027");
        r2.setIssueDate(LocalDateTime.of(2026, 8, 12, 10, 16));
        receiptRepository.save(r2);

        System.out.println("[DataSeeder] ✅ Seed data created successfully!");
        System.out.println("[DataSeeder] ---");
        System.out.println("[DataSeeder] Admin login:   admin@feetrack.com / admin123");
        System.out.println("[DataSeeder] Student login:  ashish@student.edu / student123");
        System.out.println("[DataSeeder] ---");
    }
}
