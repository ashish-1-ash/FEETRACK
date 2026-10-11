package com.feetrack.service;

import com.feetrack.dto.AuthDto;
import com.feetrack.exception.BadRequestException;
import com.feetrack.exception.ResourceNotFoundException;
import com.feetrack.model.Role;
import com.feetrack.model.Student;
import com.feetrack.model.User;
import com.feetrack.repository.StudentRepository;
import com.feetrack.repository.UserRepository;
import com.feetrack.security.CustomUserDetails;
import com.feetrack.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthDto.AuthResponse registerStudent(AuthDto.StudentRegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // 1. Password confirmation check
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long");
        }

        // 2. Email uniqueness check
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BadRequestException("An account with this email address already exists");
        }

        // 3. Prevent students from assigning themselves an existing student record merely by supplying another student's ID
        String requestedStudentId = request.getStudentId() != null ? request.getStudentId().trim().toUpperCase() : null;
        Student linkedStudent = null;
        String studentStatus = "ACTIVE";

        if (requestedStudentId != null && !requestedStudentId.isEmpty()) {
            Optional<Student> existingStudentOpt = studentRepository.findByStudentIdNumber(requestedStudentId);
            if (existingStudentOpt.isPresent()) {
                Student existingStudent = existingStudentOpt.get();
                // If this student record is already linked to another user account, reject
                if (existingStudent.getLinkedUserId() != null && !existingStudent.getLinkedUserId().isEmpty()) {
                    throw new BadRequestException("Student ID " + requestedStudentId + " is already linked to an existing account");
                }
                // Check if email matches institution record for that student ID
                if (!existingStudent.getEmail().equalsIgnoreCase(normalizedEmail)) {
                    // Mismatch between institutional email on record and registering email -> Requires admin verification
                    studentStatus = "PENDING_VERIFICATION";
                }
                linkedStudent = existingStudent;
            } else {
                // Supplied a custom ID that is not yet in student registry -> require admin verification
                studentStatus = "PENDING_VERIFICATION";
            }
        }

        // 4. Create User entity - Force ROLE_STUDENT
        User user = new User();
        user.setName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone().trim());
        user.setRole(Role.ROLE_STUDENT);
        user.setStatus(studentStatus.equals("PENDING_VERIFICATION") ? "PENDING_VERIFICATION" : "ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 5. Create or link Student record
        if (linkedStudent != null) {
            linkedStudent.setLinkedUserId(savedUser.getId());
            linkedStudent.setPhone(request.getPhone().trim());
            linkedStudent.setStatus(studentStatus);
            linkedStudent.setUpdatedAt(LocalDateTime.now());
            studentRepository.save(linkedStudent);

            savedUser.setStudentProfileId(linkedStudent.getStudentIdNumber());
            userRepository.save(savedUser);
        } else {
            String newStudentId = (requestedStudentId != null && !requestedStudentId.isEmpty())
                    ? requestedStudentId
                    : "STU-" + System.currentTimeMillis() % 1000000;

            Student newStudent = new Student();
            newStudent.setStudentIdNumber(newStudentId);
            newStudent.setFullName(request.getFullName().trim());
            newStudent.setEmail(normalizedEmail);
            newStudent.setPhone(request.getPhone().trim());
            newStudent.setCourse(request.getCourse() != null && !request.getCourse().isBlank() ? request.getCourse().trim() : "B.Tech Computer Science");
            newStudent.setAcademicYear("2026-2027");
            newStudent.setCurrentSemester("Semester 1");
            newStudent.setStatus(studentStatus);
            newStudent.setLinkedUserId(savedUser.getId());
            newStudent.setEnrollmentDate(LocalDate.now());
            newStudent.setCreatedAt(LocalDateTime.now());
            newStudent.setUpdatedAt(LocalDateTime.now());
            studentRepository.save(newStudent);

            savedUser.setStudentProfileId(newStudent.getStudentIdNumber());
            userRepository.save(savedUser);
            linkedStudent = newStudent;
        }

        String message = studentStatus.equals("PENDING_VERIFICATION")
                ? "Registration submitted. Your account is pending administrative enrollment verification."
                : "Student registration successful. You can now log in.";

        return new AuthDto.AuthResponse(
                null,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getStatus(),
                linkedStudent.getStudentIdNumber(),
                message
        );
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if ("PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("Your account is pending institutional verification. Please contact administration.");
        }

        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("Your account has been deactivated. Please contact administration.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("name", user.getName());
        claims.put("studentProfileId", user.getStudentProfileId());

        String token = jwtService.generateToken(userDetails, claims);

        return new AuthDto.AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus(),
                user.getStudentProfileId(),
                "Login successful"
        );
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadRequestException("No authenticated user found in context");
        }

        if (auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUser();
        }

        String email = auth.getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
