package com.feetrack.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionReference; // e.g. "TXN-2026-9021"

    @Indexed
    private String studentIdNumber;

    private String feeAllocationId;

    private String feeCategory;

    private String academicYear;

    private BigDecimal amount = BigDecimal.ZERO;

    private String paymentMethod; // "UPI", "Net Banking", "Debit Card", "Credit Card", "Bank Demand Draft"

    private String status; // "SUCCESS", "PENDING", "FAILED"

    private LocalDateTime paymentDate;

    private String receiptReference;

    private String notes;

    public Payment() {
    }

    public Payment(String id, String transactionReference, String studentIdNumber, String feeAllocationId, String feeCategory, String academicYear, BigDecimal amount, String paymentMethod, String status, LocalDateTime paymentDate, String receiptReference, String notes) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.studentIdNumber = studentIdNumber;
        this.feeAllocationId = feeAllocationId;
        this.feeCategory = feeCategory;
        this.academicYear = academicYear;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentDate = paymentDate;
        this.receiptReference = receiptReference;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public void setStudentIdNumber(String studentIdNumber) {
        this.studentIdNumber = studentIdNumber;
    }

    public String getFeeAllocationId() {
        return feeAllocationId;
    }

    public void setFeeAllocationId(String feeAllocationId) {
        this.feeAllocationId = feeAllocationId;
    }

    public String getFeeCategory() {
        return feeCategory;
    }

    public void setFeeCategory(String feeCategory) {
        this.feeCategory = feeCategory;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public void setReceiptReference(String receiptReference) {
        this.receiptReference = receiptReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
