
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
    `is_foreigner`, `level`, `experience`
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


-- 두 번째 메시지 (user02 답장)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(1, 2, '응, 저녁에 만나자!');  -- Chat id=2


-- 첫 메시지 (user02가 방 생성)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(2, 2, '토끼 사진 봤어?');  -- Chat id=3


-- 두 번째 메시지 (user03 답장)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(2, 3, '응! 진짜 귀엽더라 🐇');  -- Chat id=4


-- 첫 메시지 (user04가 방 생성)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(3, 4, '내일 파충류샵 갈래?');  -- Chat id=5


-- 두 번째 메시지 (user05 답장)
INSERT INTO `Chat` (`crid`, `uid`, `message`) VALUES
(3, 5, '좋아! 레오한테 먹이도 사야 해 🦎');  -- Chat id=6


-- 댓글 더미 데이터
-- Feed 1 (id=1)
INSERT INTO `Comment` (`cid`, `fid`, `content`, `uid`, `user_nick`, `user_img`, `created_at`)
VALUES
(NULL, 1, '초코 너무 귀여워요 🐶', 2, '영희', NULL, NOW()),
(NULL, 1, '산책 어디서 하셨나요?', 3, '민수', NULL, NOW());

-- Feed 1 대댓글 (cid = 첫 번째 댓글 id = 1)
INSERT INTO `Comment` (`cid`, `fid`, `content`, `uid`, `user_nick`, `user_img`, `created_at`)
VALUES
(1, 1, '감사해요! 한강공원에서 했어요!', 1, '철수', NULL, NOW());

-- Feed 2 (id=2)
INSERT INTO `Comment` (`cid`, `fid`, `content`, `uid`, `user_nick`, `user_img`, `created_at`)
VALUES
(NULL, 2, '강아지 간식 추천: 닭가슴살!', 4, '지혜', NULL, NOW()),
(NULL, 2, '저도 궁금했어요~', 5, '우성', NULL, NOW());

-- Feed 2 대댓글 (cid = 첫 번째 댓글 id = 4)
INSERT INTO `Comment` (`cid`, `fid`, `content`, `uid`, `user_nick`, `user_img`, `created_at`)
VALUES
(4, 2, '오 추천 감사합니다!', 1, '철수', NULL, NOW());

-- Feed 3 (id=3)
INSERT INTO `Comment` (`cid`, `fid`, `content`, `uid`, `user_nick`, `user_img`, `created_at`)
VALUES
(NULL, 3, '포메라니안 털 진짜 많이 빠지죠?', 2, '영희', NULL, NOW()),
(NULL, 3, '저희 집은 매일 빗질해줘요!', 3, '민수', NULL, NOW()),
(NULL, 3, '초코 너무 귀여워요!', 5, '우성', NULL, NOW());

-- Feed 3 대댓글 (cid = 두 번째 댓글 id = 7)
INSERT INTO `Comment` (`cid`, `fid`, `content`, `uid`, `user_nick`, `user_img`, `created_at`)
VALUES
(7, 3, '역시 매일 빗질해야겠네요!', 1, '철수', NULL, NOW());

-- 좋아요 더미 데이터
-- User01이 다른 유저의 Feed에 좋아요
INSERT INTO `Like` (`fid`, `uid`) VALUES
(11, 1), (12, 1), (21, 1), (22, 1), (31, 1), (41, 1), (42, 1);

-- User02
INSERT INTO `Like` (`fid`, `uid`) VALUES
(1, 2), (2, 2), (23, 2), (24, 2), (32, 2), (43, 2), (44, 2);

-- User03
INSERT INTO `Like` (`fid`, `uid`) VALUES
(3, 3), (4, 3), (13, 3), (14, 3), (33, 3), (45, 3), (46, 3);

-- User04
INSERT INTO `Like` (`fid`, `uid`) VALUES
(5, 4), (6, 4), (15, 4), (16, 4), (25, 4), (47, 4), (48, 4);

