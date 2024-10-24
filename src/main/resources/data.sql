-- -- org 테이블 초기 데이터
-- INSERT INTO org (id, org_name) VALUES (1, 'GA01');
-- INSERT INTO org (id, org_name) VALUES (2, 'GA02');
--
-- -- saas 테이블 초기 데이터
-- INSERT INTO saas (id, saas_name) VALUES (1, 'slack');
-- INSERT INTO saas (id, saas_name) VALUES (2, 'jira');
--
-- -- workspace_config 테이블 초기 데이터
-- INSERT INTO workspace_config (id, saas_admin_email, saas_name, register_date, token)
-- VALUES (1, 'test@gmail.com', 'psh_slacktest', CURRENT_TIMESTAMP, 'slack_test_token');
-- INSERT INTO workspace_config (id, saas_admin_email, saas_name, register_date, token)
-- VALUES (2, 'test3@gmail.com', '구름망', CURRENT_TIMESTAMP, 'grummmang_token');
-- INSERT INTO workspace_config (id, saas_admin_email, saas_name, register_date, token)
-- VALUES (3, 'test2@gmail.com', 'psh_slacktest', CURRENT_TIMESTAMP, 'jira_test_token');
--
-- -- admin 테이블 초기 데이터
-- INSERT INTO admin (id, org_id, email, password, first_name, last_name, last_login)
-- VALUES (1, 1, 'test@gmail.com', '1234', 'Jane', 'Doe', CURRENT_TIMESTAMP);
-- INSERT INTO admin (id, org_id, email, password, first_name, last_name, last_login)
-- VALUES (2, 1, 'test2@gmail.com', '1111', 'Rovert', 'Kim', CURRENT_TIMESTAMP);
-- INSERT INTO admin (id, org_id, email, password, first_name, last_name, last_login)
    -- VALUES (3, 2, 'test3@gmail.com', '1122', 'Steve', 'Park', CURRENT_TIMESTAMP);
--
-- -- org_saas 테이블 초기 데이터
-- INSERT INTO org_saas (org_id, saas_id, status, space_id, config, security_score)
-- VALUES (1, 1, 1, 'T077VP0SP2M', 1, 90);
-- INSERT INTO org_saas (id, org_id, saas_id, status, space_id, config, security_score)
-- VALUES (2, 1, 2, 1, 'T077VP0SP2C', 2, 95);
-- INSERT INTO org_saas (id, org_id, saas_id, status, space_id, config, security_score)
-- VALUES (3, 2, 1, 1, 'T077VP0SP2A', 3, 100);
--
-- -- file_upload 테이블 초기 데이터
-- INSERT INTO file_upload (id, org_saas_id, saas_file_id, hash, upload_ts)
-- VALUES (1, 1, 'F077H3F3Y3C', '448c7279580c6825ef9b3c2386f5a74a2e1a7c3808b2c7061a7f5396a2f30041', CURRENT_TIMESTAMP);
-- INSERT INTO file_upload (id, org_saas_id, saas_file_id, hash, upload_ts)
-- VALUES (2, 1, 'F0772GD9ACX', '0bdd32034e5dd6bc5511c466ad693675419140d37aef805874722d9f1e313a47', CURRENT_TIMESTAMP);

