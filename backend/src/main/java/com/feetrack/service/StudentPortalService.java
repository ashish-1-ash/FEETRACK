package com.feetrack.service;

import com.feetrack.dto.StudentPortalDto;
import com.feetrack.exception.BadRequestException;
import com.feetrack.exception.ResourceNotFoundException;
import com.feetrack.model.*;
import com.feetrack.repository.FeeAllocationRepository;
import com.feetrack.repository.PaymentRepository;
import com.feetrack.repository.ReceiptRepository;
import com.feetrack.repository.StudentRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentPortalService {

    private final StudentRepository studentRepository;
    private final FeeAllocationRepository feeAllocationRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final AuthService authService;

    @Value("${feetrack.institution.name:Apex Institute of Technology & Sciences}")
    private String institutionName;

    @Value("${feetrack.institution.address:Knowledge Park III, Institutional Area, Greater City}")
    private String institutionAddress;

    @Value("${feetrack.institution.contact:admissions@apexinstitute.edu.in | +91 98765 43210}")
    private String institutionContact;

    public StudentPortalService(StudentRepository studentRepository,
                                FeeAllocationRepository feeAllocationRepository,
                                PaymentRepository paymentRepository,
                                ReceiptRepository receiptRepository,
                                AuthService authService) {
        this.studentRepository = studentRepository;
        this.feeAllocationRepository = feeAllocationRepository;
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.authService = authService;
    }

    public Student getAuthenticatedStudent() {
        User user = authService.getAuthenticatedUser();
        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new BadRequestException("Access denied: Not a registered student account");
        }

        // Try resolving by linkedUserId
        return studentRepository.findByLinkedUserId(user.getId())
                .or(() -> studentRepository.findByEmailIgnoreCase(user.getEmail()))
                .or(() -> user.getStudentProfileId() != null
                        ? studentRepository.findByStudentIdNumber(user.getStudentProfileId())
                        : java.util.Optional.empty())
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
    }

    public StudentPortalDto.StudentProfileDto getProfile() {
        Student s = getAuthenticatedStudent();
        return new StudentPortalDto.StudentProfileDto(
                s.getId(),
                s.getStudentIdNumber(),
                s.getFullName(),
                s.getEmail(),
                s.getPhone(),
                s.getCourse(),
                s.getAcademicYear(),
                s.getCurrentSemester(),
                s.getStatus(),
                s.getEnrollmentDate()
        );
    }

    public StudentPortalDto.DashboardSummaryDto getDashboardSummary(String academicYear) {
        Student student = getAuthenticatedStudent();
        String studentId = student.getStudentIdNumber();

        List<FeeAllocation> allocations = (academicYear != null && !academicYear.isBlank())
                ? feeAllocationRepository.findByStudentIdNumberAndAcademicYear(studentId, academicYear.trim())
                : feeAllocationRepository.findByStudentIdNumber(studentId);

        List<Payment> allPayments = (academicYear != null && !academicYear.isBlank())
                ? paymentRepository.findByStudentIdNumberAndAcademicYearOrderByPaymentDateDesc(studentId, academicYear.trim())
                : paymentRepository.findByStudentIdNumberOrderByPaymentDateDesc(studentId);

        BigDecimal totalFees = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        BigDecimal netObligation = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal outstandingBalance = BigDecimal.ZERO;
        BigDecimal upcomingFees = BigDecimal.ZERO;
        BigDecimal overdueFees = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        LocalDate in30Days = now.plusDays(30);

        List<StudentPortalDto.UpcomingInstallmentDto> upcomingInstallments = new ArrayList<>();

        for (FeeAllocation fa : allocations) {
            BigDecimal orig = fa.getOriginalAmount() != null ? fa.getOriginalAmount() : BigDecimal.ZERO;
            BigDecimal disc = fa.getDiscountAmount() != null ? fa.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal net = fa.getNetObligation() != null ? fa.getNetObligation() : orig.subtract(disc);
            BigDecimal paid = fa.getAmountPaid() != null ? fa.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal bal = fa.getRemainingBalance() != null ? fa.getRemainingBalance() : net.subtract(paid);

            totalFees = totalFees.add(orig);
            totalDiscounts = totalDiscounts.add(disc);
            netObligation = netObligation.add(net);
            totalPaid = totalPaid.add(paid);
            outstandingBalance = outstandingBalance.add(bal.max(BigDecimal.ZERO));

            // Check installments
            if (fa.getInstallments() != null) {
                for (Installment inst : fa.getInstallments()) {
                    BigDecimal instAmount = inst.getAmount() != null ? inst.getAmount() : BigDecimal.ZERO;
                    BigDecimal instPaid = inst.getAmountPaid() != null ? inst.getAmountPaid() : BigDecimal.ZERO;
                    BigDecimal unpaid = instAmount.subtract(instPaid).max(BigDecimal.ZERO);

                    if (unpaid.compareTo(BigDecimal.ZERO) > 0 && inst.getDueDate() != null) {
                        if (inst.getDueDate().isBefore(now)) {
                            overdueFees = overdueFees.add(unpaid);
                        } else if (!inst.getDueDate().isAfter(in30Days)) {
                            upcomingFees = upcomingFees.add(unpaid);
                            upcomingInstallments.add(new StudentPortalDto.UpcomingInstallmentDto(
                                    fa.getFeeCategory(),
                                    inst.getInstallmentNumber(),
                                    unpaid,
                                    inst.getDueDate(),
                                    inst.getStatus() != null ? inst.getStatus() : "PENDING"
                            ));
                        }
                    }
                }
            } else if (fa.getDueDate() != null) {
                // If no installments, check allocation level due date
                if (bal.compareTo(BigDecimal.ZERO) > 0) {
                    if (fa.getDueDate().isBefore(now)) {
                        overdueFees = overdueFees.add(bal);
                    } else if (!fa.getDueDate().isAfter(in30Days)) {
                        upcomingFees = upcomingFees.add(bal);
                        upcomingInstallments.add(new StudentPortalDto.UpcomingInstallmentDto(
                                fa.getFeeCategory(),
                                1,
                                bal,
                                fa.getDueDate(),
                                fa.getStatus()
                        ));
                    }
                }
            }
        }

        // Calculate progress percentage safely (prevent division by zero)
        BigDecimal paymentProgress = BigDecimal.ZERO;
        if (netObligation.compareTo(BigDecimal.ZERO) > 0) {
            paymentProgress = totalPaid
                    .multiply(BigDecimal.valueOf(100))
                    .divide(netObligation, 2, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(100));
        }

        // Recent successful or pending payments (top 5)
        List<StudentPortalDto.RecentPaymentDto> recentPayments = allPayments.stream()
                .limit(5)
                .map(p -> {
                    StudentPortalDto.RecentPaymentDto rp = new StudentPortalDto.RecentPaymentDto();
                    rp.setId(p.getId());
                    rp.setTransactionReference(p.getTransactionReference());
                    rp.setFeeCategory(p.getFeeCategory());
                    rp.setAmount(p.getAmount());
                    rp.setPaymentMethod(p.getPaymentMethod());
                    rp.setStatus(p.getStatus());
                    rp.setPaymentDate(p.getPaymentDate());
                    rp.setReceiptReference(p.getReceiptReference());
                    return rp;
                })
                .collect(Collectors.toList());

        StudentPortalDto.DashboardSummaryDto summary = new StudentPortalDto.DashboardSummaryDto();
        summary.setStudentName(student.getFullName());
        summary.setStudentIdNumber(student.getStudentIdNumber());
        summary.setCourse(student.getCourse());
        summary.setAcademicYear(academicYear != null ? academicYear : student.getAcademicYear());
        summary.setAccountStatus(student.getStatus());
        summary.setTotalFees(totalFees);
        summary.setTotalDiscounts(totalDiscounts);
        summary.setNetObligation(netObligation);
        summary.setTotalPaid(totalPaid);
        summary.setOutstandingBalance(outstandingBalance);
        summary.setUpcomingFees(upcomingFees);
        summary.setOverdueFees(overdueFees);
        summary.setPaymentProgress(paymentProgress);
        summary.setUpcomingInstallments(upcomingInstallments);
        summary.setRecentPayments(recentPayments);

        return summary;
    }

    public List<FeeAllocation> getStudentFees(String academicYear, String category, String status) {
        Student student = getAuthenticatedStudent();
        List<FeeAllocation> list = (academicYear != null && !academicYear.isBlank())
                ? feeAllocationRepository.findByStudentIdNumberAndAcademicYear(student.getStudentIdNumber(), academicYear.trim())
                : feeAllocationRepository.findByStudentIdNumberOrderByDueDateAsc(student.getStudentIdNumber());

        return list.stream()
                .filter(f -> category == null || category.isBlank() || f.getFeeCategory().equalsIgnoreCase(category.trim()))
                .filter(f -> status == null || status.isBlank() || f.getStatus().equalsIgnoreCase(status.trim()))
                .collect(Collectors.toList());
    }

    public List<Payment> getStudentPayments(String academicYear) {
        Student student = getAuthenticatedStudent();
        return (academicYear != null && !academicYear.isBlank())
                ? paymentRepository.findByStudentIdNumberAndAcademicYearOrderByPaymentDateDesc(student.getStudentIdNumber(), academicYear.trim())
                : paymentRepository.findByStudentIdNumberOrderByPaymentDateDesc(student.getStudentIdNumber());
    }

    public Payment getPaymentById(String paymentId) {
        Student student = getAuthenticatedStudent();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (!payment.getStudentIdNumber().equalsIgnoreCase(student.getStudentIdNumber())) {
            throw new BadRequestException("Access denied: You do not own this payment record");
        }
        return payment;
    }

    public Receipt getReceiptByPaymentId(String paymentId) {
        Student student = getAuthenticatedStudent();
        Payment payment = getPaymentById(paymentId);

        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new BadRequestException("Receipts are only available for successfully verified payments");
        }

        return receiptRepository.findByPaymentId(payment.getId())
                .or(() -> receiptRepository.findByReceiptNumber(payment.getReceiptReference()))
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for payment: " + paymentId));
    }

    @Transactional
    public Payment makeStudentPayment(StudentPortalDto.MakePaymentRequest request) {
        Student student = getAuthenticatedStudent();

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        FeeAllocation allocation = feeAllocationRepository.findById(request.getFeeAllocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee allocation not found"));

        if (!allocation.getStudentIdNumber().equalsIgnoreCase(student.getStudentIdNumber())) {
            throw new BadRequestException("Access denied: You do not own this fee obligation");
        }

        if (allocation.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("This fee allocation is already fully settled");
        }

        BigDecimal payAmount = request.getAmount().min(allocation.getRemainingBalance());

        String txnRef = "TXN-" + System.currentTimeMillis() % 100000000;
        String recRef = "REC-" + (10000 + (int)(Math.random() * 90000));

        Payment payment = new Payment();
        payment.setTransactionReference(txnRef);
        payment.setStudentIdNumber(student.getStudentIdNumber());
        payment.setFeeAllocationId(allocation.getId());
        payment.setFeeCategory(allocation.getFeeCategory());
        payment.setAcademicYear(allocation.getAcademicYear());
        payment.setAmount(payAmount);
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI");
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setReceiptReference(recRef);
        payment.setNotes(request.getNotes() != null ? request.getNotes() : "Student Portal Online Payment");

        Payment savedPayment = paymentRepository.save(payment);

        // Update fee allocation
        BigDecimal newPaid = allocation.getAmountPaid().add(payAmount);
        BigDecimal newBalance = allocation.getNetObligation().subtract(newPaid).max(BigDecimal.ZERO);
        allocation.setAmountPaid(newPaid);
        allocation.setRemainingBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            allocation.setStatus("PAID");
        } else {
            allocation.setStatus("PARTIALLY_PAID");
        }

        // Apply payment across installments if present
        if (allocation.getInstallments() != null) {
            BigDecimal remainingToApply = payAmount;
            for (Installment inst : allocation.getInstallments()) {
                if (remainingToApply.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal instUnpaid = inst.getAmount().subtract(inst.getAmountPaid()).max(BigDecimal.ZERO);
                if (instUnpaid.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal applyToInst = remainingToApply.min(instUnpaid);
                    inst.setAmountPaid(inst.getAmountPaid().add(applyToInst));
                    remainingToApply = remainingToApply.subtract(applyToInst);
                    if (inst.getAmountPaid().compareTo(inst.getAmount()) >= 0) {
                        inst.setStatus("PAID");
                        inst.setPaidDate(LocalDate.now());
                    } else {
                        inst.setStatus("PARTIALLY_PAID");
                    }
                }
            }
        }
        allocation.setUpdatedAt(LocalDateTime.now());
        feeAllocationRepository.save(allocation);

        // Create receipt document
        Receipt receipt = new Receipt();
        receipt.setReceiptNumber(recRef);
        receipt.setPaymentId(savedPayment.getId());
        receipt.setTransactionReference(txnRef);
        receipt.setStudentIdNumber(student.getStudentIdNumber());
        receipt.setStudentName(student.getFullName());
        receipt.setAmount(payAmount);
        receipt.setPaymentMethod(payment.getPaymentMethod());
        receipt.setFeeCategory(allocation.getFeeCategory());
        receipt.setAcademicYear(allocation.getAcademicYear());
        receipt.setIssueDate(LocalDateTime.now());
        receiptRepository.save(receipt);

        return savedPayment;
    }

    public StudentPortalDto.FeeSummaryStatementDto getFeeSummaryStatement(String academicYear) {
        Student student = getAuthenticatedStudent();
        StudentPortalDto.DashboardSummaryDto summary = getDashboardSummary(academicYear);

        List<FeeAllocation> allocations = (academicYear != null && !academicYear.isBlank())
                ? feeAllocationRepository.findByStudentIdNumberAndAcademicYear(student.getStudentIdNumber(), academicYear.trim())
                : feeAllocationRepository.findByStudentIdNumber(student.getStudentIdNumber());

        List<Payment> payments = (academicYear != null && !academicYear.isBlank())
                ? paymentRepository.findByStudentIdNumberAndAcademicYearOrderByPaymentDateDesc(student.getStudentIdNumber(), academicYear.trim())
                : paymentRepository.findByStudentIdNumberOrderByPaymentDateDesc(student.getStudentIdNumber());

        StudentPortalDto.FeeSummaryStatementDto dto = new StudentPortalDto.FeeSummaryStatementDto();
        dto.setInstitutionName(institutionName);
        dto.setInstitutionAddress(institutionAddress);
        dto.setInstitutionContact(institutionContact);
        dto.setStudentFullName(student.getFullName());
        dto.setStudentIdNumber(student.getStudentIdNumber());
        dto.setCourse(student.getCourse());
        dto.setAcademicYear(academicYear != null ? academicYear : student.getAcademicYear());
        dto.setReportGeneratedAt(LocalDateTime.now());

        dto.setTotalAssignedFees(summary.getTotalFees());
        dto.setTotalDiscounts(summary.getTotalDiscounts());
        dto.setNetObligation(summary.getNetObligation());
        dto.setTotalPaid(summary.getTotalPaid());
        dto.setOutstandingBalance(summary.getOutstandingBalance());
        dto.setUpcomingFees(summary.getUpcomingFees());
        dto.setOverdueFees(summary.getOverdueFees());
        dto.setPaymentProgress(summary.getPaymentProgress());

        dto.setFeeAllocations(allocations);
        dto.setPaymentHistory(payments);

        return dto;
    }

    public byte[] generateFeeSummaryPdf(String academicYear) {
        StudentPortalDto.FeeSummaryStatementDto data = getFeeSummaryStatement(academicYear);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY);
                    Phrase footer = new Phrase("FeeTrack System Generated Statement | Page " + writer.getPageNumber(), footerFont);
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                            (document.right() - document.left()) / 2 + document.leftMargin(),
                            document.bottom() - 15, 0);
                }
            });

            document.open();

            // Colors
            java.awt.Color primaryColor = new java.awt.Color(5, 150, 105); // Emerald-600
            java.awt.Color darkColor = new java.awt.Color(30, 41, 59);     // Slate-800
            java.awt.Color lightBg = new java.awt.Color(241, 245, 249);    // Slate-100
            java.awt.Color accentGreen = new java.awt.Color(209, 250, 229);// Emerald-100

            // Header Banner
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{65, 35});

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, primaryColor);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, darkColor);

            PdfPCell leftHeader = new PdfPCell();
            leftHeader.setBorder(Rectangle.NO_BORDER);
            leftHeader.addElement(new Paragraph("FeeTrack Institutional Portal", titleFont));
            leftHeader.addElement(new Paragraph(data.getInstitutionName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, darkColor)));
            leftHeader.addElement(new Paragraph(data.getInstitutionAddress(), subTitleFont));
            leftHeader.addElement(new Paragraph("Contact: " + data.getInstitutionContact(), subTitleFont));

            PdfPCell rightHeader = new PdfPCell();
            rightHeader.setBorder(Rectangle.NO_BORDER);
            rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph docTitle = new Paragraph("STUDENT FEE STATEMENT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, primaryColor));
            docTitle.setAlignment(Element.ALIGN_RIGHT);
            rightHeader.addElement(docTitle);

            String genDate = data.getReportGeneratedAt().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
            Paragraph genP = new Paragraph("Date: " + genDate, subTitleFont);
            genP.setAlignment(Element.ALIGN_RIGHT);
            rightHeader.addElement(genP);

            Paragraph ayP = new Paragraph("Academic Year: " + data.getAcademicYear(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, darkColor));
            ayP.setAlignment(Element.ALIGN_RIGHT);
            rightHeader.addElement(ayP);

            headerTable.addCell(leftHeader);
            headerTable.addCell(rightHeader);
            document.add(headerTable);

            document.add(new Paragraph(" ")); // Spacer

            // Student Information Box
            PdfPTable studentTable = new PdfPTable(4);
            studentTable.setWidthPercentage(100);
            studentTable.setWidths(new float[]{25, 25, 25, 25});

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.DARK_GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, java.awt.Color.BLACK);

            addInfoCell(studentTable, "Student Name:", data.getStudentFullName(), labelFont, valueFont, lightBg);
            addInfoCell(studentTable, "Student ID:", data.getStudentIdNumber(), labelFont, valueFont, lightBg);
            addInfoCell(studentTable, "Program / Course:", data.getCourse(), labelFont, valueFont, lightBg);
            addInfoCell(studentTable, "Academic Year:", data.getAcademicYear(), labelFont, valueFont, lightBg);

            document.add(studentTable);
            document.add(new Paragraph(" "));

            // Financial Summary Cards
            Font summaryHeadFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, primaryColor);
            document.add(new Paragraph("Financial Summary Overview", summaryHeadFont));
            document.add(new Paragraph(" "));

            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{25, 25, 25, 25});

            addMetricCell(summaryTable, "Total Assigned Fees", "INR " + data.getTotalAssignedFees().toPlainString(), primaryColor);
            addMetricCell(summaryTable, "Authorized Discounts", "INR " + data.getTotalDiscounts().toPlainString(), java.awt.Color.GRAY);
            addMetricCell(summaryTable, "Net Fee Obligation", "INR " + data.getNetObligation().toPlainString(), darkColor);
            addMetricCell(summaryTable, "Total Paid Verified", "INR " + data.getTotalPaid().toPlainString(), primaryColor);

            addMetricCell(summaryTable, "Outstanding Balance", "INR " + data.getOutstandingBalance().toPlainString(), new java.awt.Color(220, 38, 38)); // Red
            addMetricCell(summaryTable, "Upcoming (Next 30d)", "INR " + data.getUpcomingFees().toPlainString(), new java.awt.Color(217, 119, 6)); // Amber
            addMetricCell(summaryTable, "Overdue Amount", "INR " + data.getOverdueFees().toPlainString(), new java.awt.Color(185, 28, 28)); // Dark Red
            addMetricCell(summaryTable, "Payment Progress", data.getPaymentProgress().toPlainString() + "%", primaryColor);

            document.add(summaryTable);
            document.add(new Paragraph(" "));

            // Fee Allocations & Installments Table
            document.add(new Paragraph("Assigned Fee Allocations & Installments", summaryHeadFont));
            document.add(new Paragraph(" "));

            PdfPTable feeTable = new PdfPTable(7);
            feeTable.setWidthPercentage(100);
            feeTable.setWidths(new float[]{24, 13, 13, 13, 13, 12, 12});

            String[] feeHeaders = {"Fee Category", "Original Fee", "Discounts", "Net Fee", "Paid", "Balance", "Status"};
            for (String h : feeHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, java.awt.Color.WHITE)));
                cell.setBackgroundColor(primaryColor);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                feeTable.addCell(cell);
            }

            for (FeeAllocation fa : data.getFeeAllocations()) {
                addTableCell(feeTable, fa.getFeeCategory(), Element.ALIGN_LEFT, 8);
                addTableCell(feeTable, "INR " + fa.getOriginalAmount(), Element.ALIGN_RIGHT, 8);
                addTableCell(feeTable, "INR " + fa.getDiscountAmount(), Element.ALIGN_RIGHT, 8);
                addTableCell(feeTable, "INR " + fa.getNetObligation(), Element.ALIGN_RIGHT, 8);
                addTableCell(feeTable, "INR " + fa.getAmountPaid(), Element.ALIGN_RIGHT, 8);
                addTableCell(feeTable, "INR " + fa.getRemainingBalance(), Element.ALIGN_RIGHT, 8);
                addTableCell(feeTable, fa.getStatus(), Element.ALIGN_CENTER, 8);
            }

            if (data.getFeeAllocations().isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No fee allocations found for this student.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8)));
                emptyCell.setColspan(7);
                emptyCell.setPadding(8);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                feeTable.addCell(emptyCell);
            }

            document.add(feeTable);
            document.add(new Paragraph(" "));

            // Payment Transaction History Table
            document.add(new Paragraph("Payment Transaction History", summaryHeadFont));
            document.add(new Paragraph(" "));

            PdfPTable payTable = new PdfPTable(6);
            payTable.setWidthPercentage(100);
            payTable.setWidths(new float[]{20, 20, 20, 15, 12, 13});

            String[] payHeaders = {"Date & Time", "Transaction Ref", "Fee Category", "Payment Method", "Amount", "Status"};
            for (String h : payHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, java.awt.Color.WHITE)));
                cell.setBackgroundColor(darkColor);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                payTable.addCell(cell);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
            for (Payment p : data.getPaymentHistory()) {
                String pDate = p.getPaymentDate() != null ? p.getPaymentDate().format(dtf) : "N/A";
                addTableCell(payTable, pDate, Element.ALIGN_CENTER, 8);
                addTableCell(payTable, p.getTransactionReference(), Element.ALIGN_LEFT, 8);
                addTableCell(payTable, p.getFeeCategory() != null ? p.getFeeCategory() : "Fee Payment", Element.ALIGN_LEFT, 8);
                addTableCell(payTable, p.getPaymentMethod(), Element.ALIGN_CENTER, 8);
                addTableCell(payTable, "INR " + p.getAmount(), Element.ALIGN_RIGHT, 8);
                addTableCell(payTable, p.getStatus(), Element.ALIGN_CENTER, 8);
            }

            if (data.getPaymentHistory().isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No payment transactions recorded.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8)));
                emptyCell.setColspan(6);
                emptyCell.setPadding(8);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                payTable.addCell(emptyCell);
            }

            document.add(payTable);

            // Statement Notice Footer
            document.add(new Paragraph(" "));
            Font noteFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, java.awt.Color.GRAY);
            Paragraph note = new Paragraph("Notice: This is an authentic computer-generated fee statement from FeeTrack for institutional records. For fee structure inquiries or discrepancies, please contact the accounts department.", noteFont);
            note.setAlignment(Element.ALIGN_CENTER);
            document.add(note);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error rendering PDF statement: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addInfoCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont, java.awt.Color bg) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setBorderColor(new java.awt.Color(226, 232, 240));
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value != null ? value : "N/A", valueFont));
        table.addCell(cell);
    }

    private void addMetricCell(PdfPTable table, String title, String amount, java.awt.Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new java.awt.Color(248, 250, 252));
        cell.setPadding(6);
        cell.setBorderColor(new java.awt.Color(226, 232, 240));
        cell.addElement(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.DARK_GRAY)));
        cell.addElement(new Paragraph(amount, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color)));
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, int alignment, float fontSize) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(new java.awt.Color(226, 232, 240));
        table.addCell(cell);
    }
}
