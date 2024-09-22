CREATE TABLE file_entity (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             file_name VARCHAR(255),
                             file_type VARCHAR(255),
                             size BIGINT,
                             content BLOB
);