DROP DATABASE IF EXISTS petplace;
CREATE DATABASE petplace;
USE petplace;

-- safe 모드 비활성화 
SET SQL_SAFE_UPDATES = 0;

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
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 ID',
    `user_name` VARCHAR(20) NOT NULL COMMENT '로그인용 사용자명',
    `password` VARCHAR(200) NULL COMMENT '비밀번호 (소셜 로그인 시 NULL)',
    `name` VARCHAR(20) NOT NULL COMMENT '실명',
    `nickname` VARCHAR(20) NOT NULL COMMENT '닉네임',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '가입일시',
    `deleted_at` DATETIME NULL COMMENT '탈퇴일시',
    `region_id` BIGINT NOT NULL COMMENT '지역 ID',
    `default_pet_id` BIGINT NULL COMMENT '대표 반려동물 ID',
    `login_type` ENUM('EMAIL', 'KAKAO', 'NAVER', 'GOOGLE') NOT NULL DEFAULT 'EMAIL' COMMENT '로그인 타입',
    `social_id` VARCHAR(200) NULL COMMENT '소셜 플랫폼 고유 ID',
    `social_email` VARCHAR(100) NULL COMMENT '소셜 계정 이메일',
    `user_img_src` VARCHAR(500) NULL COMMENT '프로필 이미지',
    `pet_smell` DECIMAL(4,1) NOT NULL DEFAULT 36.5 COMMENT '펫 온도',
    `default_badge_id` BIGINT NULL COMMENT '대표 뱃지 ID',
    `ci` VARCHAR(88) NOT NULL COMMENT '본인인증 고유키 (CI)',
    `phone_number` VARCHAR(20) NOT NULL COMMENT '휴대폰 번호',
    `gender` ENUM('male', 'female') NOT NULL COMMENT '성별',
    `birthday` DATE NOT NULL COMMENT '생년월일',
    `is_foreigner` TINYINT NULL DEFAULT 0 COMMENT '외국인 여부',
    `level` INT NOT NULL DEFAULT 1 COMMENT '사용자 레벨',
    `experience` INT NOT NULL DEFAULT 0 COMMENT '경험치',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_user_user_name` (`user_name`),
    UNIQUE KEY `uq_user_nickname` (`nickname`),
    UNIQUE KEY `uq_user_phone` (`phone_number`),
    UNIQUE KEY `uq_user_ci` (`ci`),
    UNIQUE KEY `uq_user_social_id` (`social_id`),
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`),
    CONSTRAINT `chk_social_login` CHECK ((`login_type` = 'EMAIL' AND `social_id` IS NULL) OR (`login_type` IN ('KAKAO', 'NAVER', 'GOOGLE') AND `social_id` IS NOT NULL)),
    CONSTRAINT `chk_password_requirement` CHECK ((`login_type` = 'EMAIL' AND `password` IS NOT NULL) OR (`login_type` IN ('KAKAO', 'NAVER', 'GOOGLE')))
) COMMENT '사용자 테이블 (소셜 로그인 지원)';

-- ✅ RefreshToken
CREATE TABLE `refresh_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
    `refresh_token` VARCHAR(500) NOT NULL UNIQUE COMMENT 'Refresh Token',
    `expires_at` DATETIME NOT NULL COMMENT '만료 시간',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '생성 시간',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_refresh_token` (`refresh_token`)
) COMMENT 'Refresh Token 저장 테이블';

