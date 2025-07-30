
-- 지역 더미 데이터
INSERT INTO `Region` (`id`, `region_name`, `parent_id`, `geometry`) VALUES
(1001, '서울특별시', NULL, ST_GeomFromText('POINT(126.9780 37.5665)')),
(1002, '부산광역시', NULL, ST_GeomFromText('POINT(129.0756 35.1796)')),
(1003, '대구광역시', NULL, ST_GeomFromText('POINT(128.6014 35.8714)')),
(1004, '인천광역시', NULL, ST_GeomFromText('POINT(126.7052 37.4563)')),
(1005, '광주광역시', NULL, ST_GeomFromText('POINT(126.8530 35.1595)'));


-- 유저 더미 데이터
INSERT INTO `User` (
    `user_id`, `password`, `name`, `nickname`, `created_at`, `rid`,
    `default_pet_id`, `kakao_oauth`, `user_img_src`, `pet_smell`,
    `default_badge_id`, `ci`, `phone_number`, `gender`, `birthday`,
    `isForeigner`, `level`, `experience`
) VALUES
('user01', 'pass1234', '김철수', '철수', NOW(), 1001, NULL, NULL, NULL, 36.5, NULL, 'CI_USER_001', '01012345678', 'male', '1990-01-15', 0, 1, 0),
('user02', 'pass1234', '이영희', '영희', NOW(), 1002, NULL, NULL, NULL, 36.7, NULL, 'CI_USER_002', '01023456789', 'female', '1992-03-10', 0, 1, 0),
('user03', 'pass1234', '박민수', '민수', NOW(), 1003, NULL, NULL, NULL, 36.4, NULL, 'CI_USER_003', '01034567890', 'male', '1988-07-22', 0, 1, 0),
('user04', 'pass1234', '최지혜', '지혜', NOW(), 1004, NULL, NULL, NULL, 36.6, NULL, 'CI_USER_004', '01045678901', 'female', '1995-11-05', 0, 1, 0),
('user05', 'pass1234', '정우성', '우성', NOW(), 1005, NULL, NULL, NULL, 36.5, NULL, 'CI_USER_005', '01056789012', 'male', '1985-05-30', 0, 1, 0);

-- 유저별 펫 더미 데이터
INSERT INTO `Pet` (`uid`, `pet_name`, `animal`, `breed`, `sex`, `birthday`)
VALUES
(1, '초코', 'dog', 'pomeranian', 'male', '2020-03-01'),
(2, '나비', 'cat', 'korean_shorthair', 'female', '2019-06-12'),
(3, '토토', 'rabbit', 'netherland_dwarf', 'male', '2021-01-05'),
(4, '햄찌', 'hamster', 'golden_hamster', 'female', '2022-02-15'),
(5, '레오', 'reptile', 'leopard_gecko', 'male', '2020-09-10');

-- 피드 더미 데이터
INSERT INTO `Feed` (`content`, `uid`, `user_nick`, `user_img`, `rid`, `category`)
VALUES
('우리 초코 산책 다녀왔어요! 너무 귀엽네요 🐶', 1, '철수', NULL, 1001, 'mypet'),
('강아지 간식 추천해주세요!', 1, '철수', NULL, 1001, 'info'),
('포메라니안 키우시는 분들 털 관리 어떻게 하세요?', 1, '철수', NULL, 1001, 'share'),
('오늘 초코가 새로운 장난감을 좋아했어요!', 1, '철수', NULL, 1001, 'mypet'),
('서울에서 반려견과 함께 갈 수 있는 카페 추천 부탁드려요 ☕', 1, '철수', NULL, 1001, 'any'),
('강아지 예방접종 후기 공유합니다!', 1, '철수', NULL, 1001, 'review'),
('초코가 처음으로 친구 강아지를 만났어요 🐕', 1, '철수', NULL, 1001, 'mypet'),
('강아지 산책 코스 추천드립니다.', 1, '철수', NULL, 1001, 'share'),
('반려견 호텔 이용해보신 분 계신가요?', 1, '철수', NULL, 1001, 'info'),
('포메라니안 건강관리 팁 공유합니다!', 1, '철수', NULL, 1001, 'review');

