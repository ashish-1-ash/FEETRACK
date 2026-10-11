package com.feetrack.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "receipts")
public class Receipt {

    @Id
    private String id;

    @Indexed(unique = true)
    private String receiptNumber; // e.g. "REC-2026-1049"

    private String paymentId;

    private String transactionReference;

    @Indexed
    private String studentIdNumber;

    private String studentName;

    private BigDecimal amount = BigDecimal.ZERO;

    private String paymentMethod;

    private String feeCategory;

    private String academicYear;

    private LocalDateTime issueDate;

    public Receipt() {
    }

    public Receipt(String id, String receiptNumber, String paymentId, String transactionReference, String studentIdNumber, String studentName, BigDecimal amount, String paymentMethod, String feeCategory, String academicYear, LocalDateTime issueDate) {
        this.id = id;
        this.receiptNumber = receiptNumber;
        this.paymentId = paymentId;
        this.transactionReference = transactionReference;
        this.studentIdNumber = studentIdNumber;
        this.studentName = studentName;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.paymentMethod = paymentMethod;
        this.feeCategory = feeCategory;
        this.academicYear = academicYear;
        this.issueDate = issueDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }
}
