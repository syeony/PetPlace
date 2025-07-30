DROP DATABASE IF EXISTS petplace;
CREATE DATABASE petplace;
USE petplace;

-- ✅ Region Table
CREATE TABLE `Region` (
    `id` BIGINT NOT NULL COMMENT '지역 고유 ID (행정 표준 코드)',
    `region_name` VARCHAR(200) NOT NULL,
    `parent_id` BIGINT NULL,
    `geometry` GEOMETRY NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `Region`(`id`) ON DELETE SET NULL
);

-- ✅ User Table
CREATE TABLE `User` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(20) NOT NULL,
    `password` VARCHAR(200) NOT NULL,
    `name` VARCHAR(20) NOT NULL,
    `nickname` VARCHAR(20) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `deleted_at` DATETIME NULL,
    `rid` BIGINT NOT NULL,
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
    UNIQUE KEY uq_user_userid (`user_id`),
    UNIQUE KEY uq_user_nickname (`nickname`),
    UNIQUE KEY uq_user_phone (`phone_number`),
    CHECK (`phone_number` REGEXP '^[0-9]+$'),
    FOREIGN KEY (`rid`) REFERENCES `Region`(`id`)
);

-- ✅ Pet Table
CREATE TABLE `Pet` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uid` INT NOT NULL,
    `pet_name` VARCHAR(20) NOT NULL,
    `animal` ENUM(
        'dog','cat','rabbit','hamster','guinea_pig','hedgehog','ferret',
        'bird','turtle','fish','reptile','amphibian','other'
    ) NOT NULL,
    `breed` ENUM(
        'pomeranian','maltese','poodle','chihuahua','bichon_frise',
        'shiba_inu','golden_retriever','labrador_retriever','siberian_husky',
        'dachshund','bulldog','cocker_spaniel','yorkshire_terrier',
        'korean_shorthair','russian_blue','siamese','persian',
        'scottish_fold','maine_coon','bengal','norwegian_forest',
        'netherland_dwarf','mini_rex','lionhead',
        'golden_hamster','dwarf_hamster','roborovski',
        'lovebird','cockatiel','budgerigar',
        'russian_tortoise','red_eared_slider',
        'leopard_gecko','bearded_dragon',
        'unknown'
    ) NOT NULL,
    `sex` ENUM('male','female') NOT NULL,
    `birthday` DATE NOT NULL,
    `pet_img_src` VARCHAR(500) NULL,
    `tnr` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_pet_uid_name (`uid`, `pet_name`),
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE
);

-- ✅ ChatRoom
CREATE TABLE `ChatRoom` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uid_1` INT NOT NULL,
    `uid_2` INT NOT NULL,
    `last_message` VARCHAR(1000) NOT NULL DEFAULT '',
    `last_message_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_chatroom_users (`uid_1`, `uid_2`),
    FOREIGN KEY (`uid_1`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`uid_2`) REFERENCES `User`(`id`) ON DELETE CASCADE
);

-- ✅ Chat
CREATE TABLE `Chat` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `crid` INT NOT NULL,
    `uid` INT NOT NULL,
    `message` VARCHAR(1000) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`crid`) REFERENCES `ChatRoom`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE
);

-- ✅ Feed
CREATE TABLE `Feed` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `content` TEXT NOT NULL,
    `uid` INT NOT NULL,
    `user_nick` VARCHAR(200) NOT NULL,
    `user_img` VARCHAR(500) NULL,
    `rid` BIGINT NOT NULL,
    `category` ENUM('mypet', 'share', 'info', 'any', 'review') NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    `like` INT NOT NULL DEFAULT 0,
    `view` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`rid`) REFERENCES `Region`(`id`)
);

-- ✅ Comment
CREATE TABLE `Comment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `cid` INT NULL,
    `fid` INT NOT NULL,
    `content` VARCHAR(200) NOT NULL,
    `uid` INT NOT NULL,
    `user_nick` VARCHAR(200) NOT NULL,
    `user_img` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`fid`) REFERENCES `Feed`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`cid`) REFERENCES `Comment`(`id`) ON DELETE CASCADE
);


