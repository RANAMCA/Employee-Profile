-- Create absences table
CREATE TABLE absences (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('VACATION', 'SICK_LEAVE', 'PERSONAL_LEAVE', 'PARENTAL_LEAVE', 'UNPAID_LEAVE', 'REMOTE_WORK')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    review_comment VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviewer FOREIGN KEY (reviewed_by) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT chk_date_range CHECK (end_date >= start_date)
);

-- Create indexes
CREATE INDEX idx_absence_employee ON absences(employee_id);
CREATE INDEX idx_absence_status ON absences(status);
CREATE INDEX idx_absence_dates ON absences(start_date, end_date);
CREATE INDEX idx_absence_type ON absences(type);
CREATE INDEX idx_absence_reviewer ON absences(reviewed_by);

-- Insert sample absence requests
INSERT INTO absences (employee_id, type, start_date, end_date, reason, status, reviewed_by, reviewed_at) VALUES
(2, 'VACATION', '2024-12-20', '2024-12-31', 'Year-end vacation', 'APPROVED', 1, CURRENT_TIMESTAMP),
(3, 'SICK_LEAVE', '2024-12-05', '2024-12-06', 'Flu symptoms', 'PENDING', NULL, NULL);

-- Add comments
COMMENT ON TABLE absences IS 'Stores employee absence/leave requests';
COMMENT ON COLUMN absences.type IS 'Type of absence: VACATION, SICK_LEAVE, PERSONAL_LEAVE, PARENTAL_LEAVE, UNPAID_LEAVE, REMOTE_WORK';
COMMENT ON COLUMN absences.status IS 'Request status: PENDING, APPROVED, REJECTED, or CANCELLED';
