-- Create roles table for normalized role management
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on role name
CREATE INDEX idx_role_name ON roles(name);

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
(1, 'EMPLOYEE', 'Default role for all employees with standard access'),
(2, 'MANAGER', 'Manager role with elevated permissions to manage department employees');

-- Set sequence to start from 3
SELECT setval('roles_id_seq', 3, false);

-- Add comments
COMMENT ON TABLE roles IS 'Stores role definitions for employee authorization';
COMMENT ON COLUMN roles.name IS 'Unique role name used in authorization';
