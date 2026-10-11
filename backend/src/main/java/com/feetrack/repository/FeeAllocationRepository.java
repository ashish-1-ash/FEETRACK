package com.feetrack.repository;

import com.feetrack.model.FeeAllocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeAllocationRepository extends MongoRepository<FeeAllocation, String> {
    List<FeeAllocation> findByStudentIdNumber(String studentIdNumber);
    List<FeeAllocation> findByStudentIdNumberAndAcademicYear(String studentIdNumber, String academicYear);
    List<FeeAllocation> findByStudentIdNumberOrderByDueDateAsc(String studentIdNumber);
}
