-- org 테이블 초기 데이터
INSERT INTO org (id, org_name) VALUES (1, 'GA01');
INSERT INTO org (id, org_name) VALUES (2, 'GA02');

-- saas 테이블 초기 데이터
INSERT INTO saas (id, saas_name) VALUES (1, 'slack');
INSERT INTO saas (id, saas_name) VALUES (2, 'jira');

-- workspace_config 테이블 초기 데이터
INSERT INTO workspace_config (id, saas_admin_email, saas_name, register_date, token)
VALUES (1, 'test@gmail.com', 'psh_slacktest', CURRENT_TIMESTAMP, 'slack_test_token');
INSERT INTO workspace_config (id, saas_admin_email, saas_name, register_date, token)
VALUES (2, 'test3@gmail.com', '구름망', CURRENT_TIMESTAMP, 'grummmang_token');
INSERT INTO workspace_config (id, saas_admin_email, saas_name, register_date, token)
VALUES (3, 'test2@gmail.com', 'psh_slacktest', CURRENT_TIMESTAMP, 'jira_test_token');

-- admin 테이블 초기 데이터
INSERT INTO admin (id, org_id, email, password, first_name, last_name, last_login)
VALUES (1, 1, 'test@gmail.com', '1234', 'Jane', 'Doe', CURRENT_TIMESTAMP);
INSERT INTO admin (id, org_id, email, password, first_name, last_name, last_login)
VALUES (2, 1, 'test2@gmail.com', '1111', 'Rovert', 'Kim', CURRENT_TIMESTAMP);
INSERT INTO admin (id, org_id, email, password, first_name, last_name, last_login)
VALUES (3, 2, 'test3@gmail.com', '1122', 'Steve', 'Park', CURRENT_TIMESTAMP);

-- org_saas 테이블 초기 데이터
INSERT INTO org_saas (id, org_id, saas_id, status, space_id, config, security_score)
VALUES (1, 1, 1, 1, 'T077VP0SP2M', 1, 90);
INSERT INTO org_saas (id, org_id, saas_id, status, space_id, config, security_score)
VALUES (2, 1, 2, 1, 'T077VP0SP2C', 2, 95);
INSERT INTO org_saas (id, org_id, saas_id, status, space_id, config, security_score)
VALUES (3, 2, 1, 1, 'T077VP0SP2A', 3, 100);


-- space_list 테이블 초기 데이터