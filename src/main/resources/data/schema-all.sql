DROP TABLE users IF EXISTS;

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
                       username VARCHAR(20),
                       status VARCHAR(20),
                       last_login_date DATE
);

INSERT INTO users (username, status, last_login_date)
VALUES
('alice', 'active', '2024-09-15'),
('bob', 'inactive', '2024-03-01'),
('charlie', 'active', '2024-08-20'),
('david', 'inactive', '2023-12-15'),
('eve', 'active', '2024-02-25'),
('frank', 'inactive', '2024-07-10'),
('grace', 'active', '2023-11-30');