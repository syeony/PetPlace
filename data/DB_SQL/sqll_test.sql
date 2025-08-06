

DROP DATABASE IF EXISTS petplace_local;
CREATE DATABASE petplace_local;
USE petplace_local;

-- ✅ Region
CREATE TABLE `regions` (
    `id` BIGINT NOT NULL COMMENT '지역 고유 ID (행정 표준 코드)',
    `name` VARCHAR(200) NOT NULL,
    `parent_id` BIGINT NULL,
    `geometry` GEOMETRY NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `regions`(`id`) ON DELETE SET NULL
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


-- ✅ User (소셜 로그인 지원으로 수정)
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 ID (Long 타입)',
    `user_name` VARCHAR(20) NOT NULL COMMENT '로그인용 사용자명',
    `password` VARCHAR(200) NULL COMMENT '비밀번호 (소셜 로그인 시 NULL 가능)', 
    `name` VARCHAR(20) NOT NULL COMMENT '실명',
    `nickname` VARCHAR(20) NOT NULL COMMENT '닉네임',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '가입일시',
    `deleted_at` DATETIME NULL COMMENT '탈퇴일시',
    `region_id` BIGINT NOT NULL COMMENT '지역 ID',
    `default_pet_id` INT NULL COMMENT '대표 반려동물 ID',
    
    -- [수정] 소셜 로그인 관련 컬럼들
    `login_type` ENUM('EMAIL', 'KAKAO', 'NAVER', 'GOOGLE') NOT NULL DEFAULT 'EMAIL' COMMENT '로그인 타입',
    `social_id` VARCHAR(200) NULL COMMENT '소셜 플랫폼 고유 ID',
    `social_email` VARCHAR(100) NULL COMMENT '소셜 계정 이메일',
    
    `user_img_src` VARCHAR(500) NULL COMMENT '프로필 이미지',
    `pet_smell` DECIMAL(4,1) NOT NULL DEFAULT 36.5 COMMENT '펫 온도',
    `default_badge_id` INT NULL COMMENT '대표 뱃지 ID',
    `ci` VARCHAR(88) NOT NULL COMMENT '본인인증 고유키 (CI)',
    `phone_number` VARCHAR(20) NOT NULL COMMENT '휴대폰 번호',
    `gender` ENUM('male', 'female') NOT NULL COMMENT '성별',
    `birthday` DATE NOT NULL COMMENT '생년월일',
    `is_foreigner` TINYINT NULL DEFAULT 0 COMMENT '외국인 여부',
    `level` INT NOT NULL DEFAULT 1 COMMENT '사용자 레벨',
    `experience` INT NOT NULL DEFAULT 0 COMMENT '경험치',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_user_user_name (`user_name`),
    UNIQUE KEY uq_user_nickname (`nickname`),
    UNIQUE KEY uq_user_phone (`phone_number`),
    UNIQUE KEY uq_user_ci (`ci`),
    UNIQUE KEY uq_user_social_id (`social_id`), -- 소셜 ID 고유 제약
    
    CHECK (`phone_number` REGEXP '^[0-9]+$'),
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`),
    
    -- 소셜 로그인 관련 제약조건
    CONSTRAINT chk_social_login CHECK (
        (login_type = 'EMAIL' AND social_id IS NULL) OR 
        (login_type IN ('KAKAO', 'NAVER', 'GOOGLE') AND social_id IS NOT NULL)
    ),
    CONSTRAINT chk_password_requirement CHECK (
        (login_type = 'EMAIL' AND password IS NOT NULL) OR 
        (login_type IN ('KAKAO', 'NAVER', 'GOOGLE'))
    )
) COMMENT '사용자 테이블 (소셜 로그인 지원)';

-- ✅ RefreshToken (사용자 ID 타입 변경)
CREATE TABLE `refresh_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '사용자 ID (Long 타입으로 변경)',
    `refresh_token` VARCHAR(500) NOT NULL UNIQUE COMMENT 'Refresh Token',
    `expires_at` DATETIME NOT NULL COMMENT '만료 시간',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '생성 시간',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX idx_user_id (`user_id`),
    INDEX idx_refresh_token (`refresh_token`)
) COMMENT 'Refresh Token 저장 테이블';

