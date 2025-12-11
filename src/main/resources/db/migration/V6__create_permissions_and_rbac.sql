-- Create enterprise RBAC (Role-Based Access Control) with dynamic permissions

-- Permissions table: Defines what actions can be performed
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    resource VARCHAR(50) NOT NULL,     -- EMPLOYEE, ABSENCE, FEEDBACK, DEPARTMENT
    action VARCHAR(50) NOT NULL,       -- CREATE, READ, UPDATE, DELETE, APPROVE
    scope VARCHAR(50) NOT NULL,        -- OWN, DEPARTMENT, ALL
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_permission_resource_action_scope UNIQUE (resource, action, scope)
);

-- Role-Permission mapping (many-to-many)
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- Create indexes for performance
CREATE INDEX idx_permission_resource ON permissions(resource);
CREATE INDEX idx_permission_action ON permissions(action);
CREATE INDEX idx_permission_scope ON permissions(scope);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Insert granular permissions
-- Format: resource:action:scope
-- Example: EMPLOYEE:READ:OWN = "Can read own employee data"

-- Employee Permissions
INSERT INTO permissions (name, resource, action, scope, description) VALUES
-- OWN scope
('EMPLOYEE:READ:OWN', 'EMPLOYEE', 'READ', 'OWN', 'View own profile'),
('EMPLOYEE:UPDATE:OWN', 'EMPLOYEE', 'UPDATE', 'OWN', 'Edit own profile'),

-- DEPARTMENT scope
('EMPLOYEE:READ:DEPARTMENT', 'EMPLOYEE', 'READ', 'DEPARTMENT', 'View employees in own department'),
('EMPLOYEE:UPDATE:DEPARTMENT', 'EMPLOYEE', 'UPDATE', 'DEPARTMENT', 'Edit employees in own department'),
('EMPLOYEE:CREATE:DEPARTMENT', 'EMPLOYEE', 'CREATE', 'DEPARTMENT', 'Create employees in own department'),
('EMPLOYEE:DELETE:DEPARTMENT', 'EMPLOYEE', 'DELETE', 'DEPARTMENT', 'Deactivate employees in own department'),

-- ALL scope
('EMPLOYEE:READ:ALL', 'EMPLOYEE', 'READ', 'ALL', 'View all employees across departments'),
('EMPLOYEE:UPDATE:ALL', 'EMPLOYEE', 'UPDATE', 'ALL', 'Edit any employee'),
('EMPLOYEE:CREATE:ALL', 'EMPLOYEE', 'CREATE', 'ALL', 'Create employees in any department'),
('EMPLOYEE:DELETE:ALL', 'EMPLOYEE', 'DELETE', 'ALL', 'Deactivate any employee');

-- Absence Permissions
INSERT INTO permissions (name, resource, action, scope, description) VALUES
-- OWN scope
('ABSENCE:CREATE:OWN', 'ABSENCE', 'CREATE', 'OWN', 'Create own absence requests'),
('ABSENCE:READ:OWN', 'ABSENCE', 'READ', 'OWN', 'View own absence requests'),
('ABSENCE:UPDATE:OWN', 'ABSENCE', 'UPDATE', 'OWN', 'Edit own absence requests'),
('ABSENCE:DELETE:OWN', 'ABSENCE', 'DELETE', 'OWN', 'Cancel own absence requests'),

-- DEPARTMENT scope
('ABSENCE:READ:DEPARTMENT', 'ABSENCE', 'READ', 'DEPARTMENT', 'View absence requests in own department'),
('ABSENCE:APPROVE:DEPARTMENT', 'ABSENCE', 'APPROVE', 'DEPARTMENT', 'Approve/reject absence requests in own department'),

-- ALL scope
('ABSENCE:READ:ALL', 'ABSENCE', 'READ', 'ALL', 'View all absence requests'),
('ABSENCE:APPROVE:ALL', 'ABSENCE', 'APPROVE', 'ALL', 'Approve/reject any absence request');

-- Feedback Permissions
INSERT INTO permissions (name, resource, action, scope, description) VALUES
-- OWN scope
('FEEDBACK:CREATE:OWN', 'FEEDBACK', 'CREATE', 'OWN', 'Create own feedback'),
('FEEDBACK:READ:OWN', 'FEEDBACK', 'READ', 'OWN', 'View own feedback'),

-- DEPARTMENT scope
('FEEDBACK:READ:DEPARTMENT', 'FEEDBACK', 'READ', 'DEPARTMENT', 'View feedback in own department'),
('FEEDBACK:CREATE:DEPARTMENT', 'FEEDBACK', 'CREATE', 'DEPARTMENT', 'Create feedback for department colleagues'),

-- ALL scope
('FEEDBACK:READ:ALL', 'FEEDBACK', 'READ', 'ALL', 'View all feedback'),
('FEEDBACK:CREATE:ALL', 'FEEDBACK', 'CREATE', 'ALL', 'Create feedback for anyone');