-- ✅ Pet
CREATE TABLE `pets` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
    `name` VARCHAR(20) NOT NULL,
    `animal` ENUM('DOG','CAT','RABBIT','HAMSTER','GUINEA_PIG','HEDGEHOG','FERRET','BIRD','TURTLE','FISH','REPTILE','AMPHIBIAN','OTHER') NOT NULL,
    `breed` ENUM(
    -- DOG
    'AFFENPINSCHER',
    'AFGHAN_HOUND',
    'AIREDALE',
    'AKITA',
    'APPENZELLER',
    'AUSTRALIAN_TERRIER',
    'BASENJI',
    'BASSET',
    'BEAGLE',
    'BEDLINGTON_TERRIER',
    'BERNESE_MOUNTAIN_DOG',
    'BLACK_AND_TAN_COONHOUND',
    'BLENHEIM_SPANIEL',
    'BLOODHOUND',
    'BORDER_COLLIE',
    'BORDER_TERRIER',
    'BORZOI',
    'BOSTON_BULL',
    'BOUVIER_DES_FLANDRES',
    'BOXER',
    'BRABANCON_GRIFFON',
    'BRIARD',
    'BRITTANY_SPANIEL',
    'BULL_MASTIFF',
    'CAIRN',
    'CARDIGAN',
    'CHESAPEAKE_BAY_RETRIEVER',
    'CHIHUAHUA',
    'CHOW',
    'CLUMBER',
    'COCKER_SPANIEL',
    'COLLIE',
    'CURLY_COATED_RETRIEVER',
    'DANDIE_DINMONT',
    'DHOLE',
    'DINGO',
    'DOBERMAN',
    'ENGLISH_FOXHOUND',
    'ENGLISH_SETTER',
    'ENGLISH_SPRINGER',
    'ENTLEBUCHER',
    'ESKIMO_DOG',
    'FLAT_COATED_RETRIEVER',
    'FRENCH_BULLDOG',
    'GERMAN_SHEPHERD',
    'GERMAN_SHORT_HAIRED_POINTER',
    'GIANT_SCHNAUZER',
    'GOLDEN_RETRIEVER',
    'GORDON_SETTER',
    'GREAT_DANE',
    'GREAT_PYRENEES',
    'GREATER_SWISS_MOUNTAIN_DOG',
    'GROENENDAEL',
    'IBIZAN_HOUND',
    'IRISH_SETTER',
    'IRISH_TERRIER',
    'IRISH_WATER_SPANIEL',
    'IRISH_WOLFHOUND',
    'ITALIAN_GREYHOUND',
    'JAPANESE_SPANIEL',
    'KEESHOND',
    'KELPIE',
    'KERRY_BLUE_TERRIER',
    'KOMONDOR',
    'KUVASZ',
    'LABRADOR_RETRIEVER',
    'LAKELAND_TERRIER',
    'LEONBERG',
    'LHASA',
    'MALAMUTE',
    'MALINOIS',
    'MALTESE_DOG',
    'MEXICAN_HAIRLESS',
    'MINIATURE_PINSCHER',
    'MINIATURE_POODLE',
    'MINIATURE_SCHNAUZER',
    'NEWFOUNDLAND',
    'NORFOLK_TERRIER',
    'NORWEGIAN_ELKHOUND',
    'NORWICH_TERRIER',
    'OLD_ENGLISH_SHEEPDOG',
    'OTTERHOUND',
    'PAPILLON',
    'PEKINESE',
    'PEMBROKE',
    'POMERANIAN',
    'PUG',
    'REDBONE',
    'RHODESIAN_RIDGEBACK',
    'ROTTWEILER',
    'SAINT_BERNARD',
    'SALUKI',
    'SAMOYED',
    'SCHIPPERKE',
    'SCOTCH_TERRIER',
    'SCOTTISH_DEERHOUND',
    'SEALYHAM_TERRIER',
    'SHETLAND_SHEEPDOG',
    'SHIH_TZU',
    'SIBERIAN_HUSKY',
    'SILKY_TERRIER',
    'SOFT_COATED_WHEATEN_TERRIER',
    'STAFFORDSHIRE_BULLTERRIER',
    'STANDARD_POODLE',
    'STANDARD_SCHNAUZER',
    'SUSSEX_SPANIEL',
    'TIBETAN_MASTIFF',
    'TIBETAN_TERRIER',
    'TOY_POODLE',
    'TOY_TERRIER',
    'VIZSLA',
    'WALKER_HOUND',
    'WEIMARANER',
    'WELSH_SPRINGER_SPANIEL',
    'WEST_HIGHLAND_WHITE_TERRIER',
    'WHIPPET',
    'WIRE_HAIRED_FOX_TERRIER',
    'YORKSHIRE_TERRIER',

    -- CAT
    'KOREAN_SHORTHAIR',
    'RUSSIAN_BLUE',
    'PERSIAN',
    'SIAMESE',
    'MUNCHKIN',
    'SCOTTISH_FOLD',
    'RAGDOLL',
    'KOREAN_MEDIUMHAIR',
    'AMERICAN_SHORTHAIR',
    'DOMESTIC_LONG_HAIR',
    'TORTOISESHELL',
    'CALICO',
    'TORBIE',
    'DILUTE_CALICO',
    'TUXEDO',
    'DILUTE_TORTOISESHELL',
    'TABBY',
    'MAINE_COON',
    'BENGAL',
    'NORWEGIAN_FOREST',

    -- RABBIT
    'NETHERLAND_DWARF',
    'MINI_REX',
    'LIONHEAD',

    -- HAMSTER
    'GOLDEN_HAMSTER',
    'TEDDY_BEAR_HAMSTER',
    'CAMPBELL_DWARF',
    'WINTER_WHITE_DWARF',
    'PEARL_WINTER_WHITE_DWARF',
    'ROBOROVSKI_DWARF',
    'CHINESE_HAMSTER',

    -- BIRD
    'BUDGERIGAR',
    'COCKATIEL',
    'LOVE_BIRD',
    'AFRICAN_GREY_PARROT',
    'MACAW',
    'COCKATOO',
    'CONURE',
    'PARROTLET',
    'AMAZON_PARROT',
    'RINGNECK_PARAKEET',
    'CANARY',
    'ZEBRA_FINCH',
    'JAVA_FINCH',
    'SOCIETY_FINCH',
    'GOULDIAN_FINCH',

    -- REPTILE
    'LEOPARD_GECKO',
    'CRESTED_GECKO',
    'BEARDED_DRAGON',

    -- 기타
    'UNKNOWN'
),
    `sex` ENUM('MALE','FEMALE') NOT NULL,
    `birthday` DATE NOT NULL,
    `img_src` VARCHAR(500) NULL,
    `tnr` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_pet_uid_name` (`user_id`, `name`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ ChatRoom
CREATE TABLE `chat_rooms` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id_1` BIGINT NOT NULL,
    `user_id_2` BIGINT NOT NULL,
    `last_message` VARCHAR(1000) NOT NULL DEFAULT '',
    `last_message_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_chatroom_users` (`user_id_1`, `user_id_2`),
    FOREIGN KEY (`user_id_1`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id_2`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Chat
CREATE TABLE `chats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `chat_rooms_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `message` VARCHAR(1000) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`chat_rooms_id`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Feed
CREATE TABLE `feeds` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
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

-- ✅ Comment
CREATE TABLE `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `parent_comment_id` BIGINT NULL,
    `feed_id` BIGINT NOT NULL,
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

-- ✅ Care
CREATE TABLE cares (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '돌봄/산책 요청 ID',
    title VARCHAR(100) NOT NULL COMMENT '제목',
    content TEXT NOT NULL COMMENT '내용',
    user_id BIGINT NOT NULL COMMENT '작성자 ID',
    pet_id BIGINT NOT NULL COMMENT '돌봄/산책 대상 반려동물 ID',
    region_id BIGINT NOT NULL COMMENT '지역 ID',
    category ENUM('WALK_WANT', 'WALK_REQ', 'CARE_WANT', 'CARE_REQ') NOT NULL COMMENT '카테고리',
    start_datetime DATETIME NOT NULL COMMENT '시작 일시 (산책: 날짜+시작시간, 돌봄: 시작 날짜)',
    end_datetime DATETIME NOT NULL COMMENT '종료 일시 (산책: 날짜+종료시간, 돌봄: 종료 날짜)',
    views INT NOT NULL DEFAULT 0 COMMENT '조회수',
    status ENUM('ACTIVE', 'MATCHED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME NULL COMMENT '삭제일시',
    
    PRIMARY KEY (id),
    
    -- 외래키 제약조건
    CONSTRAINT fk_cares_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cares_pet_id FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    CONSTRAINT fk_cares_region_id FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE,
    
    -- 인덱스 (성능 최적화)
    INDEX idx_cares_region_id (region_id),
    INDEX idx_cares_user_id (user_id),
    INDEX idx_cares_pet_id (pet_id),
    INDEX idx_cares_category (category),
    INDEX idx_cares_status (status),
    INDEX idx_cares_start_datetime (start_datetime),
    INDEX idx_cares_created_at (created_at),
    INDEX idx_cares_deleted_at (deleted_at),
    
    -- 복합 인덱스 (자주 사용되는 조건 조합)
    INDEX idx_cares_region_category (region_id, category),
    INDEX idx_cares_region_status (region_id, status),
    INDEX idx_cares_user_status (user_id, status),
    INDEX idx_cares_region_deleted (region_id, deleted_at),
    INDEX idx_cares_category_status (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='돌봄/산책 요청 테이블';

-- ✅ Badge
CREATE TABLE `badges` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(20) NOT NULL,
    `description` VARCHAR(200) NOT NULL,
    PRIMARY KEY (`id`)
);

-- ✅ BadgeList
CREATE TABLE `badge_lists` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `bedge_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_uid_bid` (`user_id`, `bedge_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`bedge_id`) REFERENCES `badges`(`id`) ON DELETE CASCADE
);

-- ✅ Image
CREATE TABLE `images` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ref_id` BIGINT NOT NULL,
    `ref_type` ENUM('FEED', 'CARE', 'HOTEL', 'USER', 'REVIEW', 'CHAT',
    'MISSING_REPORT', 'SIGHTING'  -- 실종 신고 관련 추가 
    ) NOT NULL,
    `src` VARCHAR(500) NOT NULL,
    `sort` INT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`)
);


-- ✅ UserChatRoom
CREATE TABLE `user_chat_rooms` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `chat_room_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `last_read_cid` BIGINT NULL,
    `leave_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_crid_uid` (`chat_room_id`, `user_id`),
    FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Like
CREATE TABLE `likes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `feed_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `liked_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_uid_fid` (`user_id`, `feed_id`),
    FOREIGN KEY (`feed_id`) REFERENCES `feeds`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- ✅ Place
CREATE TABLE `places` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
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
    UNIQUE KEY `uq_place_name` (`name`, `address`),
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- ✅ Review
CREATE TABLE `reviews` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `place_id` BIGINT NOT NULL,
    `region_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_review_unique` (`place_id`, `user_id`, `title`),
    FOREIGN KEY (`place_id`) REFERENCES `places`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
);

-- ✅ Tag
CREATE TABLE `tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_tag_name` (`name`)
);

-- ✅ FeedTag
CREATE TABLE `feed_tags` (
    `feed_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    PRIMARY KEY (`feed_id`, `tag_id`),
    FOREIGN KEY (`feed_id`) REFERENCES `feeds`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE
);

-- ✅ Introduction
CREATE TABLE `introduction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `content` VARCHAR(2000) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_intro_uid` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- =================================================================
-- NEW: 호텔 예약 시스템을 위한 신규 테이블
-- =================================================================

-- ✅ Hotel 
CREATE TABLE `hotels` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '호텔 고유 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '호텔 이름',
    `description` VARCHAR(500) NULL COMMENT '호텔 설명',
    `address` VARCHAR(200) NOT NULL COMMENT '주소',
    `phone_number` VARCHAR(20) NULL COMMENT '연락처',
    `latitude` DECIMAL(10, 8) NULL COMMENT '위도',
    `longitude` DECIMAL(11, 8) NULL COMMENT '경도',
    `price_per_night` DECIMAL(10, 2) NOT NULL COMMENT '1박당 가격',
    `max_capacity` INT NOT NULL COMMENT '최대 수용 가능 펫 수',
    `image_url` VARCHAR(500) NULL COMMENT '대표 이미지 URL',
    `active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '활성화 여부',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW() COMMENT '수정일시',
    PRIMARY KEY (`id`)
) COMMENT '반려동물 호텔 정보 테이블';

-- ✅ Hotel Supported Pet Types 
CREATE TABLE `hotel_supported_pet_types` (
    `hotel_id` BIGINT NOT NULL COMMENT '호텔 ID',
    `pet_type` ENUM('DOG', 'CAT') NOT NULL COMMENT '수용 가능한 펫 종류',
    PRIMARY KEY (`hotel_id`, `pet_type`),
    FOREIGN KEY (`hotel_id`) REFERENCES `hotels`(`id`) ON DELETE CASCADE
) COMMENT '호텔별 수용 가능한 반려동물 타입 매핑';

-- ✅ 수정된 Reservation 테이블 (pet_id 추가, check_in/check_out 제거)
CREATE TABLE `reservations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 고유 ID',
    `user_id` BIGINT NOT NULL COMMENT '예약한 사용자 ID',
    `pet_id` BIGINT NOT NULL COMMENT '예약된 반려동물 ID',
    `hotel_id` BIGINT NOT NULL COMMENT '예약된 호텔 ID',
    `total_price` DECIMAL(10, 2) NOT NULL COMMENT '최종 결제 금액',
    `status` ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '예약 상태',
    `special_requests` VARCHAR(1000) NULL COMMENT '특별 요청사항',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW() COMMENT '수정일시',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`pet_id`) REFERENCES `pets`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`hotel_id`) REFERENCES `hotels`(`id`) ON DELETE CASCADE,
    INDEX `idx_reservations_status` (`status`),
    INDEX `idx_reservations_user_id` (`user_id`),
    INDEX `idx_reservations_hotel_id` (`hotel_id`)
) COMMENT '호텔 예약 정보 테이블 (리팩토링됨)';

-- ✅ Payment 테이블 재생성
CREATE TABLE `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '결제 고유 ID',
    `reservation_id` BIGINT NOT NULL COMMENT '연관된 예약 ID',
    `merchant_uid` VARCHAR(100) NOT NULL COMMENT '가맹점 주문번호',
    `imp_uid` VARCHAR(100) NULL COMMENT '포트원 거래번호',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '결제 금액',
    `status` ENUM('PENDING', 'PAID', 'CANCELLED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '결제 상태',
    `payment_method` ENUM('CARD', 'KAKAOPAY', 'NAVERPAY', 'BANK') NULL COMMENT '결제 수단',
    `paid_at` DATETIME NULL COMMENT '결제 완료 일시',
    `cancelled_at` DATETIME NULL COMMENT '결제 취소 일시',
    `failure_reason` VARCHAR(500) NULL COMMENT '실패 사유',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '생성일시',
    `updated_at` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW() COMMENT '수정일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_merchant_uid` (`merchant_uid`),
    FOREIGN KEY (`reservation_id`) REFERENCES `reservations`(`id`) ON DELETE CASCADE
) COMMENT '결제 정보 테이블';


-- =================================================================
-- 호텔별 예약 가능 날짜 생성 (테스트용)
-- =================================================================



-- ✅ 호텔별 예약 가능 날짜를 관리하는 테이블 생성
CREATE TABLE available_dates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 호텔별 날짜 유니크 제약조건 (같은 호텔의 같은 날짜는 하나만 존재)
    UNIQUE KEY uk_hotel_date (hotel_id, date),
    
    -- 외래키 제약조건
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    
    -- 검색 성능을 위한 인덱스
    INDEX idx_hotel_date_status (hotel_id, date, status),
    INDEX idx_date_status (date, status)
);

-- 상태값 체크 제약조건 (MySQL 8.0.16 이상)
ALTER TABLE available_dates 
ADD CONSTRAINT chk_status 
CHECK (status IN ('AVAILABLE', 'BOOKED'));

-- =================================================================
-- 이제 reservation_dates 테이블 생성 (available_dates 테이블 생성 후)
-- =================================================================

-- ✅ 예약과 예약된 날짜들을 연결하는 조인 테이블 생성
CREATE TABLE reservation_dates (
    reservation_id BIGINT NOT NULL,
    available_date_id BIGINT NOT NULL,
    
    -- 복합 기본키 (예약 ID + 날짜 ID)
    PRIMARY KEY (reservation_id, available_date_id),
    
    -- 외래키 제약조건
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (available_date_id) REFERENCES available_dates(id) ON DELETE CASCADE,
    
    -- 검색 성능을 위한 인덱스
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_available_date_id (available_date_id)
);

-- =================================================================
-- 호텔별 예약 가능 날짜 생성 (테이블 생성 후 데이터 삽입)
-- =================================================================

-- 기존 호텔들에 대해 향후 3개월간 예약 가능 날짜 생성
INSERT INTO available_dates (hotel_id, date, status, created_at, updated_at)
SELECT h.id, 
       DATE_ADD(CURDATE(), INTERVAL seq.seq DAY) as date,
       'AVAILABLE' as status,
       NOW() as created_at,
       NOW() as updated_at
FROM hotels h
CROSS JOIN (
    SELECT 0 as seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION 
    SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION 
    SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION 
    SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION 
    SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION 
    SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION 
    SELECT 30 UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION 
    SELECT 35 UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION 
    SELECT 40 UNION SELECT 41 UNION SELECT 42 UNION SELECT 43 UNION SELECT 44 UNION 
    SELECT 45 UNION SELECT 46 UNION SELECT 47 UNION SELECT 48 UNION SELECT 49 UNION 
    SELECT 50 UNION SELECT 51 UNION SELECT 52 UNION SELECT 53 UNION SELECT 54 UNION 
    SELECT 55 UNION SELECT 56 UNION SELECT 57 UNION SELECT 58 UNION SELECT 59 UNION 
    SELECT 60 UNION SELECT 61 UNION SELECT 62 UNION SELECT 63 UNION SELECT 64 UNION 
    SELECT 65 UNION SELECT 66 UNION SELECT 67 UNION SELECT 68 UNION SELECT 69 UNION 
    SELECT 70 UNION SELECT 71 UNION SELECT 72 UNION SELECT 73 UNION SELECT 74 UNION 
    SELECT 75 UNION SELECT 76 UNION SELECT 77 UNION SELECT 78 UNION SELECT 79 UNION 
    SELECT 80 UNION SELECT 81 UNION SELECT 82 UNION SELECT 83 UNION SELECT 84 UNION 
    SELECT 85 UNION SELECT 86 UNION SELECT 87 UNION SELECT 88 UNION SELECT 89
) seq
WHERE DATE_ADD(CURDATE(), INTERVAL seq.seq DAY) <= DATE_ADD(CURDATE(), INTERVAL 3 MONTH);





-- =================================================================
-- 인덱스 및 기본 테스트 데이터 (기존 정보 기반)
-- =================================================================

-- 🔍 소셜 로그인 관련 인덱스 추가
-- [수정] MySQL은 CREATE INDEX에 IF NOT EXISTS를 지원하지 않으므로 제거합니다.
CREATE INDEX `idx_users_login_type` ON `users`(`login_type`);
CREATE INDEX `idx_users_social_id` ON `users`(`social_id`);
CREATE INDEX `idx_users_login_type_social_id` ON `users`(`login_type`, `social_id`);

-- 경상북도 및 구미시 대표 데이터
INSERT IGNORE INTO `regions` (`id`, `name`, `parent_id`, `geometry`) VALUES
(37000000, '경상북도', NULL, ST_GeomFromText('POINT(128.876379 36.433979)')),
(37050000, '구미시', 37000000, ST_GeomFromText('POINT(128.362699 36.212292)'));

-- 구미시 하위 지역 데이터 (로그 기반 ID)
INSERT IGNORE INTO `regions` (`id`, `name`, `parent_id`, `geometry`) VALUES
(37050110, '선산읍', 37050000, ST_GeomFromText('POINT(128.283580 36.246959)')),
(37050120, '고아읍', 37050000, ST_GeomFromText('POINT(128.327688 36.183645)')),
(37050130, '산동읍', 37050000, ST_GeomFromText('POINT(128.462296 36.181061)')),
(37050310, '무을면', 37050000, ST_GeomFromText('POINT(128.187926 36.265641)')),
(37050320, '옥성면', 37050000, ST_GeomFromText('POINT(128.265567 36.304640)')),
(37050330, '도개면', 37050000, ST_GeomFromText('POINT(128.353137 36.304457)')),
(37050340, '해평면', 37050000, ST_GeomFromText('POINT(128.408992 36.219688)')),
(37050360, '장천면', 37050000, ST_GeomFromText('POINT(128.511828 36.150167)')),
(37050510, '송정동', 37050000, ST_GeomFromText('POINT(128.353882 36.121199)')),
(37050550, '도량동', 37050000, ST_GeomFromText('POINT(128.337561 36.145738)')),
(37050560, '지산동', 37050000, ST_GeomFromText('POINT(128.357704 36.139665)')),
(37050570, '선주원남동', 37050000, ST_GeomFromText('POINT(128.319947 36.131881)')),
(37050590, '형곡1동', 37050000, ST_GeomFromText('POINT(128.336033 36.118847)')),
(37050600, '형곡2동', 37050000, ST_GeomFromText('POINT(128.336888 36.107089)')),
(37050610, '신평1동', 37050000, ST_GeomFromText('POINT(128.362825 36.121911)')),
(37050620, '신평2동', 37050000, ST_GeomFromText('POINT(128.366608 36.127303)')),
(37050660, '광평동', 37050000, ST_GeomFromText('POINT(128.360467 36.108405)')),
(37050670, '상모사곡동', 37050000, ST_GeomFromText('POINT(128.352855 36.094567)')),
(37050690, '임오동', 37050000, ST_GeomFromText('POINT(128.366007 36.078246)')),
(37050700, '인동동', 37050000, ST_GeomFromText('POINT(128.454495 36.102718)')),
(37050710, '진미동', 37050000, ST_GeomFromText('POINT(128.410431 36.109180)')),
(37050720, '양포동', 37050000, ST_GeomFromText('POINT(128.413260 36.140688)')),
(37050730, '비산동', 37050000, ST_GeomFromText('POINT(128.377883 36.125332)')),
(37050740, '공단동', 37050000, ST_GeomFromText('POINT(128.410274 36.168089)')),
(37050750, '원평동', 37050000, ST_GeomFromText('POINT(128.347429 36.130805)'));

-- ✅ 태그 & 해시태그 더미 데이터
INSERT INTO `tags` (`name`) VALUES
('산책'), ('목욕'), ('미용'), ('사료'), ('간식'),
('놀이'), ('훈련'), ('건강관리'), ('동물병원'), ('호텔'),
('유치원'), ('캣타워'), ('펫시터'), ('입양'), ('보험'),
('장난감'), ('케어'), ('리드줄'), ('하네스'), ('이동장'), ('실종');


-- ===================================
-- 호텔 테스트 데이터
-- ===================================

INSERT IGNORE INTO hotels (name, description, address, phone_number, latitude, longitude, price_per_night, max_capacity, image_url, created_at, updated_at) VALUES
-- 서울 지역 호텔들
('댕댕이 호텔 강남점', '강남 최고급 반려동물 호텔입니다. 24시간 돌봄 서비스와 넓은 운동장을 제공합니다.', '서울시 강남구 테헤란로 123', '02-1234-5678', 37.4979462, 127.0276368, 80000.00, 15, 'https://example.com/hotel1.jpg', NOW(), NOW()),
('냥냥이 펜션 홍대점', '고양이 전문 호텔로 조용하고 편안한 환경을 제공합니다. 캣타워와 숨숨집이 완비되어 있어요.', '서울시 마포구 홍대입구역 456', '02-9876-5432', 37.5563135, 126.9245734, 65000.00, 10, 'https://example.com/hotel2.jpg', NOW(), NOW()),
('펫플레이스 호텔 잠실점', '잠실 롯데월드 근처에 위치한 프리미엄 펫 호텔입니다. 수영장과 미용실까지 완비!', '서울시 송파구 잠실동 789', '02-5555-1234', 37.5133665, 127.1025597, 90000.00, 20, 'https://example.com/hotel3.jpg', NOW(), NOW()),
('우리집 같은 펜션', '아늑하고 따뜻한 분위기의 소규모 펜션입니다. 가족 같은 돌봄을 약속드려요.', '서울시 용산구 이태원로 321', '02-7777-8888', 37.5347896, 126.9947061, 55000.00, 8, 'https://example.com/hotel4.jpg', NOW(), NOW()),
-- 경기도 지역 호텔들
('파라다이스 펫 리조트', '넓은 정원과 자연 친화적인 환경에서 반려동물이 뛰어놀 수 있는 리조트형 호텔입니다.', '경기도 성남시 분당구 정자일로 100', '031-1111-2222', 37.3595316, 127.1052133, 70000.00, 25, 'https://example.com/hotel5.jpg', NOW(), NOW()),
('꿈나무 펫 호텔', '수원 영통구에 위치한 현대적인 시설의 펫 호텔. CCTV로 실시간 모니터링 가능해요.', '경기도 수원시 영통구 월드컵로 200', '031-3333-4444', 37.2595632, 127.0467065, 60000.00, 12, 'https://example.com/hotel6.jpg', NOW(), NOW()),
-- 부산 지역 호텔
('해운대 펫 빌라', '바다가 보이는 최고의 위치! 반려동물과 함께 바다 구경도 하고 힐링도 하세요.', '부산시 해운대구 해운대해변로 500', '051-1111-9999', 35.1595454, 129.1603193, 75000.00, 18, 'https://example.com/hotel7.jpg', NOW(), NOW()),
-- 제주도 호텔
('제주 펫 파라다이스', '제주도의 아름다운 자연 속에서 반려동물과 함께 힐링할 수 있는 최고의 펜션입니다.', '제주시 애월읍 고내리 333', '064-2222-7777', 33.4506921, 126.4017004, 95000.00, 30, 'https://example.com/hotel8.jpg', NOW(), NOW()),
-- 구미 호텔
('마이구미 펫 호텔', '구미에서 만나는 펫 호텔 ! 강아지와 고양을 환영합니다. ', '구미시 진평2길 22 ', '064-2222-7777', 36.1190, 128.3445 , 40000.00, 30, 'https://example.com/hotel8.jpg', NOW(), NOW()),
('구미 펫하우스', '구미 중심에 위치한 소규모 반려동물 호텔. 소형견과 고양이에게 최적화된 공간을 제공합니다.', '구미시 원평동 45-12', '054-111-2233', 36.1285, 128.3459, 45000.00, 12, 'https://example.com/gumi_hotel1.jpg', NOW(), NOW()),
('강변 펫 리조트', '낙동강 근처에 위치해 산책 코스가 좋은 호텔입니다. 넓은 운동장과 CCTV 모니터링 시스템 제공.', '구미시 선산읍 강변로 100', '054-333-5566', 36.2478, 128.2790, 70000.00, 20, 'https://example.com/gumi_hotel2.jpg', NOW(), NOW()),
('스마일 펫 호텔', '합리적인 가격과 깔끔한 시설을 갖춘 호텔. 중소형 반려견과 고양이 전용 공간 구비.', '구미시 도량동 88-7', '054-444-7788', 36.1451, 128.3377, 38000.00, 15, 'https://example.com/gumi_hotel3.jpg', NOW(), NOW()),
('펫케어 구미', '전문 수의사가 상주하는 프리미엄 반려동물 호텔. 건강 관리와 미용 서비스까지 지원합니다.', '구미시 인동동 210-4', '054-555-9999', 36.1025, 128.4568, 95000.00, 25, 'https://example.com/gumi_hotel4.jpg', NOW(), NOW());


-- 호텔별 지원 펫 타입 데이터 삽입
INSERT IGNORE INTO hotel_supported_pet_types (hotel_id, pet_type) VALUES
(1, 'DOG'), (2, 'CAT'), (3, 'DOG'), (3, 'CAT'), (4, 'DOG'), (4, 'CAT'),
(5, 'DOG'), (6, 'DOG'), (6, 'CAT'), (7, 'DOG'), (8, 'DOG'), (8, 'CAT'),
(9, 'DOG'),(10, 'DOG'), (10, 'CAT'),
(11, 'DOG'),
(12, 'DOG'), (12, 'CAT'),
(13, 'DOG'), (13, 'CAT');



-- ===================================
-- 예약 테스트 데이터
-- ==================================

-- 예약된 날짜들과 예약 연결 (reservation_dates 테이블)
-- 예약 ID 3: 호텔 ID 2, 오늘부터 7일 후부터 2박
INSERT INTO reservation_dates (reservation_id, available_date_id)
SELECT 3, ad.id 
FROM available_dates ad 
WHERE ad.hotel_id = 2 
  AND ad.date BETWEEN DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND DATE_ADD(CURDATE(), INTERVAL 8 DAY)
  AND ad.status = 'AVAILABLE'
LIMIT 2;

-- 예약 ID 4: 호텔 ID 2, 오늘부터 14일 후부터 2박  
INSERT INTO reservation_dates (reservation_id, available_date_id)
SELECT 4, ad.id 
FROM available_dates ad 
WHERE ad.hotel_id = 2 
  AND ad.date BETWEEN DATE_ADD(CURDATE(), INTERVAL 14 DAY) AND DATE_ADD(CURDATE(), INTERVAL 15 DAY)
  AND ad.status = 'AVAILABLE'
LIMIT 2;

-- 예약 ID 5: 호텔 ID 4, 오늘부터 21일 후부터 2박
INSERT INTO reservation_dates (reservation_id, available_date_id)
SELECT 5, ad.id 
FROM available_dates ad 
WHERE ad.hotel_id = 4 
  AND ad.date BETWEEN DATE_ADD(CURDATE(), INTERVAL 21 DAY) AND DATE_ADD(CURDATE(), INTERVAL 22 DAY)
  AND ad.status = 'AVAILABLE'
LIMIT 2;

-- 예약된 날짜들을 BOOKED 상태로 변경
UPDATE available_dates 
SET status = 'BOOKED' 
WHERE id IN (
    SELECT available_date_id 
    FROM reservation_dates 
    WHERE reservation_id IN (3, 4, 5)
);

-- =================================================================
-- 새로운 방식의 결제 테스트 데이터
-- =================================================================

INSERT INTO payments (reservation_id, merchant_uid, imp_uid, amount, status, payment_method, paid_at, created_at, updated_at) VALUES
-- 완료된 결제들
(1, 'HOTEL_1_20250701100000', 'imp_123456789', 160000.00, 'PAID', 'KAKAOPAY', '2025-07-01 10:05:00', '2025-07-01 10:05:00', NOW()),
(2, 'HOTEL_2_20250705143000', 'imp_234567890', 140000.00, 'PAID', 'CARD', '2025-07-05 14:35:00', '2025-07-05 14:35:00', NOW()),
(3, 'HOTEL_3_20250808120000', 'imp_345678901', 130000.00, 'PAID', 'KAKAOPAY', NOW(), NOW(), NOW()),
(4, 'HOTEL_4_20250808130000', 'imp_456789012', 130000.00, 'PAID', 'NAVERPAY', NOW(), NOW(), NOW()),
(5, 'HOTEL_5_20250808140000', 'imp_567890123', 110000.00, 'PAID', 'CARD', NOW(), NOW(), NOW()),

-- 결제 대기 중
(6, 'HOTEL_6_20250808170000', NULL, 140000.00, 'PENDING', NULL, NULL, NOW(), NOW()),
(7, 'HOTEL_7_20250808180000', NULL, 150000.00, 'PENDING', NULL, NULL, NOW(), NOW());

-- safe 모드 활성화 
SET SQL_SAFE_UPDATES = 1;

-- missing_reports
CREATE TABLE `missing_reports` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '실종 신고 고유 ID',
    `user_id` BIGINT NOT NULL COMMENT '신고한 사용자 ID',
    `pet_id` BIGINT NOT NULL COMMENT '실종된 반려동물 ID',
    `region_id` BIGINT NOT NULL COMMENT '실종 지역 ID',
    `address` VARCHAR(300) NOT NULL COMMENT '상세 주소',
    `latitude` DECIMAL(10, 8) NOT NULL COMMENT '실종 위치 위도',
    `longitude` DECIMAL(11, 8) NOT NULL COMMENT '실종 위치 경도',
    `content` TEXT NOT NULL COMMENT '상세 내용 (상황, 특징 등)',
    `status` ENUM('MISSING', 'FOUND', 'CANCELLED') NOT NULL DEFAULT 'MISSING' COMMENT '신고 상태 (실종, 찾음, 취소)',
    `missing_at` DATETIME NOT NULL COMMENT '실종 일시',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '작성일시',
    `updated_at` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW() COMMENT '수정일시',
    `deleted_at` DATETIME NULL COMMENT '삭제일시',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`pet_id`) REFERENCES `pets`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
) COMMENT '반려동물 실종 신고 정보';


