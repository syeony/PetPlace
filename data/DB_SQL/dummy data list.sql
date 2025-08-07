-- ✅ 지역 더미 데이터
INSERT INTO `regions` (`id`, `name`, `parent_id`, `geometry`) VALUES
(1001, '서울특별시', NULL, ST_GeomFromText('POINT(126.9780 37.5665)')),
(1002, '부산광역시', NULL, ST_GeomFromText('POINT(129.0756 35.1796)')),
(1003, '대구광역시', NULL, ST_GeomFromText('POINT(128.6014 35.8714)')),
(1004, '인천광역시', NULL, ST_GeomFromText('POINT(126.7052 37.4563)')),
(1005, '광주광역시', NULL, ST_GeomFromText('POINT(126.8530 35.1595)'));

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

-- ✅ 유저 더미 데이터
INSERT INTO `users` (
    `user_name`, `password`, `name`, `nickname`, `created_at`, `region_id`,
    `default_pet_id`, `user_img_src`, `pet_smell`,
    `default_badge_id`, `ci`, `phone_number`, `gender`, `birthday`,
    `is_foreigner`, `level`, `experience`, `login_type`
) VALUES
('user01', 'pass1234', '김철수', '철수', NOW(), 1001, NULL, NULL, 36.5, NULL, 'CI_USER_001', '01012345678', 'male', '1990-01-15', 0, 1, 0, 'EMAIL'),
('user02', 'pass1234', '이영희', '영희', NOW(), 1002, NULL, NULL, 36.7, NULL, 'CI_USER_002', '01023456789', 'female', '1992-03-10', 0, 1, 0, 'EMAIL'),
('user03', 'pass1234', '박민수', '민수', NOW(), 1003, NULL, NULL, 36.4, NULL, 'CI_USER_003', '01034567890', 'male', '1988-07-22', 0, 1, 0, 'EMAIL'),
('user04', 'pass1234', '최지혜', '지혜', NOW(), 1004, NULL, NULL, 36.6, NULL, 'CI_USER_004', '01045678901', 'female', '1995-11-05', 0, 1, 0, 'EMAIL'),
('user05', 'pass1234', '정우성', '우성', NOW(), 1005, NULL, NULL, 36.5, NULL, 'CI_USER_005', '01056789012', 'male', '1985-05-30', 0, 1, 0, 'EMAIL');

-- ✅ 펫 더미 데이터
INSERT INTO `pets` (`user_id`, `name`, `animal`, `breed`, `sex`, `birthday`) VALUES
(1, '초코', 'DOG', 'POMERANIAN', 'MALE', '2020-03-01'),
(2, '나비', 'CAT', 'KOREAN_SHORTHAIR', 'FEMALE', '2019-06-12'),
(3, '토토', 'RABBIT', 'NETHERLAND_DWARF', 'MALE', '2021-01-05'),
(4, '햄찌', 'HAMSTER', 'GOLDEN_HAMSTER', 'FEMALE', '2022-02-15'),
(5, '레오', 'REPTILE', 'LEOPARD_GECKO', 'MALE', '2020-09-10');

-- ✅ 피드 더미 데이터 (User01)
INSERT INTO `feeds` (`content`, `user_id`, `user_nick`, `user_img`, `region_id`, `category`) VALUES
('우리 초코 산책 다녀왔어요! 너무 귀엽네요 🐶', 1, '철수', NULL, 1001, 'MYPET'),
('강아지 간식 추천해주세요!', 1, '철수', NULL, 1001, 'INFO'),
('포메라니안 키우시는 분들 털 관리 어떻게 하세요?', 1, '철수', NULL, 1001, 'SHARE'),
('오늘 초코가 새로운 장난감을 좋아했어요!', 1, '철수', NULL, 1001, 'MYPET'),
('서울에서 반려견과 함께 갈 수 있는 카페 추천 부탁드려요 ☕', 1, '철수', NULL, 1001, 'ANY'),
('강아지 예방접종 후기 공유합니다!', 1, '철수', NULL, 1001, 'REVIEW'),
('초코가 처음으로 친구 강아지를 만났어요 🐕', 1, '철수', NULL, 1001, 'MYPET'),
('강아지 산책 코스 추천드립니다.', 1, '철수', NULL, 1001, 'SHARE'),
('반려견 호텔 이용해보신 분 계신가요?', 1, '철수', NULL, 1001, 'INFO'),
('포메라니안 건강관리 팁 공유합니다!', 1, '철수', NULL, 1001, 'REVIEW');

