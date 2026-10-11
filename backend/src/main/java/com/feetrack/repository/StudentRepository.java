package com.feetrack.repository;

import com.feetrack.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    Optional<Student> findByStudentIdNumber(String studentIdNumber);
    Optional<Student> findByEmailIgnoreCase(String email);
    Optional<Student> findByLinkedUserId(String linkedUserId);
    boolean existsByStudentIdNumber(String studentIdNumber);
    boolean existsByEmailIgnoreCase(String email);
}