INSERT INTO `Feed` (`content`, `uid`, `user_nick`, `user_img`, `rid`, `category`)
VALUES
('우리 나비가 창밖을 보며 하루종일 앉아있네요 🐱', 2, '영희', NULL, 1002, 'mypet'),
('고양이 사료 추천 좀 부탁드려요!', 2, '영희', NULL, 1002, 'info'),
('서울에서 고양이와 함께 가기 좋은 병원 리스트 공유합니다.', 2, '영희', NULL, 1002, 'share'),
('나비가 캣타워에서 놀다가 자버렸어요 😺', 2, '영희', NULL, 1002, 'mypet'),
('고양이 모래 어떤 브랜드 쓰세요?', 2, '영희', NULL, 1002, 'any'),
('코리안숏헤어 털 관리 팁 공유합니다.', 2, '영희', NULL, 1002, 'review'),
('우리 집 냥이가 처음으로 간식 먹어봤어요!', 2, '영희', NULL, 1002, 'mypet'),
('부산에 좋은 고양이 미용실 아시는 분?', 2, '영희', NULL, 1002, 'info'),
('나비가 오늘 너무 귀엽네요 💕', 2, '영희', NULL, 1002, 'mypet'),
('고양이 스트레스 해소법 공유합니다.', 2, '영희', NULL, 1002, 'share');

INSERT INTO `Feed` (`content`, `uid`, `user_nick`, `user_img`, `rid`, `category`)
VALUES
('토토가 오늘 첫 산책을 나갔어요 🐇', 3, '민수', NULL, 1003, 'mypet'),
('토끼 케이지 추천 좀 해주세요.', 3, '민수', NULL, 1003, 'info'),
('네덜란드 드워프 키우시는 분 계신가요?', 3, '민수', NULL, 1003, 'share'),
('토끼 간식으로 건초가 최고네요!', 3, '민수', NULL, 1003, 'review'),
('대구에서 토끼 전문 병원 찾고 있어요.', 3, '민수', NULL, 1003, 'info'),
('토토가 너무 귀여워서 사진 찍었어요 📸', 3, '민수', NULL, 1003, 'mypet'),
('토끼 배변 훈련 팁 공유합니다.', 3, '민수', NULL, 1003, 'share'),
('오늘 토토가 제 손을 핥았어요 🥰', 3, '민수', NULL, 1003, 'mypet'),
('토끼 집 청소 루틴 공유합니다.', 3, '민수', NULL, 1003, 'any'),
('네덜란드 드워프 토끼 키우기 후기입니다.', 3, '민수', NULL, 1003, 'review');

INSERT INTO `Feed` (`content`, `uid`, `user_nick`, `user_img`, `rid`, `category`)
VALUES
('햄찌가 바퀴에서 열심히 뛰고 있어요 🐹', 4, '지혜', NULL, 1004, 'mypet'),
('햄스터 집 꾸미기 아이디어 공유합니다.', 4, '지혜', NULL, 1004, 'share'),
('골든햄스터 키우시는 분들 사료 뭐 쓰세요?', 4, '지혜', NULL, 1004, 'info'),
('햄찌가 손 위에서 자네요 😴', 4, '지혜', NULL, 1004, 'mypet'),
('햄스터 건강관리 꿀팁 공유합니다.', 4, '지혜', NULL, 1004, 'review'),
('햄찌가 오늘 첫 간식을 먹었어요!', 4, '지혜', NULL, 1004, 'mypet'),
('인천에서 햄스터 용품 잘 파는 곳 아시나요?', 4, '지혜', NULL, 1004, 'any'),
('햄스터 목욕은 어떻게 하시나요?', 4, '지혜', NULL, 1004, 'info'),
('햄찌가 너무 귀여워서 사진 찍었어요 📸', 4, '지혜', NULL, 1004, 'mypet'),
('햄스터 스트레스 줄이는 법 공유합니다.', 4, '지혜', NULL, 1004, 'share');

