package com.newwork.employeeprofile.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "permissions", uniqueConstraints = {
    @UniqueConstraint(name = "uq_permission_resource_action_scope",
                     columnNames = {"resource", "action", "scope"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;  // Format: RESOURCE:ACTION:SCOPE (e.g., "EMPLOYEE:READ:DEPARTMENT")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionResource resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionScope scope;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this permission grants access for a given resource, action, and scope
     */
    public boolean grants(PermissionResource resource, PermissionAction action, PermissionScope scope) {
        return this.resource == resource &&
               this.action == action &&
               this.scope.includes(scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
