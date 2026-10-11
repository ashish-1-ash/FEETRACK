package com.feetrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StudentPortalDto {

    public static class StudentProfileDto {
        private String id;
        private String studentIdNumber;
        private String fullName;
        private String email;
        private String phone;
        private String course;
        private String academicYear;
        private String currentSemester;
        private String status;
        private LocalDate enrollmentDate;

        public StudentProfileDto() {}

        public StudentProfileDto(String id, String studentIdNumber, String fullName, String email, String phone, String course, String academicYear, String currentSemester, String status, LocalDate enrollmentDate) {
            this.id = id;
            this.studentIdNumber = studentIdNumber;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.course = course;
            this.academicYear = academicYear;
            this.currentSemester = currentSemester;
            this.status = status;
            this.enrollmentDate = enrollmentDate;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getStudentIdNumber() { return studentIdNumber; }
        public void setStudentIdNumber(String studentIdNumber) { this.studentIdNumber = studentIdNumber; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
        public String getCurrentSemester() { return currentSemester; }
        public void setCurrentSemester(String currentSemester) { this.currentSemester = currentSemester; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDate getEnrollmentDate() { return enrollmentDate; }
        public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    }

    public static class DashboardSummaryDto {
        private String studentName;
        private String studentIdNumber;
        private String course;
        private String academicYear;
        private String accountStatus;

        private BigDecimal totalFees = BigDecimal.ZERO;
        private BigDecimal totalDiscounts = BigDecimal.ZERO;
        private BigDecimal netObligation = BigDecimal.ZERO;
        private BigDecimal totalPaid = BigDecimal.ZERO;
        private BigDecimal outstandingBalance = BigDecimal.ZERO;
        private BigDecimal upcomingFees = BigDecimal.ZERO; // Next 30 days
        private BigDecimal overdueFees = BigDecimal.ZERO;
        private BigDecimal paymentProgress = BigDecimal.ZERO; // Percentage

        private List<UpcomingInstallmentDto> upcomingInstallments;
        private List<RecentPaymentDto> recentPayments;

        public DashboardSummaryDto() {}

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getStudentIdNumber() { return studentIdNumber; }
        public void setStudentIdNumber(String studentIdNumber) { this.studentIdNumber = studentIdNumber; }
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
        public String getAccountStatus() { return accountStatus; }
        public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
        public BigDecimal getTotalFees() { return totalFees; }
        public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }
        public BigDecimal getTotalDiscounts() { return totalDiscounts; }
        public void setTotalDiscounts(BigDecimal totalDiscounts) { this.totalDiscounts = totalDiscounts; }
        public BigDecimal getNetObligation() { return netObligation; }
        public void setNetObligation(BigDecimal netObligation) { this.netObligation = netObligation; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
        public BigDecimal getOutstandingBalance() { return outstandingBalance; }
        public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
        public BigDecimal getUpcomingFees() { return upcomingFees; }
        public void setUpcomingFees(BigDecimal upcomingFees) { this.upcomingFees = upcomingFees; }
        public BigDecimal getOverdueFees() { return overdueFees; }
        public void setOverdueFees(BigDecimal overdueFees) { this.overdueFees = overdueFees; }
        public BigDecimal getPaymentProgress() { return paymentProgress; }
        public void setPaymentProgress(BigDecimal paymentProgress) { this.paymentProgress = paymentProgress; }
        public List<UpcomingInstallmentDto> getUpcomingInstallments() { return upcomingInstallments; }
        public void setUpcomingInstallments(List<UpcomingInstallmentDto> upcomingInstallments) { this.upcomingInstallments = upcomingInstallments; }
        public List<RecentPaymentDto> getRecentPayments() { return recentPayments; }
        public void setRecentPayments(List<RecentPaymentDto> recentPayments) { this.recentPayments = recentPayments; }
    }

    public static class UpcomingInstallmentDto {
        private String feeCategory;
        private int installmentNumber;
        private BigDecimal amount;
        private LocalDate dueDate;
        private String status;

        public UpcomingInstallmentDto() {}
        public UpcomingInstallmentDto(String feeCategory, int installmentNumber, BigDecimal amount, LocalDate dueDate, String status) {
            this.feeCategory = feeCategory;
            this.installmentNumber = installmentNumber;
            this.amount = amount;
            this.dueDate = dueDate;
            this.status = status;
        }

        public String getFeeCategory() { return feeCategory; }
        public void setFeeCategory(String feeCategory) { this.feeCategory = feeCategory; }
        public int getInstallmentNumber() { return installmentNumber; }
        public void setInstallmentNumber(int installmentNumber) { this.installmentNumber = installmentNumber; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class RecentPaymentDto {
        private String id;
        private String transactionReference;
        private String feeCategory;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private LocalDateTime paymentDate;
        private String receiptReference;

        public RecentPaymentDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        public String getFeeCategory() { return feeCategory; }
        public void setFeeCategory(String feeCategory) { this.feeCategory = feeCategory; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
        public String getReceiptReference() { return receiptReference; }
        public void setReceiptReference(String receiptReference) { this.receiptReference = receiptReference; }
    }

    public static class FeeSummaryStatementDto {
        private String institutionName;
        private String institutionAddress;
        private String institutionContact;
        private String studentFullName;
        private String studentIdNumber;
        private String course;
        private String academicYear;
        private LocalDateTime reportGeneratedAt;

        private BigDecimal totalAssignedFees;
        private BigDecimal totalDiscounts;
        private BigDecimal netObligation;
        private BigDecimal totalPaid;
        private BigDecimal outstandingBalance;
        private BigDecimal upcomingFees;
        private BigDecimal overdueFees;
        private BigDecimal paymentProgress;

        private List<com.feetrack.model.FeeAllocation> feeAllocations;
        private List<com.feetrack.model.Payment> paymentHistory;

        public FeeSummaryStatementDto() {}

        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
        public String getInstitutionAddress() { return institutionAddress; }
        public void setInstitutionAddress(String institutionAddress) { this.institutionAddress = institutionAddress; }
        public String getInstitutionContact() { return institutionContact; }
        public void setInstitutionContact(String institutionContact) { this.institutionContact = institutionContact; }
        public String getStudentFullName() { return studentFullName; }
        public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }
        public String getStudentIdNumber() { return studentIdNumber; }
        public void setStudentIdNumber(String studentIdNumber) { this.studentIdNumber = studentIdNumber; }
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
        public LocalDateTime getReportGeneratedAt() { return reportGeneratedAt; }
        public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) { this.reportGeneratedAt = reportGeneratedAt; }
        public BigDecimal getTotalAssignedFees() { return totalAssignedFees; }
        public void setTotalAssignedFees(BigDecimal totalAssignedFees) { this.totalAssignedFees = totalAssignedFees; }
        public BigDecimal getTotalDiscounts() { return totalDiscounts; }
        public void setTotalDiscounts(BigDecimal totalDiscounts) { this.totalDiscounts = totalDiscounts; }
        public BigDecimal getNetObligation() { return netObligation; }
        public void setNetObligation(BigDecimal netObligation) { this.netObligation = netObligation; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
        public BigDecimal getOutstandingBalance() { return outstandingBalance; }
        public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
        public BigDecimal getUpcomingFees() { return upcomingFees; }
        public void setUpcomingFees(BigDecimal upcomingFees) { this.upcomingFees = upcomingFees; }
        public BigDecimal getOverdueFees() { return overdueFees; }
        public void setOverdueFees(BigDecimal overdueFees) { this.overdueFees = overdueFees; }
        public BigDecimal getPaymentProgress() { return paymentProgress; }
        public void setPaymentProgress(BigDecimal paymentProgress) { this.paymentProgress = paymentProgress; }
        public List<com.feetrack.model.FeeAllocation> getFeeAllocations() { return feeAllocations; }
        public void setFeeAllocations(List<com.feetrack.model.FeeAllocation> feeAllocations) { this.feeAllocations = feeAllocations; }
        public List<com.feetrack.model.Payment> getPaymentHistory() { return paymentHistory; }
        public void setPaymentHistory(List<com.feetrack.model.Payment> paymentHistory) { this.paymentHistory = paymentHistory; }
    }

    public static class MakePaymentRequest {
        private String feeAllocationId;
        private BigDecimal amount;
        private String paymentMethod;
        private String notes;

        public MakePaymentRequest() {}

        public String getFeeAllocationId() { return feeAllocationId; }
        public void setFeeAllocationId(String feeAllocationId) { this.feeAllocationId = feeAllocationId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
