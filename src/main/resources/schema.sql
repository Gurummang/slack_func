CREATE TABLE IF NOT EXISTS org (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   org_name VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS saas (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    saas_name VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS saas_config (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           saas_admin_email VARCHAR(255),
    nickname VARCHAR(255),
    register_date TIMESTAMP,
    saas_alias VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS admin (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     org_id BIGINT,
                                     email VARCHAR(255),
    password VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    last_login TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES org(id)
    );

CREATE TABLE IF NOT EXISTS org_saas (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        org_id BIGINT,
                                        saas_id BIGINT,
                                        status INT,
                                        config_file BIGINT,
                                        security_score INT,
                                        FOREIGN KEY (org_id) REFERENCES org(id),
    FOREIGN KEY (saas_id) REFERENCES saas(id),
    FOREIGN KEY (config_file) REFERENCES saas_config(id)
    );