-- Department Management Permissions
INSERT INTO permissions (name, resource, action, scope, description) VALUES
('DEPARTMENT:READ:ALL', 'DEPARTMENT', 'READ', 'ALL', 'View all departments'),
('DEPARTMENT:CREATE:ALL', 'DEPARTMENT', 'CREATE', 'ALL', 'Create new departments'),
('DEPARTMENT:UPDATE:ALL', 'DEPARTMENT', 'UPDATE', 'ALL', 'Edit department information'),
('DEPARTMENT:DELETE:ALL', 'DEPARTMENT', 'DELETE', 'ALL', 'Delete departments');

-- Role Management Permissions (admin-level)
INSERT INTO permissions (name, resource, action, scope, description) VALUES
('ROLE:READ:ALL', 'ROLE', 'READ', 'ALL', 'View all roles'),
('ROLE:CREATE:ALL', 'ROLE', 'CREATE', 'ALL', 'Create new roles'),
('ROLE:UPDATE:ALL', 'ROLE', 'UPDATE', 'ALL', 'Edit role permissions'),
('ROLE:DELETE:ALL', 'ROLE', 'DELETE', 'ALL', 'Delete roles');

-- ============================================================================
-- ASSIGN PERMISSIONS TO ROLES
-- ============================================================================

-- EMPLOYEE Role (id=1): Basic self-service permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Own profile
(1, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:READ:OWN')),
(1, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:UPDATE:OWN')),
-- View department colleagues
(1, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:READ:DEPARTMENT')),
-- Own absences
(1, (SELECT id FROM permissions WHERE name = 'ABSENCE:CREATE:OWN')),
(1, (SELECT id FROM permissions WHERE name = 'ABSENCE:READ:OWN')),
(1, (SELECT id FROM permissions WHERE name = 'ABSENCE:UPDATE:OWN')),
(1, (SELECT id FROM permissions WHERE name = 'ABSENCE:DELETE:OWN')),
-- Own feedback
(1, (SELECT id FROM permissions WHERE name = 'FEEDBACK:CREATE:OWN')),
(1, (SELECT id FROM permissions WHERE name = 'FEEDBACK:READ:OWN')),
-- Create feedback for colleagues
(1, (SELECT id FROM permissions WHERE name = 'FEEDBACK:CREATE:DEPARTMENT'));

-- MANAGER Role (id=2): Department-level permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Everything EMPLOYEE has (inherited)
(2, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:READ:OWN')),
(2, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:UPDATE:OWN')),
(2, (SELECT id FROM permissions WHERE name = 'ABSENCE:CREATE:OWN')),
(2, (SELECT id FROM permissions WHERE name = 'ABSENCE:READ:OWN')),
(2, (SELECT id FROM permissions WHERE name = 'ABSENCE:UPDATE:OWN')),
(2, (SELECT id FROM permissions WHERE name = 'ABSENCE:DELETE:OWN')),
(2, (SELECT id FROM permissions WHERE name = 'FEEDBACK:CREATE:OWN')),
(2, (SELECT id FROM permissions WHERE name = 'FEEDBACK:READ:OWN')),

-- PLUS department-level management
(2, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:READ:DEPARTMENT')),
(2, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:UPDATE:DEPARTMENT')),
(2, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:CREATE:DEPARTMENT')),
(2, (SELECT id FROM permissions WHERE name = 'ABSENCE:READ:DEPARTMENT')),
(2, (SELECT id FROM permissions WHERE name = 'ABSENCE:APPROVE:DEPARTMENT')),
(2, (SELECT id FROM permissions WHERE name = 'FEEDBACK:READ:DEPARTMENT')),
(2, (SELECT id FROM permissions WHERE name = 'FEEDBACK:CREATE:DEPARTMENT'));

-- ============================================================================
-- FUTURE: Add PRODUCT_OWNER role (to be done via INSERT, no code changes!)
-- ============================================================================

-- To add Product Owner in the future (NO CODE CHANGES NEEDED):
--
-- Step 1: Insert new role
-- INSERT INTO roles (name, description) VALUES
-- ('PRODUCT_OWNER', 'Product owner with cross-department visibility');
--
-- Step 2: Assign permissions (can see ALL, but cannot edit)
-- INSERT INTO role_permissions (role_id, permission_id)
-- SELECT 3, id FROM permissions
-- WHERE (resource = 'EMPLOYEE' AND action = 'READ' AND scope = 'ALL')
--    OR (resource = 'ABSENCE' AND action = 'READ' AND scope = 'ALL')
--    OR (resource = 'FEEDBACK' AND action = 'READ' AND scope = 'ALL')
--    OR (resource = 'DEPARTMENT' AND action = 'READ' AND scope = 'ALL');

-- Add comments
COMMENT ON TABLE permissions IS 'Defines granular permissions with resource:action:scope pattern';
COMMENT ON TABLE role_permissions IS 'Maps permissions to roles for flexible RBAC';
COMMENT ON COLUMN permissions.resource IS 'Resource type: EMPLOYEE, ABSENCE, FEEDBACK, DEPARTMENT, ROLE';
COMMENT ON COLUMN permissions.action IS 'Action type: CREATE, READ, UPDATE, DELETE, APPROVE';
COMMENT ON COLUMN permissions.scope IS 'Scope: OWN (self), DEPARTMENT (team), ALL (global)';