INSERT INTO `Feed` (`content`, `uid`, `user_nick`, `user_img`, `rid`, `category`)
VALUES
('레오가 오늘 첫 식사를 했어요 🦎', 5, '우성', NULL, 1005, 'mypet'),
('레오파드 게코 사육장 꾸미기 팁', 5, '우성', NULL, 1005, 'share'),
('파충류 먹이 추천 부탁드립니다.', 5, '우성', NULL, 1005, 'info'),
('레오가 오늘 첫 탈피를 했어요!', 5, '우성', NULL, 1005, 'mypet'),
('광주에서 파충류 전문 샵 아시는 분?', 5, '우성', NULL, 1005, 'any'),
('레오파드 게코 키우기 후기', 5, '우성', NULL, 1005, 'review'),
('레오가 손 위에 올라왔어요!', 5, '우성', NULL, 1005, 'mypet'),
('파충류 온도 관리 팁 공유', 5, '우성', NULL, 1005, 'info'),
('레오가 너무 귀여워요 😍', 5, '우성', NULL, 1005, 'mypet'),
('비어디 드래곤과 레오파드 게코 비교', 5, '우성', NULL, 1005, 'share');

-- 피드 이미지 더미 데이터
-- User01 Feed (id: 1~10)
INSERT INTO `Image` (`ref_id`, `ref_type`, `img_src`, `sort`) VALUES
(1, 'feed', '/images/feed_1_1.jpg', 1),
(1, 'feed', '/images/feed_1_2.jpg', 2),
(2, 'feed', '/images/feed_2_1.jpg', 1),
(3, 'feed', '/images/feed_3_1.jpg', 1),
(3, 'feed', '/images/feed_3_2.jpg', 2),
(4, 'feed', '/images/feed_4_1.jpg', 1),
(5, 'feed', '/images/feed_5_1.jpg', 1),
(6, 'feed', '/images/feed_6_1.jpg', 1),
(7, 'feed', '/images/feed_7_1.jpg', 1),
(8, 'feed', '/images/feed_8_1.jpg', 1),
(9, 'feed', '/images/feed_9_1.jpg', 1),
(10, 'feed', '/images/feed_10_1.jpg', 1);

-- User02 Feed (id: 11~20)
INSERT INTO `Image` (`ref_id`, `ref_type`, `img_src`, `sort`) VALUES
(11, 'feed', '/images/feed_11_1.jpg', 1),
(12, 'feed', '/images/feed_12_1.jpg', 1),
(13, 'feed', '/images/feed_13_1.jpg', 1),
(13, 'feed', '/images/feed_13_2.jpg', 2),
(14, 'feed', '/images/feed_14_1.jpg', 1),
(15, 'feed', '/images/feed_15_1.jpg', 1),
(16, 'feed', '/images/feed_16_1.jpg', 1),
(17, 'feed', '/images/feed_17_1.jpg', 1),
(18, 'feed', '/images/feed_18_1.jpg', 1),
(19, 'feed', '/images/feed_19_1.jpg', 1),
(20, 'feed', '/images/feed_20_1.jpg', 1);

-- User03 Feed (id: 21~30)
INSERT INTO `Image` (`ref_id`, `ref_type`, `img_src`, `sort`) VALUES
(21, 'feed', '/images/feed_21_1.jpg', 1),
(22, 'feed', '/images/feed_22_1.jpg', 1),
(23, 'feed', '/images/feed_23_1.jpg', 1),
(23, 'feed', '/images/feed_23_2.jpg', 2),
(24, 'feed', '/images/feed_24_1.jpg', 1),
(25, 'feed', '/images/feed_25_1.jpg', 1),
(26, 'feed', '/images/feed_26_1.jpg', 1),
(27, 'feed', '/images/feed_27_1.jpg', 1),
(28, 'feed', '/images/feed_28_1.jpg', 1),
(29, 'feed', '/images/feed_29_1.jpg', 1),
(30, 'feed', '/images/feed_30_1.jpg', 1);