-- ✅ Pet (user_id 타입 변경)
CREATE TABLE `pets` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '사용자 ID (Long 타입)',
    `name` VARCHAR(20) NOT NULL,
    `animal` ENUM(
        'DOG','CAT','RABBIT','HAMSTER','GUINEA_PIG','HEDGEHOG','FERRET',
        'BIRD','TURTLE','FISH','REPTILE','AMPHIBIAN','OTHER'
    ) NOT NULL,
    `breed` ENUM(
        'POMERANIAN','MALTESE','POODLE','CHIHUAHUA','BICHON_FRISE',
        'SHIBA_INU','GOLDEN_RETRIEVER','LABRADOR_RETRIEVER','SIBERIAN_HUSKY',
        'DACHSHUND','BULLDOG','COCKER_SPANIEL','YORKSHIRE_TERRIER',
        'KOREAN_SHORTHAIR','RUSSIAN_BLUE','SIAMESE','PERSIAN',
        'SCOTTISH_FOLD','MAINE_COON','BENGAL','NORWEGIAN_FOREST',
        'NETHERLAND_DWARF','MINI_REX','LIONHEAD',
        'GOLDEN_HAMSTER','DWARF_HAMSTER','ROBOROVSKI',
        'LOVEBIRD','COCKATIEL','BUDGERIGAR',
        'RUSSIAN_TORTOISE','RED_EARED_SLIDER',
        'LEOPARD_GECKO','BEARDED_DRAGON',
        'UNKNOWN'
    ) NOT NULL,
    `sex` ENUM('MALE','FEMALE') NOT NULL,
    `birthday` DATE NOT NULL,
    `img_src` VARCHAR(500) NULL,
    `tnr` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_pet_uid_name (`user_id`, `name`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ ChatRoom (user_id 타입 변경)
CREATE TABLE `chat_rooms` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id_1` BIGINT NOT NULL,
    `user_id_2` BIGINT NOT NULL,
    `last_message` VARCHAR(1000) NOT NULL DEFAULT '',
    `last_message_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_chatroom_users (`user_id_1`, `user_id_2`),
    FOREIGN KEY (`user_id_1`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id_2`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Chat (user_id 타입 변경)
CREATE TABLE `chats` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `chat_rooms_id` INT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `message` VARCHAR(1000) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`chat_rooms_id`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Feed (user_id 타입 변경)
CREATE TABLE `feeds` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `content` TEXT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_nick` VARCHAR(200) NOT NULL,
    `user_img` VARCHAR(500) NULL,
    `region_id` BIGINT NOT NULL,
    `category` ENUM('MYPET', 'SHARE', 'INFO', 'ANY', 'REVIEW') NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    `likes` INT NOT NULL DEFAULT 0,
    `views` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- ✅ Comment (user_id 타입 변경)
CREATE TABLE `comments` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `parent_comment_id` INT NULL,
    `feed_id` INT NOT NULL,
    `content` VARCHAR(200) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_nick` VARCHAR(200) NOT NULL,
    `user_img` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`feed_id`) REFERENCES `feeds`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_comment_id`) REFERENCES `comments`(`id`) ON DELETE CASCADE
);

-- ✅ Care (user_id 타입 변경)
CREATE TABLE `cares` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_nick` VARCHAR(200) NOT NULL,
    `user_img` VARCHAR(500) NULL,
    `region_id` BIGINT NOT NULL,
    `category` ENUM('WALK_WANT', 'WALK_REQ', 'CARE_WANT', 'CARE_REQ') NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    `views` INT NOT NULL DEFAULT 0,
    `date` DATETIME NOT NULL,
    `start_time` DATETIME NULL,
    `end_time` DATETIME NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- ✅ Hotel
CREATE TABLE `hotels` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `description` VARCHAR(1000) NOT NULL,
    `price_per_night` INT NOT NULL,
    `field` ENUM('CAT','DOG') NOT NULL,
    PRIMARY KEY (`id`)
);

-- ✅ Hotel Reservation (user_id 타입 변경)
CREATE TABLE `hotel_reservations` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `hotel_id` INT NOT NULL,
    `pet_id` INT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_hotel_reservation (`user_id`, `hotel_id`, `pet_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`hotel_id`) REFERENCES `hotels`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`pet_id`) REFERENCES `pets`(`id`) ON DELETE CASCADE
);