-- ✅ Care
CREATE TABLE `Care` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `uid` INT NOT NULL,
    `user_nick` VARCHAR(200) NOT NULL,
    `user_img` VARCHAR(500) NULL,
    `rid` BIGINT NOT NULL,
    `category` ENUM('walk_want', 'walk_req', 'care_want', 'care_req') NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    `view` INT NOT NULL DEFAULT 0,
    `date` DATETIME NOT NULL,
    `start_time` DATETIME NULL,
    `end_time` DATETIME NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`rid`) REFERENCES `Region`(`id`)
);

-- ✅ Hotel
CREATE TABLE `Hotel` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `hotel_description` VARCHAR(1000) NOT NULL,
    `price_per_night` INT NOT NULL,
    `Field` ENUM('cat','dog') NOT NULL,
    PRIMARY KEY (`id`)
);

-- ✅ Hotel Reservation
CREATE TABLE `Hotel_Reservation` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uid` INT NOT NULL,
    `hid` INT NOT NULL,
    `pet` INT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_hotel_reservation (`uid`, `hid`, `pet`),
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`hid`) REFERENCES `Hotel`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`pet`) REFERENCES `Pet`(`id`) ON DELETE CASCADE
);

-- ✅ Badge
CREATE TABLE `Badge` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `badge_name` VARCHAR(20) NOT NULL,
    `description` VARCHAR(200) NOT NULL,
    PRIMARY KEY (`id`)
);

-- ✅ BadgeList
CREATE TABLE `BadgeList` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uid` INT NOT NULL,
    `bid` INT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_uid_bid (`uid`, `bid`),
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`bid`) REFERENCES `Badge`(`id`) ON DELETE CASCADE
);

-- ✅ Image
CREATE TABLE `Image` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `ref_id` INT NOT NULL,
    `ref_type` ENUM('feed', 'care', 'hotel', 'user', 'review', 'chat') NOT NULL,
    `img_src` VARCHAR(500) NOT NULL,
    `sort` INT NOT NULL,
    PRIMARY KEY (`id`)
);

-- ✅ MessageRead
CREATE TABLE `MessageRead` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `crid` INT NOT NULL,
    `cid` INT NOT NULL,
    `uid` INT NOT NULL,
    `read_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_uid_cid (`uid`, `cid`),
    FOREIGN KEY (`crid`) REFERENCES `ChatRoom`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`cid`) REFERENCES `Chat`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE
);

-- ✅ Like
CREATE TABLE `Like` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `fid` INT NOT NULL,
    `uid` INT NOT NULL,
    `liked_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_uid_fid (`uid`, `fid`),
    FOREIGN KEY (`fid`) REFERENCES `Feed`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE
);

-- ✅ Place
CREATE TABLE `Place` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `rid` BIGINT NOT NULL,
    `place_name` VARCHAR(100) NOT NULL,
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
    UNIQUE KEY uq_place_name (`place_name`, `address`),
    FOREIGN KEY (`rid`) REFERENCES `Region`(`id`)
);

-- ✅ Review
CREATE TABLE `Review` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `pid` INT NOT NULL,
    `rid` BIGINT NOT NULL,
    `uid` INT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_review_unique (`pid`, `uid`, `title`),
    FOREIGN KEY (`pid`) REFERENCES `Place`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`rid`) REFERENCES `Region`(`id`)
);

-- ✅ Tag
CREATE TABLE `Tag` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tag_name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_tag_name (`tag_name`)
);

-- ✅ Hashtag
CREATE TABLE `Hashtag` (
    `fid` INT NOT NULL,
    `tid` INT NOT NULL,
    PRIMARY KEY (`fid`, `tid`),
    FOREIGN KEY (`fid`) REFERENCES `Feed`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tid`) REFERENCES `Tag`(`id`) ON DELETE CASCADE
);

-- ✅ ReadMe
CREATE TABLE `ReadMe` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uid` INT NOT NULL,
    `intro` VARCHAR(2000) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_readme_uid (`uid`),
    FOREIGN KEY (`uid`) REFERENCES `User`(`id`) ON DELETE CASCADE
);