-- User04 Feed (id: 31~40)
INSERT INTO `Image` (`ref_id`, `ref_type`, `img_src`, `sort`) VALUES
(31, 'feed', '/images/feed_31_1.jpg', 1),
(32, 'feed', '/images/feed_32_1.jpg', 1),
(33, 'feed', '/images/feed_33_1.jpg', 1),
(34, 'feed', '/images/feed_34_1.jpg', 1),
(35, 'feed', '/images/feed_35_1.jpg', 1),
(35, 'feed', '/images/feed_35_2.jpg', 2),
(36, 'feed', '/images/feed_36_1.jpg', 1),
(37, 'feed', '/images/feed_37_1.jpg', 1),
(38, 'feed', '/images/feed_38_1.jpg', 1),
(39, 'feed', '/images/feed_39_1.jpg', 1),
(40, 'feed', '/images/feed_40_1.jpg', 1);

-- User05 Feed (id: 41~50)
INSERT INTO `Image` (`ref_id`, `ref_type`, `img_src`, `sort`) VALUES
(41, 'feed', '/images/feed_41_1.jpg', 1),
(41, 'feed', '/images/feed_41_2.jpg', 2),
(42, 'feed', '/images/feed_42_1.jpg', 1),
(43, 'feed', '/images/feed_43_1.jpg', 1),
(44, 'feed', '/images/feed_44_1.jpg', 1),
(45, 'feed', '/images/feed_45_1.jpg', 1),
(46, 'feed', '/images/feed_46_1.jpg', 1),
(47, 'feed', '/images/feed_47_1.jpg', 1),
(48, 'feed', '/images/feed_48_1.jpg', 1),
(49, 'feed', '/images/feed_49_1.jpg', 1),
(50, 'feed', '/images/feed_50_1.jpg', 1);

-- 채팅방 더미 데이터
INSERT INTO `ChatRoom` (`uid_1`, `uid_2`, `last_message`, `last_message_at`) VALUES
(1, 2, '안녕, 오늘 시간 돼?', NOW()),      -- 방1
(2, 3, '토끼 사진 봤어?', NOW()),         -- 방2
(4, 5, '내일 파충류샵 갈래?', NOW());     -- 방3

-- 채팅 더미 데이터
-- 첫 메시지 (user01이 방 생성)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(1, 1, '안녕, 오늘 시간 돼?');  -- Chat id=1

-- MessageRead 생성
INSERT INTO `MessageRead` (`crid`, `cid`, `uid`, `read_at`) VALUES
(1, 1, 1, NOW()),   -- 작성자(user01)는 즉시 읽음 처리
(1, 1, 2, NOW());    -- 상대방(user02)는 아직 안 읽음

-- 두 번째 메시지 (user02 답장)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(1, 2, '응, 저녁에 만나자!');  -- Chat id=2

INSERT INTO `MessageRead` (`crid`, `cid`, `uid`, `read_at`) VALUES
(1, 2, 2, NOW()),   -- 작성자(user02)는 즉시 읽음 처리
(1, 2, 1, NULL);    -- user01은 아직 안 읽음

-- 첫 메시지 (user02가 방 생성)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(2, 2, '토끼 사진 봤어?');  -- Chat id=3

INSERT INTO `MessageRead` (`crid`, `cid`, `uid`, `read_at`) VALUES
(2, 3, 2, NOW()),
(2, 3, 3, NOW());

-- 두 번째 메시지 (user03 답장)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(2, 3, '응! 진짜 귀엽더라 🐇');  -- Chat id=4

INSERT INTO `MessageRead` (`crid`, `cid`, `uid`, `read_at`) VALUES
(2, 4, 3, NOW()),
(2, 4, 2, NULL);

-- 첫 메시지 (user04가 방 생성)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(3, 4, '내일 파충류샵 갈래?');  -- Chat id=5

INSERT INTO `MessageRead` (`crid`, `cid`, `uid`, `read_at`) VALUES
(3, 5, 4, NOW()),
(3, 5, 5, NOW());

-- 두 번째 메시지 (user05 답장)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(3, 5, '좋아! 레오한테 먹이도 사야 해 🦎');  -- Chat id=6

INSERT INTO `MessageRead` (`crid`, `cid`, `uid`, `read_at`) VALUES
(3, 6, 5, NOW()),
(3, 6, 4, NULL);
