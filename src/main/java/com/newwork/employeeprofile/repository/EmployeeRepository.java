package com.newwork.employeeprofile.repository;

import com.newwork.employeeprofile.domain.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find employee by email
     */
    @EntityGraph(attributePaths = {"role", "role.permissions", "department"})
    Optional<Employee> findByEmail(String email);

    /**
     * Find employee by ID with all relationships loaded
     */
    @EntityGraph(attributePaths = {"role", "role.permissions", "department"})
    Optional<Employee> findById(Long id);

    /**
     * Check if employee exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find all employees by role ID
     */
    List<Employee> findByRoleId(Long roleId);

    /**
     * Find all employees by role name
     */
    @Query("SELECT e FROM Employee e WHERE e.role.name = :roleName")
    List<Employee> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find all employees in a department
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.role r " +
           "LEFT JOIN FETCH r.permissions " +
           "LEFT JOIN FETCH e.department d " +
           "WHERE e.department.id = :departmentId")
    List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * Find all employees in a department (by department name)
     */
    @Query("SELECT e FROM Employee e WHERE e.department.name = :departmentName")
    List<Employee> findByDepartmentName(@Param("departmentName") String departmentName);

    /**
     * Find all employees managed by a specific manager (employees in departments managed by this person)
     */
    @Query("SELECT e FROM Employee e WHERE e.department.manager.id = :managerId AND e.active = true")
    List<Employee> findEmployeesByManagerId(@Param("managerId") Long managerId);

    /**
     * Find all active employees
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.role r " +
           "LEFT JOIN FETCH r.permissions " +
           "LEFT JOIN FETCH e.department d " +
           "WHERE e.active = true")
    List<Employee> findAllActive();

    /**
     * Search employees by keyword (name or email)
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.role r " +
           "LEFT JOIN FETCH r.permissions " +
           "LEFT JOIN FETCH e.department d " +
           "WHERE e.active = true AND " +
           "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Employee> searchEmployees(@Param("keyword") String keyword);

    /**
     * Count employees by department ID
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.active = true")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * Find all employees in same department as given employee
     */
    @Query("SELECT e FROM Employee e WHERE e.department.id = (SELECT emp.department.id FROM Employee emp WHERE emp.id = :employeeId) AND e.active = true")
    List<Employee> findColleaguesInSameDepartment(@Param("employeeId") Long employeeId);
}
