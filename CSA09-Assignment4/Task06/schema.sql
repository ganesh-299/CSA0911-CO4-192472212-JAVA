-- ====================================================================
-- Campus Event Registration System - Database Schema
-- Task 6: Database Definition & Test Data Seeds
-- ====================================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS campus_events
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_events;

-- 2. Drop existing table if recreating
-- DROP TABLE IF EXISTS registrations;

-- 3. Create Registrations Table with Unique Constraint
CREATE TABLE IF NOT EXISTS registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Prevent duplicate registration for the same student in the same event
    CONSTRAINT uk_student_event UNIQUE (student_id, event_name)
) ENGINE=InnoDB;

-- 4. Create Indexes for High-Performance Filtering and Pagination
CREATE INDEX idx_event_name ON registrations(event_name);
CREATE INDEX idx_student_id ON registrations(student_id);
CREATE INDEX idx_registration_date ON registrations(registration_date DESC);

-- 5. Seed Test Records for Demonstration & Verification
INSERT INTO registrations (student_id, student_name, event_name, registration_date) VALUES
('21CS001', 'Alice Johnson', 'Hackathon 2026', '2026-11-10 09:30:00'),
('21CS002', 'Bob Smith', 'AI & Machine Learning Workshop', '2026-11-10 10:15:00'),
('21IT015', 'Charlie Brown', 'Web Dev Bootcamp', '2026-11-11 11:00:00'),
('21EC030', 'David Miller', 'Robotics Expo', '2026-11-11 14:20:00'),
('21ME045', 'Emma Watson', 'Hackathon 2026', '2026-11-12 16:45:00'),
('21CS010', 'Frank Castle', 'Competitive Coding Marathon', '2026-11-13 10:00:00'),
('21IT022', 'Grace Hopper', 'AI & Machine Learning Workshop', '2026-11-13 11:30:00'),
('21CS033', 'Henry Cavill', 'Web Dev Bootcamp', '2026-11-14 13:10:00'),
('21EC044', 'Iris West', 'Robotics Expo', '2026-11-14 15:40:00'),
('21CS055', 'Jack Ryan', 'Hackathon 2026', '2026-11-15 09:00:00'),
('21IT066', 'Karen Page', 'Competitive Coding Marathon', '2026-11-15 10:20:00'),
('21ME077', 'Luke Cage', 'Robotics Expo', '2026-11-15 11:45:00')
ON DUPLICATE KEY UPDATE registration_date = registration_date;

-- 6. Verification Queries:
-- SELECT * FROM registrations ORDER BY id DESC LIMIT 5 OFFSET 0;
-- SELECT COUNT(*) FROM registrations WHERE event_name = 'Hackathon 2026';
