-- Give 2 new permissions to MANAGER role
-- EMPLOYEE:UPDATE:ALL - allows manager to update any employee (promote to manager, etc.)
-- EMPLOYEE:READ:ALL - allows manager to access employee sensitive data (phone, dateOfBirth, hireDate)

INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:UPDATE:ALL'));

INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, (SELECT id FROM permissions WHERE name = 'EMPLOYEE:READ:ALL'));
