package com.newwork.employeeprofile.repository;

import com.newwork.employeeprofile.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Find department by name
     * @param name Department name
     * @return Optional containing the department if found
     */
    Optional<Department> findByName(String name);

    /**
     * Find all active departments
     * @return List of active departments
     */
    List<Department> findByActiveTrue();

    /**
     * Find department by manager ID
     * @param managerId Manager's employee ID
     * @return Optional containing the department if found
     */
    Optional<Department> findByManagerId(Long managerId);

    /**
     * Find all sub-departments of a parent department
     * @param parentId Parent department ID
     * @return List of sub-departments
     */
    List<Department> findByParentDepartmentId(Long parentId);

    /**
     * Check if employee is manager of any department
     * @param employeeId Employee ID
     * @return true if employee is a manager of any department
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.manager.id = :employeeId")
    boolean isEmployeeManagerOfAnyDepartment(@Param("employeeId") Long employeeId);

    /**
     * Find all root departments (departments without parent)
     * @return List of root departments
     */
    @Query("SELECT d FROM Department d WHERE d.parentDepartment IS NULL AND d.active = true")
    List<Department> findAllRootDepartments();
}
