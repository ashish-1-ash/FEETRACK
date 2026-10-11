package com.feetrack.controller;

import com.feetrack.model.FeeAllocation;
import com.feetrack.model.Payment;
import com.feetrack.model.Student;
import com.feetrack.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // --- Dashboard ---
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // --- Students ---
    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getStudentById(id));
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> updateStudent(@PathVariable String id, @RequestBody Student updatedData) {
        return ResponseEntity.ok(adminService.updateStudent(id, updatedData));
    }

    @PostMapping("/students/{studentIdNumber}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Student> approveStudent(@PathVariable String studentIdNumber) {
        return ResponseEntity.ok(adminService.approveStudentAccount(studentIdNumber));
    }

    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable String id) {
        adminService.deleteStudent(id);
        return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
    }

    // --- Fee Allocations ---
    @GetMapping("/fee-allocations")
    public ResponseEntity<List<FeeAllocation>> getAllFeeAllocations() {
        return ResponseEntity.ok(adminService.getAllFeeAllocations());
    }

    @GetMapping("/fee-allocations/student/{studentIdNumber}")
    public ResponseEntity<List<FeeAllocation>> getFeeAllocationsForStudent(@PathVariable String studentIdNumber) {
        return ResponseEntity.ok(adminService.getFeeAllocationsForStudent(studentIdNumber));
    }

    @PostMapping("/fee-allocations")
    public ResponseEntity<FeeAllocation> createFeeAllocation(@RequestBody FeeAllocation allocation) {
        return ResponseEntity.ok(adminService.createFeeAllocation(allocation));
    }

    @PutMapping("/fee-allocations/{id}")
    public ResponseEntity<FeeAllocation> updateFeeAllocation(@PathVariable String id, @RequestBody FeeAllocation updated) {
        return ResponseEntity.ok(adminService.updateFeeAllocation(id, updated));
    }

    @DeleteMapping("/fee-allocations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteFeeAllocation(@PathVariable String id) {
        adminService.deleteFeeAllocation(id);
        return ResponseEntity.ok(Map.of("message", "Fee allocation deleted successfully"));
    }

    // --- Payments ---
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @GetMapping("/payments/student/{studentIdNumber}")
    public ResponseEntity<List<Payment>> getPaymentsForStudent(@PathVariable String studentIdNumber) {
        return ResponseEntity.ok(adminService.getPaymentsForStudent(studentIdNumber));
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> recordPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(adminService.recordPayment(payment));
    }

    @DeleteMapping("/payments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePayment(@PathVariable String id) {
        adminService.deletePayment(id);
        return ResponseEntity.ok(Map.of("message", "Payment deleted successfully"));
    }
}
