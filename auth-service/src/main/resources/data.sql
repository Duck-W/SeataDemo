-- 创建一个测试用户，用户名: admin，密码: admin123 (加密后的值)
INSERT INTO auth_user (username, password, role, created_at, updated_at) 
VALUES ('admin', '$2a$10$wVdP2MQ2GVG5p4aN7fHuku5.7NsdR.WA0./././kQqDK.eTqWQfG', 'ADMIN', NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;