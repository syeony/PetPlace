# PetPlace
### 우리동네 펫 커뮤니티 

---

# 프로젝트 개요
![image](/uploads/372b6e6f29f595aa0c584cc9dfc13b58/image.png){width=800 height=430}
![image](/uploads/200a8106cfda9e5285d983465bddbbc4/image.png){width=800 height=430}
![image](/uploads/a8573860908fb78e1e563ae183da75d5/image.png){width=800 height=430}
![image](/uploads/491cace99241778950d8d1dfdbdef33b/image.png){width=800 height=430}
![image](/uploads/8d153e184a8b8c1bf911970e4455c625/image.png){width=800 height=430}
![image](/uploads/5f0b2318ec03ff81c98aa1de7d665363/image.png){width=800 height=430}
![image](/uploads/f79a753620acb8842fade8c51128d415/image.png){width=800 height=430}
![image](/uploads/ee0dbc8edf80d4fc86f03933c17f5e99/image.png){width=800 height=430}
![image](/uploads/740e51455cf837a8f6517d0d49583612/image.png){width=800 height=430}
![image](/uploads/f1bb3e3ccaebdb79c201c0a01b8417e3/image.png){width=800 height=430}
![image](/uploads/1280ed206d10a05bebf63ea529a462f0/image.png){width=800 height=430}
![image](/uploads/906202147e3fe94209a6b66a3fdc3631/image.png){width=800 height=430}

# 스플래쉬 화면
![image](docu/webp/loading.webp){height=430}

# 피드 페이지
- TopHeader의 부드러운 숨김처리 구현
- 피드 5개씩 페이지네이션 구현
- 글 작성 화면 구현 
- 개인 추천 알고리즘 적용  
- 좋아요, 댓글 FCM 알람 연동 

![image](docu/webp/_MConverter.eu_feed.webp){height=430}
![image](docu/webp/_MConverter.eu_KakaoTalk_20250902_153732630.webp){height=430}
![KakaoTalk_20250902_171833606_17](/uploads/2047aee94edc74326cf9e393470c6223/KakaoTalk_20250902_171833606_17.png){width=200}

# 우리동네 페이지
- 바텀시트 구현
- 카카오맵 API 연동하여 위치 마커 표시

![Screenshot_20250815_144836_PetPlace](/uploads/2b39eb000e9bca5d70baa5c41d660838/Screenshot_20250815_144836_PetPlace.jpg){height=430}

# 산책/돌봄 페이지
- 게시판 리스트 구현 
- 글 작성 화면 구현

![image](docu/webp/_MConverter.eu_KakaoTalk_20250902_154047838.webp){height=430}
![image](docu/webp/_MConverter.eu_KakaoTalk_20250902_153738954.webp){height=430}

# 실종 신고/등록 페이지 
- 실종신고 페이지 구현(잃어버렸어요)
- 실종등록 페이지 구현(목격했어요)
- 실종매칭 FCM 알람 연동 

![image](docu/webp/missingregister.webp){height=430}
![KakaoTalk_20250902_171833606_15](/uploads/5eae799fe44e33da2b7e62da220aae36/KakaoTalk_20250902_171833606_15.png){height=430}
![image](docu/webp/matching.webp){height=430}

# 채팅페이지
![image](docu/webp/chat.webp){height=430}

# 마이페이지
- 꼬순내 로직 구현
- 마이 프로필 수정 
- 내 가족(펫) 등록 및 추가 
- 신뢰기반 내 용품 등록 구현
- 마이피드, 댓글, 찜한피드 조회 구현

![image](docu/webp/mypage.webp){height=430}
![image](docu/webp/petadd.webp){height=430}


# 영상포트폴리오
[![Video Title](https://img.youtube.com/vi/vrb4LYhehxQ/0.jpg){width=800 height=430}](https://www.youtube.com/watch?v=vrb4LYhehxQ)

# 시스템 아키텍처
![PetPlace__5_](/uploads/5dedbe523d47b1b143a2201ba65bce6a/PetPlace__5_.png)

# Role
|오승연|김민|조경호|
|:---:|:---:|:---:|
|모바일|백엔드|인프라, AI|
|[![승연이](/uploads/96a13d572f9e5c689e8fa39f1affb135/승연이.png){width=150 height=150}](https://github.com/syeony)|[![민](/uploads/11c40ffb37c999fe40a139ae2c7686af/민.jpg){width=150 height=150}](https://github.com/ZeppIine)|[![경호](/uploads/9f62ef8e28045eb96a9416ea33a6c05e/경호.png){width=150 height=150}](https://github.com/Alien-kh)

|이도형|송정현|정유진|
|:---:|:---:|:---:|
|모바일|모바일|백엔드|
|![도형](/uploads/0fc4ce481b0985a8e254a7b1501935a6/도형.png){width=150 height=150}|![정현](/uploads/e87defba9f4ca6ed07dbb3dccfe9ec6c/정현.png){width=150 height=150}|![유진](/uploads/f16612d09b1dc1a104ee90f4e6161d9f/유진.png){width=150 height=150}|

# 상세 역할분담

- 오승연: 디자인, 피드, 글쓰기, 댓글, 산책/돌봄, 실종 페이지 및 기능 연결

- 김민: 피드, 댓글, 좋아요, 마이페이지, 펫, 꼬순내, FCM 기능 구현 및 DB 설계

- 조경호: AI 모델 연구 및 최적화, 빅데이터 추천 로직 구현, 인프라 구축, 채팅 기능 구현

- 이도형: 로그인, 소셜로그인, 호텔 페이지 기능 연결, 카카오맵 API 연결, 온디바이스 AI 적용

- 송정현: 채팅, 마이페이지, 펫 페이지 기능 연결, FCM 딥링크 구현

- 정유진: 디자인, 로그인, JWT, 호텔, 산책/돌봄 기능 구현 및 DB 설계