-- sightings
CREATE TABLE `sightings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '목격 제보 고유 ID',
    `user_id` BIGINT NOT NULL COMMENT '제보한 사용자 ID',
    `region_id` BIGINT NOT NULL COMMENT '목격 지역 ID',
    `address` VARCHAR(300) NOT NULL COMMENT '상세 주소',
    `latitude` DECIMAL(10, 8) NOT NULL COMMENT '목격 위치 위도',
    `longitude` DECIMAL(11, 8) NOT NULL COMMENT '목격 위치 경도',
    `content` TEXT NOT NULL COMMENT '상세 내용 (상황, 특징 등)',
    `sighted_at` DATETIME NOT NULL COMMENT '목격 일시',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '작성일시',
    `deleted_at` DATETIME NULL COMMENT '삭제일시',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`region_id`) REFERENCES `regions`(`id`)
) COMMENT '실종 동물 목격 제보 정보';


-- sighting_matches
CREATE TABLE `sighting_matches` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sighting_id` BIGINT NOT NULL COMMENT '목격 제보 ID',
    `missing_report_id` BIGINT NOT NULL COMMENT '연결된 실종 신고 ID',
	`image_id` BIGINT NULL COMMENT '매칭에 사용된 실종신고 이미지 ID(선택)',
    `score` DECIMAL(5, 4) NOT NULL COMMENT '모델이 계산한 유사도 점수 (0.0000 ~ 1.0000)',
    `status` ENUM('PENDING', 'CONFIRMED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '매칭 상태 (대기, 주인 확인, 관계 없음)',
    `created_at` DATETIME NOT NULL DEFAULT NOW() COMMENT '매칭 생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_sighting_missing` (`sighting_id`, `missing_report_id`),
    FOREIGN KEY (`sighting_id`) REFERENCES `sightings`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`missing_report_id`) REFERENCES `missing_reports`(`id`) ON DELETE CASCADE,
    INDEX `idx_score` (`score`)
) COMMENT '목격-실종 자동 매칭 결과';

