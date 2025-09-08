DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS processed_person;

CREATE TABLE person (
    id BIGINT NOT NULL PRIMARY KEY,
    last_name VARCHAR(20),
    first_name VARCHAR(20)
);

CREATE TABLE processed_person (
    id BIGINT NOT NULL PRIMARY KEY,
    last_name VARCHAR(20),
    first_name VARCHAR(20)
);
