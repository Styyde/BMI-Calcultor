-- V1__create_users_and_bmi_records_tables.sql

-- ==========================================
-- 1. Table `users` (Entité User)
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    last_login TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE
    );

-- Index sur email pour les recherches (déjà unique, mais utile)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ==========================================
-- 2. Table `bmi_records` (Entité BmiRecord)
-- ==========================================
CREATE TABLE IF NOT EXISTS bmi_records (
                                           id BIGSERIAL PRIMARY KEY,
                                           ip_address VARCHAR(45) NOT NULL,
    weight_kg DOUBLE PRECISION NOT NULL,
    height_cm DOUBLE PRECISION NOT NULL,
    bmi_value DOUBLE PRECISION NOT NULL,
    category VARCHAR(50) NOT NULL,
    user_id BIGINT,
    calculated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
    );

-- Indexes définis dans l'entité BmiRecord
CREATE INDEX IF NOT EXISTS idx_ip_calculated_at ON bmi_records (ip_address, calculated_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_id ON bmi_records (user_id);

-- ==========================================
-- 3. Contrainte de clé étrangère
-- ==========================================
ALTER TABLE bmi_records
    ADD CONSTRAINT fk_bmi_records_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE SET NULL;