CREATE TABLE courses (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO courses (title) VALUES ('Intro to Cloud'), ('Safe Deployments 101');