-- ✅ Badge
CREATE TABLE `badges` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(20) NOT NULL,
    `description` VARCHAR(200) NOT NULL,
    PRIMARY KEY (`id`)
);

-- ✅ BadgeList (user_id 타입 변경)
CREATE TABLE `badge_lists` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `bedge_id` INT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_uid_bid (`user_id`, `bedge_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`bedge_id`) REFERENCES `badges`(`id`) ON DELETE CASCADE
);

-- ✅ Image
CREATE TABLE `images` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `ref_id` INT NOT NULL,
    `ref_type` ENUM('FEED', 'CARE', 'HOTEL', 'USER', 'REVIEW', 'CHAT') NOT NULL,
    `src` VARCHAR(500) NOT NULL,
    `sort` INT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`)
);

-- ✅ UserChatRoom (user_id 타입 변경)
CREATE TABLE `user_chat_rooms` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `chat_room_id` INT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `last_read_cid` INT NULL,
    `leave_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_crid_uid (`chat_room_id`, `user_id`),
    FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Like (user_id 타입 변경)
CREATE TABLE `likes` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `feed_id` INT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `liked_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_uid_fid (`user_id`, `feed_id`),
    FOREIGN KEY (`feed_id`) REFERENCES `feeds`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Place
CREATE TABLE `places` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `region_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `category` ENUM('HOTEL', 'HOSPITAL', 'BEAUTY', 'CAFE', 'PARK') NOT NULL,
    `address` VARCHAR(300) NOT NULL,
    `latitude` DECIMAL(10, 8) NOT NULL,
    `longitude` DECIMAL(11, 8) NOT NULL,
    `phone_number` VARCHAR(20) NULL,
    `description` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_place_name (`name`, `address`),
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- ✅ Review (user_id 타입 변경)
CREATE TABLE `reviews` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `place_id` INT NOT NULL,
    `region_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_review_unique (`place_id`, `user_id`, `title`),
    FOREIGN KEY (`place_id`) REFERENCES `places`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- ✅ Tag
CREATE TABLE `tags` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_tag_name (`name`)
);

-- ✅ FeedTag
CREATE TABLE `feed_tags` (
    `feed_id` INT NOT NULL,
    `tag_id` INT NOT NULL,
    PRIMARY KEY (`feed_id`, `tag_id`),
    FOREIGN KEY (`feed_id`) REFERENCES `feeds`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE
);

-- ✅ Introduction (user_id 타입 변경)
CREATE TABLE `introduction` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `content` VARCHAR(2000) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_intro_uid (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- 🔍 소셜 로그인 관련 인덱스 추가 (성능 최적화)
CREATE INDEX idx_users_login_type ON users(login_type);
CREATE INDEX idx_users_social_id ON users(social_id);
CREATE INDEX idx_users_login_type_social_id ON users(login_type, social_id);

-- 📊 소셜 로그인 테스트 데이터 (선택적)
INSERT INTO regions (id, name, parent_id, geometry) VALUES 
(1, '서울특별시', NULL, ST_GeomFromText('POINT(126.9780 37.5665)'));

-- 일반 이메일 사용자 예시
INSERT INTO users (user_name, password, name, nickname, region_id, ci, phone_number, gender, birthday, login_type) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb0bKIppuetjm6P7eGdKz3u5ey.7BtGAO3t6xtxaG', '홍길동', '펫러버', 1, 'TEST_CI_001', '01012345678', 'male', '1990-01-01', 'EMAIL');

-- 카카오 사용자 예시
INSERT INTO users (user_name, password, name, nickname, region_id, ci, phone_number, gender, birthday, login_type, social_id, social_email) VALUES
('kakao_12345678', NULL, '김카카', '카카오유저', 1, 'TEST_CI_002', '01087654321', 'female', '1995-05-05', 'KAKAO', '12345678', 'user@kakao.com');

COMMIT;