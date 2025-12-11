package com.newwork.employeeprofile.domain;

public enum PermissionScope {
    OWN,         // Own data only
    DEPARTMENT,  // Department-level access
    ALL;         // Global access

    /**
     * Check if this scope includes another scope
     * Hierarchy: OWN < DEPARTMENT < ALL
     */
    public boolean includes(PermissionScope other) {
        return switch (this) {
            case ALL -> true;  // ALL includes everything
            case DEPARTMENT -> other == DEPARTMENT || other == OWN;
            case OWN -> other == OWN;
        };
    }
}
