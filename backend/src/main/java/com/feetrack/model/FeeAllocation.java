package com.feetrack.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "fee_allocations")
public class FeeAllocation {

    @Id
    private String id;

    @Indexed
    private String studentIdNumber;

    private String academicYear; // e.g. "2026-2027"

    private String feeCategory; // e.g. "Tuition Fee", "Hostel Fee", "Examination Fee", "Library & Laboratory"

    private BigDecimal originalAmount = BigDecimal.ZERO;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal netObligation = BigDecimal.ZERO;

    private BigDecimal amountPaid = BigDecimal.ZERO;

    private BigDecimal remainingBalance = BigDecimal.ZERO;

    private String status; // "PAID", "PARTIALLY_PAID", "PENDING", "OVERDUE"

    private LocalDate dueDate;

    private List<Installment> installments = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FeeAllocation() {
    }

    public FeeAllocation(String id, String studentIdNumber, String academicYear, String feeCategory, BigDecimal originalAmount, BigDecimal discountAmount, BigDecimal netObligation, BigDecimal amountPaid, BigDecimal remainingBalance, String status, LocalDate dueDate, List<Installment> installments, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.studentIdNumber = studentIdNumber;
        this.academicYear = academicYear;
        this.feeCategory = feeCategory;
        this.originalAmount = originalAmount != null ? originalAmount : BigDecimal.ZERO;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.netObligation = netObligation != null ? netObligation : BigDecimal.ZERO;
        this.amountPaid = amountPaid != null ? amountPaid : BigDecimal.ZERO;
        this.remainingBalance = remainingBalance != null ? remainingBalance : BigDecimal.ZERO;
        this.status = status;
        this.dueDate = dueDate;
        this.installments = installments != null ? installments : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public void setStudentIdNumber(String studentIdNumber) {
        this.studentIdNumber = studentIdNumber;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getFeeCategory() {
        return feeCategory;
    }

    public void setFeeCategory(String feeCategory) {
        this.feeCategory = feeCategory;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getNetObligation() {
        return netObligation;
    }

    public void setNetObligation(BigDecimal netObligation) {
        this.netObligation = netObligation;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<Installment> getInstallments() {
        return installments;
    }

    public void setInstallments(List<Installment> installments) {
        this.installments = installments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