-- -- stored_file 테이블 초기 데이터
-- INSERT INTO stored_file (id, salted_hash, size, type, save_path)
-- VALUES (1, '448c7279580c6825ef9b3c2386f5a74a2e1a7c3808b2c7061a7f5396a2f30041', 1262, 'quip', 'downloaded_files\\slack\\psh_slacktest\\unknown_channel\\________________');
-- INSERT INTO stored_file (id, salted_hash, size, type, save_path)
-- VALUES (2, '0bdd32034e5dd6bc5511c466ad693675419140d37aef805874722d9f1e313a47', 1513, 'quip', 'downloaded_files\\slack\\psh_slacktest\\__\\____________');
--
-- -- gscan 테이블 초기 데이터
-- INSERT INTO gscan (id, file_id, step2_detail, detected)
-- VALUES (1, 1, 'gscan_test1', true);
-- INSERT INTO gscan (id, file_id, step2_detail, detected)
-- VALUES (2, 2, 'gscan_test2', true);
--
-- -- dlp_report 테이블 초기 데이터
-- INSERT INTO dlp_report (id, file_id, dlp, result_data)
-- VALUES (1, 1, 1, 'dlp_test1');
-- INSERT INTO dlp_report (id, file_id, dlp, result_data)
-- VALUES (2, 2, 1, 'dlp_test2');
--
-- -- file_status 테이블 초기 데이터
-- INSERT INTO file_status (id, file_id, gscan_status, dlp_status, vt_status)
-- VALUES (1, 1, 1, 1, 1);
-- INSERT INTO file_status (id, file_id, gscan_status, dlp_status, vt_status)
-- VALUES (2, 2, 1, 1, 1);
--
-- -- vt_report 테이블 초기 데이터
-- INSERT INTO vt_report (id, file_id, type, V3, ALYac, Kaspersky, Falcon, Avast, SentinelOne, detect_engine, complete_engine, score, threat_label, report_url)
-- VALUES (1, 1, 'pdf', 'undetected', 'Trojan', 'undetected', 'undetected', 'undetected', 'type-unsupported', 1, 64, 0, 'Trojan', 'https://www.virustotal.com/gui/file/01051cbba9e2d21c4311478e4aced5cc92aab546fdacf1c8d85e3732603bdf2c');
-- INSERT INTO vt_report (id, file_id, type, V3, ALYac, Kaspersky, Falcon, Avast, SentinelOne, detect_engine, complete_engine, score, threat_label, report_url)
-- VALUES (2, 2, 'pdf', 'Trojan', 'undetected', 'undetected', 'undetected', 'undetected', 'undetected', 1, 65, 0, 'Trojan', 'https://www.virustotal.com/gui/file/01051cbba9e2d21c4311478e4aced5cc92aab546fdacf1c8d85e3732603bdf2c');
--
-- -- monitored_users 테이블 초기 데이터
-- INSERT INTO monitored_users (id, user_id, org_saas_id, email, user_name, status)
-- VALUES (1, 'USLACKBOT', 1, NULL, 'Slackbot', NULL);
-- INSERT INTO monitored_users (id, user_id, org_saas_id, email, user_name, status)
-- VALUES (2, 'U0772GD6Q5D', 1, 'hsp003636@gmail.com', 'hsp003636', NULL);
-- INSERT INTO monitored_users (id, user_id, org_saas_id, email, user_name, status)
-- VALUES (3, 'U078632QNU8', 1, NULL, NULL, NULL);
-- INSERT INTO monitored_users (id, user_id, org_saas_id, email, user_name, status)
-- VALUES (4, 'U078CD6M9U6', 1, NULL, NULL, NULL);
-- INSERT INTO monitored_users (id, user_id, org_saas_id, email, user_name, status)
-- VALUES (5, 'U0797S2BLN4', 1, NULL, 'slack_app_example', NULL);
-- INSERT INTO monitored_users (id, user_id, org_saas_id, email, user_name, status)
-- VALUES (6, 'U07BKG7CA9G', 1, NULL, 'slack_spring_test', NULL);
--
-- -- activities 테이블 초기 데이터
-- INSERT INTO activities (id, user_id, event_type, saas_file_id, file_name, event_ts, upload_channel)
-- VALUES (1, 'USLACKBOT', 'file_uploaded', 'F077H3F3Y3C', '________________', CURRENT_TIMESTAMP, 'slack/psh_slacktest/unknown_channel/Slackbot');
-- INSERT INTO activities (id, user_id, event_type, saas_file_id, file_name, event_ts, upload_channel)
-- VALUES (2, 'USLACKBOT', 'file_uploaded', 'F0772GD9ACX', '____________', CURRENT_TIMESTAMP, 'slack/psh_slacktest/소셜/Slackbot');
--
-- -- channel_list 테이블 초기 데이터
-- INSERT INTO channel_list (id, org_saas_id, channel_id, channel_name)
-- VALUES (1, 1, 'C0772GD7VE3', '소셜');
-- INSERT INTO channel_list (id, org_saas_id, channel_id, channel_name)
-- VALUES (2, 1, 'C077E6F9JKF', '개발');
-- INSERT INTO channel_list (id, org_saas_id, channel_id, channel_name)
-- VALUES (3, 1, 'C0785SRRG7J', 'slack-전체');
-- INSERT INTO channel_list (id, org_saas_id, channel_id, channel_name)
-- VALUES (4, 1, 'C07C8EFTB32', '슬랙테스트');
-- INSERT INTO channel_list (id, org_saas_id, channel_id, channel_name)
-- VALUES (5, 1, 'C07CDU783EW', '채널생성');
-- INSERT INTO channel_list (id, org_saas_id, channel_id, channel_name)
-- VALUES (6, 1, 'C07D3RYEEQG', '플랜테스트');
