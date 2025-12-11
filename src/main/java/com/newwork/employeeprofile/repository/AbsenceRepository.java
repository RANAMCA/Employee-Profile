package com.newwork.employeeprofile.repository;

import com.newwork.employeeprofile.domain.Absence;
import com.newwork.employeeprofile.domain.AbsenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    List<Absence> findByEmployeeId(Long employeeId);

    List<Absence> findByStatus(AbsenceStatus status);

    @Query("SELECT a FROM Absence a WHERE a.employee.id = :employeeId AND a.status = :status")
    List<Absence> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId,
                                             @Param("status") AbsenceStatus status);

    @Query("SELECT a FROM Absence a WHERE a.employee.department.manager.id = :managerId")
    List<Absence> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT a FROM Absence a WHERE a.startDate <= :endDate AND a.endDate >= :startDate")
    List<Absence> findOverlappingAbsences(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Absence a WHERE a.employee.id = :employeeId AND " +
           "a.startDate <= :endDate AND a.endDate >= :startDate AND " +
           "a.status IN ('PENDING', 'APPROVED')")
    List<Absence> findConflictingAbsences(@Param("employeeId") Long employeeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.employee.id = :employeeId AND " +
           "a.status = 'APPROVED' AND YEAR(a.startDate) = :year")
    long countApprovedAbsencesByEmployeeAndYear(@Param("employeeId") Long employeeId,
                                                  @Param("year") int year);
}
