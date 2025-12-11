package com.newwork.employeeprofile.repository;

import com.newwork.employeeprofile.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name
     * @param name Role name (e.g., "EMPLOYEE", "MANAGER", "PRODUCT_OWNER")
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role exists by name
     * @param name Role name
     * @return true if role exists
     */
    boolean existsByName(String name);
}
