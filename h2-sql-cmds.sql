-- open h2-console at http://localhost:9002/h2-console
-- run below commands to test is h2 is configured properly
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);


INSERT INTO users (id, name)
VALUES (1, 'John Doe');

INSERT INTO users (id, name)
VALUES (2, 'Will Smith');

SELECT * FROM users;


