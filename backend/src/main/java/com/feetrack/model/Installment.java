package com.feetrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Installment {

    private int installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status; // "PAID", "PARTIALLY_PAID", "PENDING", "OVERDUE"
    private BigDecimal amountPaid;
    private LocalDate paidDate;

    public Installment() {
        this.amount = BigDecimal.ZERO;
        this.amountPaid = BigDecimal.ZERO;
    }

    public Installment(int installmentNumber, BigDecimal amount, LocalDate dueDate, String status, BigDecimal amountPaid, LocalDate paidDate) {
        this.installmentNumber = installmentNumber;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.dueDate = dueDate;
        this.status = status;
        this.amountPaid = amountPaid != null ? amountPaid : BigDecimal.ZERO;
        this.paidDate = paidDate;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }
}