-- ✅ 피드 더미 데이터 (User02)
INSERT INTO `feeds` (`content`, `user_id`, `user_nick`, `user_img`, `region_id`, `category`) VALUES
('우리 나비가 창밖을 보며 하루종일 앉아있네요 🐱', 2, '영희', NULL, 1002, 'MYPET'),
('고양이 사료 추천 좀 부탁드려요!', 2, '영희', NULL, 1002, 'INFO'),
('서울에서 고양이와 함께 가기 좋은 병원 리스트 공유합니다.', 2, '영희', NULL, 1002, 'SHARE'),
('나비가 캣타워에서 놀다가 자버렸어요 😺', 2, '영희', NULL, 1002, 'MYPET'),
('고양이 모래 어떤 브랜드 쓰세요?', 2, '영희', NULL, 1002, 'ANY'),
('코리안숏헤어 털 관리 팁 공유합니다.', 2, '영희', NULL, 1002, 'REVIEW'),
('우리 집 냥이가 처음으로 간식 먹어봤어요!', 2, '영희', NULL, 1002, 'MYPET'),
('부산에 좋은 고양이 미용실 아시는 분?', 2, '영희', NULL, 1002, 'INFO'),
('나비가 오늘 너무 귀엽네요 💕', 2, '영희', NULL, 1002, 'MYPET'),
('고양이 스트레스 해소법 공유합니다.', 2, '영희', NULL, 1002, 'SHARE');

-- ✅ 피드 더미 데이터 (User03)
INSERT INTO `feeds` (`content`, `user_id`, `user_nick`, `user_img`, `region_id`, `category`) VALUES
('토토가 오늘 첫 산책을 나갔어요 🐇', 3, '민수', NULL, 1003, 'MYPET'),
('토끼 케이지 추천 좀 해주세요.', 3, '민수', NULL, 1003, 'INFO'),
('네덜란드 드워프 키우시는 분 계신가요?', 3, '민수', NULL, 1003, 'SHARE'),
('토끼 간식으로 건초가 최고네요!', 3, '민수', NULL, 1003, 'REVIEW'),
('대구에서 토끼 전문 병원 찾고 있어요.', 3, '민수', NULL, 1003, 'INFO'),
('토토가 너무 귀여워서 사진 찍었어요 📸', 3, '민수', NULL, 1003, 'MYPET'),
('토끼 배변 훈련 팁 공유합니다.', 3, '민수', NULL, 1003, 'SHARE'),
('오늘 토토가 제 손을 핥았어요 🥰', 3, '민수', NULL, 1003, 'MYPET'),
('토끼 집 청소 루틴 공유합니다.', 3, '민수', NULL, 1003, 'ANY'),
('네덜란드 드워프 토끼 키우기 후기입니다.', 3, '민수', NULL, 1003, 'REVIEW');

