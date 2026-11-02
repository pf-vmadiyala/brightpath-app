CREATE TABLE enrollments (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    course_id     BIGINT       NOT NULL,
    student_email VARCHAR(255) NOT NULL,
    enrolled_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses (id)
);
