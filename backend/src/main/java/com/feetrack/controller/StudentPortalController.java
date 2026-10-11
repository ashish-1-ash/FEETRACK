package com.feetrack.controller;

import com.feetrack.dto.StudentPortalDto;
import com.feetrack.model.FeeAllocation;
import com.feetrack.model.Payment;
import com.feetrack.model.Receipt;
import com.feetrack.service.StudentPortalService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    public StudentPortalController(StudentPortalService studentPortalService) {
        this.studentPortalService = studentPortalService;
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentPortalDto.StudentProfileDto> getProfile() {
        return ResponseEntity.ok(studentPortalService.getProfile());
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<StudentPortalDto.DashboardSummaryDto> getDashboardSummary(
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(studentPortalService.getDashboardSummary(academicYear));
    }

    @GetMapping("/fees")
    public ResponseEntity<List<FeeAllocation>> getFees(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String feeCategory,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(studentPortalService.getStudentFees(academicYear, feeCategory, status));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(studentPortalService.getStudentPayments(academicYear));
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> makePayment(@RequestBody StudentPortalDto.MakePaymentRequest request) {
        return ResponseEntity.ok(studentPortalService.makeStudentPayment(request));
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String paymentId) {
        return ResponseEntity.ok(studentPortalService.getPaymentById(paymentId));
    }

    @GetMapping("/payments/{paymentId}/receipt")
    public ResponseEntity<Receipt> getReceiptByPaymentId(@PathVariable String paymentId) {
        return ResponseEntity.ok(studentPortalService.getReceiptByPaymentId(paymentId));
    }

    @GetMapping("/fee-summary")
    public ResponseEntity<StudentPortalDto.FeeSummaryStatementDto> getFeeSummary(
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(studentPortalService.getFeeSummaryStatement(academicYear));
    }

    @GetMapping("/fee-summary/pdf")
    public ResponseEntity<byte[]> downloadFeeSummaryPdf(
            @RequestParam(required = false) String academicYear) {
        byte[] pdfBytes = studentPortalService.generateFeeSummaryPdf(academicYear);

        String safeYear = (academicYear != null && !academicYear.isBlank())
                ? academicYear.replaceAll("[^a-zA-Z0-9-]", "_")
                : "All-Years";
        String filename = "FeeTrack-Fee-Summary-" + safeYear + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
