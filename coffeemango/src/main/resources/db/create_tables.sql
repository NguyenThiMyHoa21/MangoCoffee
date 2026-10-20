-- Tạo bảng coffee_tables
CREATE TABLE IF NOT EXISTS coffee_tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number VARCHAR(20) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED') NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Thêm cột table_id vào bảng invoices (nếu chưa có)
ALTER TABLE invoices 
ADD COLUMN table_id BIGINT NULL,
ADD CONSTRAINT fk_invoices_table 
FOREIGN KEY (table_id) REFERENCES coffee_tables(id) ON DELETE SET NULL;