-- ✅ 피드 더미 데이터 (User04)
INSERT INTO `feeds` (`content`, `user_id`, `user_nick`, `user_img`, `region_id`, `category`) VALUES
('햄찌가 바퀴에서 열심히 뛰고 있어요 🐹', 4, '지혜', NULL, 1004, 'MYPET'),
('햄스터 집 꾸미기 아이디어 공유합니다.', 4, '지혜', NULL, 1004, 'SHARE'),
('골든햄스터 키우시는 분들 사료 뭐 쓰세요?', 4, '지혜', NULL, 1004, 'INFO'),
('햄찌가 손 위에서 자네요 😴', 4, '지혜', NULL, 1004, 'MYPET'),
('햄스터 건강관리 꿀팁 공유합니다.', 4, '지혜', NULL, 1004, 'REVIEW'),
('햄찌가 오늘 첫 간식을 먹었어요!', 4, '지혜', NULL, 1004, 'MYPET'),
('인천에서 햄스터 용품 잘 파는 곳 아시나요?', 4, '지혜', NULL, 1004, 'ANY'),
('햄스터 목욕은 어떻게 하시나요?', 4, '지혜', NULL, 1004, 'INFO'),
('햄찌가 너무 귀여워서 사진 찍었어요 📸', 4, '지혜', NULL, 1004, 'MYPET'),
('햄스터 스트레스 줄이는 법 공유합니다.', 4, '지혜', NULL, 1004, 'SHARE');

-- ✅ 피드 더미 데이터 (User05)
INSERT INTO `feeds` (`content`, `user_id`, `user_nick`, `user_img`, `region_id`, `category`) VALUES
('레오가 오늘 첫 식사를 했어요 🦎', 5, '우성', NULL, 1005, 'MYPET'),
('레오파드 게코 사육장 꾸미기 팁', 5, '우성', NULL, 1005, 'SHARE'),
('파충류 먹이 추천 부탁드립니다.', 5, '우성', NULL, 1005, 'INFO'),
('레오가 오늘 첫 탈피를 했어요!', 5, '우성', NULL, 1005, 'MYPET'),
('광주에서 파충류 전문 샵 아시는 분?', 5, '우성', NULL, 1005, 'ANY'),
('레오파드 게코 키우기 후기', 5, '우성', NULL, 1005, 'REVIEW'),
('레오가 손 위에 올라왔어요!', 5, '우성', NULL, 1005, 'MYPET'),
('파충류 온도 관리 팁 공유', 5, '우성', NULL, 1005, 'INFO'),
('레오가 너무 귀여워요 😍', 5, '우성', NULL, 1005, 'MYPET'),
('비어디 드래곤과 레오파드 게코 비교', 5, '우성', NULL, 1005, 'SHARE');

