-- Create departments table for normalized department management
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    location VARCHAR(255),
    manager_id BIGINT,
    parent_department_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent_department FOREIGN KEY (parent_department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_department_name ON departments(name);
CREATE INDEX idx_department_manager ON departments(manager_id);
CREATE INDEX idx_department_parent ON departments(parent_department_id);
CREATE INDEX idx_department_active ON departments(active);

-- Insert default departments
INSERT INTO departments (id, name, description, location, active) VALUES
(1, 'Engineering', 'Software Engineering and Development', 'New York', TRUE),
(2, 'Human Resources', 'HR and Employee Management', 'New York', TRUE),
(3, 'Sales', 'Sales and Business Development', 'San Francisco', TRUE),
(4, 'Marketing', 'Marketing and Communications', 'Los Angeles', TRUE);

-- Set sequence to start from 5
SELECT setval('departments_id_seq', 5, false);

-- Add comments
COMMENT ON TABLE departments IS 'Stores department information with hierarchical structure';
COMMENT ON COLUMN departments.manager_id IS 'Employee ID of the department manager (set after employee creation)';
COMMENT ON COLUMN departments.parent_department_id IS 'Parent department for hierarchical structure';
