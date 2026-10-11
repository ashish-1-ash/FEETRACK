package com.feetrack.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "students")
public class Student {

    @Id
    private String id;

    @Indexed(unique = true)
    private String studentIdNumber; // e.g. "STU-2026-001"

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String phone;

    private String course; // e.g. "B.Tech Computer Science & Engineering"

    private String academicYear; // e.g. "2026-2027"

    private String currentSemester; // e.g. "Semester 5"

    private String status; // "ACTIVE", "PENDING_VERIFICATION", "SUSPENDED"

    private String linkedUserId; // Links to User.id

    private LocalDate enrollmentDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Student() {
    }

    public Student(String id, String studentIdNumber, String fullName, String email, String phone, String course, String academicYear, String currentSemester, String status, String linkedUserId, LocalDate enrollmentDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.studentIdNumber = studentIdNumber;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.course = course;
        this.academicYear = academicYear;
        this.currentSemester = currentSemester;
        this.status = status;
        this.linkedUserId = linkedUserId;
        this.enrollmentDate = enrollmentDate;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(String currentSemester) {
        this.currentSemester = currentSemester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(String linkedUserId) {
        this.linkedUserId = linkedUserId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
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