-- ✅ 피드 이미지 더미 데이터 (전부 변환)
INSERT INTO `images` (`ref_id`, `ref_type`, `src`, `sort`) VALUES
(1, 'FEED', '/images/feed_1_1.jpg', 1),
(1, 'FEED', '/images/feed_1_2.jpg', 2),
(2, 'FEED', '/images/feed_2_1.jpg', 1),
(3, 'FEED', '/images/feed_3_1.jpg', 1),
(3, 'FEED', '/images/feed_3_2.jpg', 2),
(4, 'FEED', '/images/feed_4_1.jpg', 1),
(5, 'FEED', '/images/feed_5_1.jpg', 1),
(6, 'FEED', '/images/feed_6_1.jpg', 1),
(7, 'FEED', '/images/feed_7_1.jpg', 1),
(8, 'FEED', '/images/feed_8_1.jpg', 1),
(9, 'FEED', '/images/feed_9_1.jpg', 1),
(10, 'FEED', '/images/feed_10_1.jpg', 1),
(11, 'FEED', '/images/feed_11_1.jpg', 1),
(12, 'FEED', '/images/feed_12_1.jpg', 1),
(13, 'FEED', '/images/feed_13_1.jpg', 1),
(13, 'FEED', '/images/feed_13_2.jpg', 2),
(14, 'FEED', '/images/feed_14_1.jpg', 1),
(15, 'FEED', '/images/feed_15_1.jpg', 1),
(16, 'FEED', '/images/feed_16_1.jpg', 1),
(17, 'FEED', '/images/feed_17_1.jpg', 1),
(18, 'FEED', '/images/feed_18_1.jpg', 1),
(19, 'FEED', '/images/feed_19_1.jpg', 1),
(20, 'FEED', '/images/feed_20_1.jpg', 1),
(21, 'FEED', '/images/feed_21_1.jpg', 1),
(22, 'FEED', '/images/feed_22_1.jpg', 1),
(23, 'FEED', '/images/feed_23_1.jpg', 1),
(23, 'FEED', '/images/feed_23_2.jpg', 2),
(24, 'FEED', '/images/feed_24_1.jpg', 1),
(25, 'FEED', '/images/feed_25_1.jpg', 1),
(26, 'FEED', '/images/feed_26_1.jpg', 1),
(27, 'FEED', '/images/feed_27_1.jpg', 1),
(28, 'FEED', '/images/feed_28_1.jpg', 1),
(29, 'FEED', '/images/feed_29_1.jpg', 1),
(30, 'FEED', '/images/feed_30_1.jpg', 1),
(31, 'FEED', '/images/feed_31_1.jpg', 1),
(32, 'FEED', '/images/feed_32_1.jpg', 1),
(33, 'FEED', '/images/feed_33_1.jpg', 1),
(34, 'FEED', '/images/feed_34_1.jpg', 1),
(35, 'FEED', '/images/feed_35_1.jpg', 1),
(35, 'FEED', '/images/feed_35_2.jpg', 2),
(36, 'FEED', '/images/feed_36_1.jpg', 1),
(37, 'FEED', '/images/feed_37_1.jpg', 1),
(38, 'FEED', '/images/feed_38_1.jpg', 1),
(39, 'FEED', '/images/feed_39_1.jpg', 1),
(40, 'FEED', '/images/feed_40_1.jpg', 1),
(41, 'FEED', '/images/feed_41_1.jpg', 1),
(41, 'FEED', '/images/feed_41_2.jpg', 2),
(42, 'FEED', '/images/feed_42_1.jpg', 1),
(43, 'FEED', '/images/feed_43_1.jpg', 1),
(44, 'FEED', '/images/feed_44_1.jpg', 1),
(45, 'FEED', '/images/feed_45_1.jpg', 1),
(46, 'FEED', '/images/feed_46_1.jpg', 1),
(47, 'FEED', '/images/feed_47_1.jpg', 1),
(48, 'FEED', '/images/feed_48_1.jpg', 1),
(49, 'FEED', '/images/feed_49_1.jpg', 1),
(50, 'FEED', '/images/feed_50_1.jpg', 1);

-- ✅ 채팅방 더미 데이터
INSERT INTO `chat_rooms` (`user_id_1`, `user_id_2`, `last_message`, `last_message_at`) VALUES
(1, 2, '안녕, 오늘 시간 돼?', NOW()),
(2, 3, '토끼 사진 봤어?', NOW()),
(4, 5, '내일 파충류샵 갈래?', NOW());

-- ✅ 채팅 더미 데이터
INSERT INTO `chats` (`chat_rooms_id`, `user_id`, `message`) VALUES
(1, 1, '안녕, 오늘 시간 돼?'),
(1, 2, '응, 저녁에 만나자!'),
(2, 2, '토끼 사진 봤어?'),
(2, 3, '응! 진짜 귀엽더라 🐇'),
(3, 4, '내일 파충류샵 갈래?'),
(3, 5, '좋아! 레오한테 먹이도 사야 해 🦎');

-- ✅ 댓글 더미 데이터
INSERT INTO `comments` (`parent_comment_id`, `feed_id`, `content`, `user_id`, `user_nick`, `user_img`, `created_at`) VALUES
(NULL, 1, '초코 너무 귀여워요 🐶', 2, '영희', NULL, NOW()),
(NULL, 1, '산책 어디서 하셨나요?', 3, '민수', NULL, NOW()),
(1, 1, '감사해요! 한강공원에서 했어요!', 1, '철수', NULL, NOW()),
(NULL, 2, '강아지 간식 추천: 닭가슴살!', 4, '지혜', NULL, NOW()),
(NULL, 2, '저도 궁금했어요~', 5, '우성', NULL, NOW()),
(4, 2, '오 추천 감사합니다!', 1, '철수', NULL, NOW()),
(NULL, 3, '포메라니안 털 진짜 많이 빠지죠?', 2, '영희', NULL, NOW()),
(NULL, 3, '저희 집은 매일 빗질해줘요!', 3, '민수', NULL, NOW()),
(NULL, 3, '초코 너무 귀여워요!', 5, '우성', NULL, NOW()),
(7, 3, '역시 매일 빗질해야겠네요!', 1, '철수', NULL, NOW());

