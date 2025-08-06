DROP DATABASE IF EXISTS petplace;
CREATE DATABASE petplace;
USE petplace;

-- ✅ Region
CREATE TABLE `regions` (
    `id` BIGINT NOT NULL COMMENT '지역 고유 ID (행정 표준 코드)',
    `name` VARCHAR(200) NOT NULL,
    `parent_id` BIGINT NULL,
    `geometry` GEOMETRY NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `regions`(`id`) ON DELETE SET NULL
);

-- ✅ User
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
    UNIQUE KEY uq_user_user_name (`user_name`),
    UNIQUE KEY uq_user_nickname (`nickname`),
    UNIQUE KEY uq_user_phone (`phone_number`),
    CHECK (`phone_number` REGEXP '^[0-9]+$'),
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- ✅ RefreshToken
CREATE TABLE `refresh_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `refresh_token` VARCHAR(500) NOT NULL UNIQUE,
    `expires_at` DATETIME NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX idx_user_id (`user_id`),
    INDEX idx_refresh_token (`refresh_token`)
);


-- ✅ Pet
CREATE TABLE `pets` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
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

-- ✅ ChatRoom
CREATE TABLE `chat_rooms` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id_1` INT NOT NULL,
    `user_id_2` INT NOT NULL,
    `last_message` VARCHAR(1000) NOT NULL DEFAULT '',
    `last_message_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_chatroom_users (`user_id_1`, `user_id_2`),
    FOREIGN KEY (`user_id_1`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id_2`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Chat
CREATE TABLE `chats` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `chat_rooms_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `message` VARCHAR(1000) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`chat_rooms_id`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Feed
CREATE TABLE `feeds` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `content` TEXT NOT NULL,
    `user_id` INT NOT NULL,
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

-- ✅ Comment
CREATE TABLE `comments` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `parent_comment_id` INT NULL,
    `feed_id` INT NOT NULL,
    `content` VARCHAR(200) NOT NULL,
    `user_id` INT NOT NULL,
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


-- ✅ Care
CREATE TABLE `cares` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `user_id` INT NOT NULL,
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

-- ✅ Hotel Reservation
CREATE TABLE `hotel_reservations` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
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

-- ✅ BadgeList
CREATE TABLE `badge_lists` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
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

-- ✅ UserChatRoom
CREATE TABLE `user_chat_rooms` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `chat_room_id` INT NOT NULL,  -- 채팅방 인덱스
    `user_id` INT NOT NULL,   -- 유저 인덱스
    `last_read_cid` INT NULL,   -- 마지막으로 읽은 메시지의 id
    `leave_at` DATETIME NULL,   -- 방 나간 시간 (참여중이면 NULL)
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_crid_uid (`chat_room_id`, `user_id`),
    FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);


-- ✅ Like
CREATE TABLE `likes` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `feed_id` INT NOT NULL,
    `user_id` INT NOT NULL,
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

-- ✅ Review
CREATE TABLE `reviews` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `place_id` INT NOT NULL,
    `region_id` BIGINT NOT NULL,
    `user_id` INT NOT NULL,
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

-- ✅ Introduction
CREATE TABLE `introduction` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `content` VARCHAR(2000) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_intro_uid (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

commit;