-- User05
INSERT INTO `Like` (`fid`, `uid`) VALUES
(7, 5), (8, 5), (17, 5), (18, 5), (26, 5), (34, 5), (49, 5);

-- 태그 더미 데이터
INSERT INTO `Tag` (`tag_name`) VALUES
('강아지'), ('고양이'), ('토끼'), ('햄스터'), ('파충류'),
('반려동물용품'), ('훈련팁'), ('건강관리'), ('산책코스'), ('입양후기'),
('간식추천'), ('사료후기'), ('미용'), ('동물병원'), ('호텔리뷰'),
('일상공유'), ('귀여움주의'), ('사진공유'), ('초보집사'), ('경험담');

-- 해시태그 더미 데이터
-- Feed 1
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (1, 1), (1, 9), (1, 11);

-- Feed 2
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (2, 1), (2, 6);

-- Feed 3
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (3, 1), (3, 8), (3, 10), (3, 16);

-- Feed 4
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (4, 1), (4, 17);

-- Feed 5
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (5, 1), (5, 9), (5, 13);

-- Feed 6
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (6, 1), (6, 8), (6, 12);

-- Feed 7
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (7, 1), (7, 18);

-- Feed 8
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (8, 1), (8, 9);

-- Feed 9
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (9, 1), (9, 14);

-- Feed 10
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (10, 1), (10, 8), (10, 16);

-- Feed 11
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (11, 2), (11, 17);

-- Feed 12
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (12, 2), (12, 6), (12, 11);

-- Feed 13
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (13, 2), (13, 18), (13, 16);

-- Feed 14
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (14, 2), (14, 8), (14, 12);

-- Feed 15
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (15, 2), (15, 13);

-- Feed 16
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (16, 2), (16, 9), (16, 10);

-- Feed 17
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (17, 2), (17, 6);

-- Feed 18
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (18, 2), (18, 16);

-- Feed 19
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (19, 2), (19, 17);

-- Feed 20
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (20, 2), (20, 8);

-- Feed 21
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (21, 3), (21, 6);

-- Feed 22
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (22, 3), (22, 16), (22, 17);

-- Feed 23
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (23, 3), (23, 18), (23, 9);

-- Feed 24
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (24, 3), (24, 8);

-- Feed 25
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (25, 3), (25, 12), (25, 13);

-- Feed 26
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (26, 3), (26, 10);

-- Feed 27
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (27, 3), (27, 6), (27, 16);

-- Feed 28
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (28, 3), (28, 18);

-- Feed 29
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (29, 3), (29, 9);

-- Feed 30
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (30, 3), (30, 11), (30, 13);

-- Feed 31
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (31, 4), (31, 12);

-- Feed 32
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (32, 4), (32, 6), (32, 17);

-- Feed 33
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (33, 4), (33, 8), (33, 13);

-- Feed 34
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (34, 4), (34, 18);

-- Feed 35
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (35, 4), (35, 9);

-- Feed 36
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (36, 4), (36, 16);

-- Feed 37
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (37, 4), (37, 6), (37, 17);

-- Feed 38
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (38, 4), (38, 10);

-- Feed 39
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (39, 4), (39, 8);

-- Feed 40
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (40, 4), (40, 18);

-- Feed 41
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (41, 5), (41, 6);

-- Feed 42
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (42, 5), (42, 10), (42, 12);

-- Feed 43
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (43, 5), (43, 9), (43, 16);

-- Feed 44
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (44, 5), (44, 8);

-- Feed 45
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (45, 5), (45, 17);

-- Feed 46
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (46, 5), (46, 6), (46, 13);

-- Feed 47
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (47, 5), (47, 18);

-- Feed 48
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (48, 5), (48, 8), (48, 14);

-- Feed 49
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (49, 5), (49, 9), (49, 11);

-- Feed 50
INSERT INTO `Hashtag` (`fid`, `tid`) VALUES (50, 5), (50, 16);

commit;
