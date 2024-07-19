CREATE TABLE IF NOT EXISTS org (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   org_name VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS saas (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    saas_name VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS workspace_config (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                saas_admin_email VARCHAR(100) NOT NULL,
    saas_name VARCHAR(100) NOT NULL,
    register_date TIMESTAMP NOT NULL,
    token VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS admin (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     org_id BIGINT NOT NULL,
                                     email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    last_login TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES org(id)
    );

CREATE TABLE IF NOT EXISTS org_saas (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        org_id BIGINT NOT NULL,
                                        saas_id BIGINT NOT NULL,
                                        status INT NOT NULL,
                                        space_id VARCHAR(255),
    config BIGINT NOT NULL,
    security_score INT,
    FOREIGN KEY (org_id) REFERENCES org(id),
    FOREIGN KEY (saas_id) REFERENCES saas(id),
    FOREIGN KEY (config) REFERENCES workspace_config(id)
    );

CREATE TABLE IF NOT EXISTS channel_list (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            org_saas_id BIGINT NOT NULL,
                                            channel_id VARCHAR(255),
    channel_name VARCHAR(255),
    FOREIGN KEY (org_saas_id) REFERENCES org_saas(id)
    );

CREATE TABLE IF NOT EXISTS monitored_users (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               user_id VARCHAR(100) UNIQUE NOT NULL,
    org_saas_id BIGINT NOT NULL,
    email VARCHAR(100),
    user_name VARCHAR(100),
    status BIGINT,
    FOREIGN KEY (org_saas_id) REFERENCES org_saas(id)
    );

CREATE TABLE IF NOT EXISTS activities (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          user_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100),
    saas_file_id VARCHAR(100),
    file_name VARCHAR(255),
    event_ts TIMESTAMP,
    upload_channel VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES monitored_users(user_id)
    );

CREATE TABLE stored_file (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             salted_hash VARCHAR(255) NOT NULL,
                             size BIGINT NOT NULL,
                             type VARCHAR(10) NOT NULL,
                             save_path VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS file_status (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           file_id BIGINT NOT NULL,
                                           gscan_status INT,
                                           dlp_status INT,
                                           vt_status INT,
                                           FOREIGN KEY (file_id) REFERENCES stored_file(id)
    );

CREATE TABLE IF NOT EXISTS file_upload (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           org_saas_id BIGINT NOT NULL,
                                           saas_file_id VARCHAR(255) UNIQUE NOT NULL,
    hash TEXT,
    upload_ts TIMESTAMP NOT NULL,
    FOREIGN KEY (org_saas_id) REFERENCES org_saas(id)
    );

CREATE TABLE IF NOT EXISTS vt_report (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         file_id BIGINT,
                                         type VARCHAR(255),
    V3 VARCHAR(255),
    ALYac VARCHAR(255),
    Kaspersky VARCHAR(255),
    Falcon VARCHAR(255),
    Avast VARCHAR(255),
    SentinelOne VARCHAR(255),
    detect_engine INT NOT NULL,
    complete_engine INT NOT NULL,
    score INT NOT NULL,
    threat_label VARCHAR(255),
    report_url TEXT NOT NULL,
    FOREIGN KEY (file_id) REFERENCES stored_file(id)
    );

CREATE TABLE IF NOT EXISTS dlp_report (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          file_id BIGINT NOT NULL,
                                          dlp BOOLEAN,
                                          result_data TEXT,
                                          FOREIGN KEY (file_id) REFERENCES stored_file(id)
    );

CREATE TABLE IF NOT EXISTS type_scan (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         file_id BIGINT NOT NULL,
                                         consistence BOOLEAN,
                                         mimetype VARCHAR(255),
    extension VARCHAR(255),
    FOREIGN KEY (file_id) REFERENCES file_upload(id)
    );

CREATE TABLE IF NOT EXISTS gscan (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     file_id BIGINT NOT NULL,
                                     step2_detail TEXT,
                                     detected BOOLEAN,
                                     FOREIGN KEY (file_id) REFERENCES stored_file(id)
    );
