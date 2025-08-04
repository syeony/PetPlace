DROP DATABASE IF EXISTS petplace_local;
CREATE DATABASE petplace_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE petplace_local;


-- 2. 지역 테이블 (regions)
CREATE TABLE `regions` (
    `id` BIGINT NOT NULL COMMENT '지역 고유 ID (행정 표준 코드)',
    `name` VARCHAR(200) NOT NULL,
    `parent_id` BIGINT NULL,
    `geometry` GEOMETRY NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `regions`(`id`) ON DELETE SET NULL
);

-- 3. 사용자 테이블 (users) - 메인 테이블
CREATE TABLE `users` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_name` VARCHAR(20) NOT NULL,
    `password` VARCHAR(200) NOT NULL,
    `name` VARCHAR(20) NOT NULL,
    `nickname` VARCHAR(20) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `deleted_at` DATETIME NULL,
    `region_id` BIGINT NOT NULL,
    `default_pet_id` INT NULL,
    `kakao_oauth` VARCHAR(200) NULL,
    `user_img_src` VARCHAR(500) NULL,
    `pet_smell` DECIMAL(4,1) NOT NULL DEFAULT 36.5,
    `default_badge_id` INT NULL,
    `ci` VARCHAR(88) NOT NULL UNIQUE,
    `phone_number` VARCHAR(20) NOT NULL,
    `gender` ENUM('male', 'female') NOT NULL,
    `birthday` DATE NOT NULL,
    `is_foreigner` TINYINT NULL DEFAULT 0,
    `level` INT NOT NULL DEFAULT 1,
    `experience` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_user_user_name` (`user_name`),
    UNIQUE KEY `uq_user_nickname` (`nickname`),
    UNIQUE KEY `uq_user_phone` (`phone_number`),
    CHECK (`phone_number` REGEXP '^[0-9]+$'),
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- 4. 리프레시 토큰 테이블 (refresh_tokens)
CREATE TABLE `refresh_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(20) NOT NULL,
    `refresh_token` VARCHAR(500) NOT NULL UNIQUE,
    `expires_at` DATETIME NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_name`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_refresh_token` (`refresh_token`)
);




-- 기본 광역시/도
INSERT INTO regions (id, name, parent_id, geometry) VALUES 
(1100000000, '서울특별시', NULL, ST_GeomFromText('POINT(126.9784 37.5667)')),
(4100000000, '경기도', NULL, ST_GeomFromText('POINT(127.5183 37.2741)')),
(4700000000, '경상북도', NULL, ST_GeomFromText('POINT(128.9056 36.4919)'));

-- 주요 시군구 (구미 포함)
INSERT INTO regions (id, name, parent_id, geometry) VALUES 
(1111000000, '종로구', 1100000000, ST_GeomFromText('POINT(126.9792 37.5730)')),
(4111000000, '수원시', 4100000000, ST_GeomFromText('POINT(127.0286 37.2636)')),
(4719000000, '구미시', 4700000000, ST_GeomFromText('POINT(128.3445 36.1190)'));


-- 데이터 확인
SELECT * FROM regions ORDER BY id;

-- 구미시 확인
SELECT * FROM regions WHERE name = '구미시';

-- 계층구조 확인
SELECT p.name as 상위지역, r.name as 하위지역 
FROM regions r 
LEFT JOIN regions p ON r.parent_id = p.id;
