-- Refactor employees table to use normalized roles and departments

-- Step 1: Add new columns
ALTER TABLE employees
ADD COLUMN role_id BIGINT,
ADD COLUMN department_id BIGINT;

-- Step 2: Migrate existing role data to role_id
UPDATE employees SET role_id = 2 WHERE role = 'MANAGER';  -- MANAGER
UPDATE employees SET role_id = 1 WHERE role = 'EMPLOYEE'; -- EMPLOYEE
UPDATE employees SET role_id = 1 WHERE role = 'COWORKER'; -- COWORKER -> EMPLOYEE (default)

-- Step 3: Create department entries for existing departments and migrate data
-- First, insert any departments that don't exist yet
INSERT INTO departments (name, description, active)
SELECT DISTINCT department, 'Auto-migrated department', TRUE
FROM employees
WHERE department NOT IN (SELECT name FROM departments)
ON CONFLICT (name) DO NOTHING;

-- Update employees with department_id
UPDATE employees e
SET department_id = d.id
FROM departments d
WHERE e.department = d.name;

-- Step 4: Set managers for departments based on existing manager_id relationships
-- This finds the first manager in each department and assigns them
UPDATE departments d
SET manager_id = (
    SELECT e.id
    FROM employees e
    WHERE e.department_id = d.id
      AND e.role_id = 2
    LIMIT 1
);

-- Step 5: Make new columns NOT NULL (now that data is migrated)
ALTER TABLE employees ALTER COLUMN role_id SET NOT NULL;
ALTER TABLE employees ALTER COLUMN department_id SET NOT NULL;

-- Step 6: Add foreign key constraints
ALTER TABLE employees
ADD CONSTRAINT fk_employee_role FOREIGN KEY (role_id) REFERENCES roles(id),
ADD CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments(id);

-- Step 7: Add foreign key from departments to employees for manager
ALTER TABLE departments
ADD CONSTRAINT fk_department_manager FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL;

-- Step 8: Drop old columns (role VARCHAR, department VARCHAR, manager_id)
ALTER TABLE employees DROP COLUMN role;
ALTER TABLE employees DROP COLUMN department;
ALTER TABLE employees DROP COLUMN manager_id;

-- Step 9: Update indexes
CREATE INDEX idx_employee_role_id ON employees(role_id);
CREATE INDEX idx_employee_department_id ON employees(department_id);

-- Step 10: Update comments
COMMENT ON COLUMN employees.role_id IS 'Foreign key to roles table (MANAGER or EMPLOYEE)';
COMMENT ON COLUMN employees.department_id IS 'Foreign key to departments table';
COMMENT ON TABLE employees IS 'Stores employee profile information with normalized roles and departments';