-- ✅ 좋아요 더미 데이터
INSERT INTO `likes` (`feed_id`, `user_id`) VALUES
(11, 1), (12, 1), (21, 1), (22, 1), (31, 1), (41, 1), (42, 1),
(1, 2), (2, 2), (23, 2), (24, 2), (32, 2), (43, 2), (44, 2),
(3, 3), (4, 3), (13, 3), (14, 3), (33, 3), (45, 3), (46, 3),
(5, 4), (6, 4), (15, 4), (16, 4), (25, 4), (47, 4), (48, 4),
(7, 5), (8, 5), (17, 5), (18, 5), (26, 5), (34, 5), (49, 5);

-- ✅ 태그 & 해시태그 더미 데이터
INSERT INTO `tags` (`name`) VALUES
('산책'), ('목욕'), ('미용'), ('사료'), ('간식'),
('놀이'), ('훈련'), ('건강관리'), ('동물병원'), ('호텔'),
('유치원'), ('캣타워'), ('펫시터'), ('입양'), ('보험'),
('장난감'), ('케어'), ('리드줄'), ('하네스'), ('이동장'), ('실종');

INSERT INTO `feed_tags` (`feed_id`, `tag_id`) VALUES
(1, 1), (1, 9), (1, 11),
(2, 1), (2, 6),
(3, 1), (3, 8), (3, 10), (3, 16),
(4, 1), (4, 17),
(5, 1), (5, 9), (5, 13),
(6, 1), (6, 8), (6, 12),
(7, 1), (7, 18),
(8, 1), (8, 9),
(9, 1), (9, 14),
(10, 1), (10, 8), (10, 16),
(11, 2), (11, 17),
(12, 2), (12, 6), (12, 11),
(13, 2), (13, 18), (13, 16),
(14, 2), (14, 8), (14, 12),
(15, 2), (15, 13),
(16, 2), (16, 9), (16, 10),
(17, 2), (17, 6),
(18, 2), (18, 16),
(19, 2), (19, 17),
(20, 2), (20, 8),
(21, 3), (21, 6),
(22, 3), (22, 16), (22, 17),
(23, 3), (23, 18), (23, 9),
(24, 3), (24, 8),
(25, 3), (25, 12), (25, 13),
(26, 3), (26, 10),
(27, 3), (27, 6), (27, 16),
(28, 3), (28, 18),
(29, 3), (29, 9),
(30, 3), (30, 11), (30, 13),
(31, 4), (31, 12),
(32, 4), (32, 6), (32, 17),
(33, 4), (33, 8), (33, 13),
(34, 4), (34, 18),
(35, 4), (35, 9),
(36, 4), (36, 16),
(37, 4), (37, 6), (37, 17),
(38, 4), (38, 10),
(39, 4), (39, 8),
(40, 4), (40, 18),
(41, 5), (41, 6),
(42, 5), (42, 10), (42, 12),
(43, 5), (43, 9), (43, 16),
(44, 5), (44, 8),
(45, 5), (45, 17),
(46, 5), (46, 6), (46, 13),
(47, 5), (47, 18),
(48, 5), (48, 8), (48, 14),
(49, 5), (49, 9), (49, 11),
(50, 5), (50, 16);

-- 1) feed_id별 선택 횟수 집계
UPDATE feeds AS f
JOIN (
  SELECT
    feed_id,
    COUNT(*) AS cnt
  FROM likes
  GROUP BY feed_id
) AS a ON f.id = a.feed_id
SET f.likes = f.likes + a.cnt;

commit;