-- Create employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('MANAGER', 'EMPLOYEE', 'COWORKER')),
    department VARCHAR(100) NOT NULL,
    position VARCHAR(100),
    phone VARCHAR(20),
    date_of_birth DATE,
    hire_date DATE,
    bio VARCHAR(1000),
    skills VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_employee_email ON employees(email);
CREATE INDEX idx_employee_role ON employees(role);
CREATE INDEX idx_employee_department ON employees(department);
CREATE INDEX idx_employee_active ON employees(active);
CREATE INDEX idx_employee_manager ON employees(manager_id);

-- Insert sample data for testing
-- BCrypt hash for password: "password"
INSERT INTO employees (first_name, last_name, email, password, role, department, position, phone, hire_date, active) VALUES
('John', 'Manager', 'john.manager@newwork.com', '$2a$10$q3ndRMAyLO7KfvPRWlR8gel/agRTOZuTy49hR26KB6Gal1uHjWGKC', 'MANAGER', 'Engineering', 'Engineering Manager', '+1234567890', '2020-01-15', TRUE),
('Jane', 'Smith', 'jane.smith@newwork.com', '$2a$10$q3ndRMAyLO7KfvPRWlR8gel/agRTOZuTy49hR26KB6Gal1uHjWGKC', 'EMPLOYEE', 'Engineering', 'Senior Developer', '+1234567891', '2021-03-20', TRUE),
('Bob', 'Coworker', 'bob.coworker@newwork.com', '$2a$10$q3ndRMAyLO7KfvPRWlR8gel/agRTOZuTy49hR26KB6Gal1uHjWGKC', 'COWORKER', 'Engineering', 'Junior Developer', '+1234567892', '2022-06-10', TRUE);

-- Update manager relationships
UPDATE employees SET manager_id = 1 WHERE id IN (2, 3);

-- Add comments
COMMENT ON TABLE employees IS 'Stores employee profile information';
COMMENT ON COLUMN employees.role IS 'User role: MANAGER, EMPLOYEE, or COWORKER';
COMMENT ON COLUMN employees.active IS 'Indicates if the employee is currently active';