-- 유저 디바이스 토큰 저장용 테이블
CREATE TABLE `user_device_token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token` VARCHAR(256) NOT NULL,
  `app_version` VARCHAR(32) NULL,
  `active` TINYINT NOT NULL DEFAULT 1, -- 비활성화 시 0
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  `updated_at` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW(),

  PRIMARY KEY (`id`),

  CONSTRAINT `fk_udt_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,

  CONSTRAINT `uq_user_token`
    UNIQUE KEY (`user_id`, `token`),

  KEY `idx_udt_user_active` (`user_id`, `active`),
  KEY `idx_udt_updated_at` (`updated_at`)
);

-- 알림 저장용 테이블
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_user_id` BIGINT NOT NULL,
  `type` ENUM('COMMENT','LIKE','CHAT','SIGHT') NOT NULL,
  `ref_type` ENUM('FEED', 'CARE', 'HOTEL', 'USER', 'REVIEW', 'CHAT',
    'MISSING_REPORT', 'SIGHTING'
    ) NOT NULL,
  `ref_id` BIGINT NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `body` VARCHAR(300) NOT NULL,
  `data_json` JSON NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),

  PRIMARY KEY (`id`),
  CONSTRAINT `fk_noti_user`
    FOREIGN KEY (`target_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,

  KEY `idx_noti_target_created` (`target_user_id`, `created_at`),
  KEY `idx_noti_type_target` (`type`, `target_user_id`)
);

COMMIT;