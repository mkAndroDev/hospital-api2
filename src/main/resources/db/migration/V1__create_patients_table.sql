CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    pesel VARCHAR(11) NOT NULL UNIQUE,
    condition VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    admitted_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_condition CHECK (condition IN ('GREEN', 'YELLOW', 'ORANGE', 'RED', 'BROWN', 'BLACK')),
    CONSTRAINT chk_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'TREATED'))
);

CREATE INDEX idx_patients_status ON patients(status);
CREATE INDEX idx_patients_condition ON patients(condition);
CREATE INDEX idx_patients_admitted_at ON patients(admitted_at);
