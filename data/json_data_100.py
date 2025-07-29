import random
import json
from datetime import datetime, timedelta

categories = ["0", "1", "2", "3", "4"]  # ENUM 값 (문자열로)
animals = ["dog", "cat", "rabbit", "hamster", "bird"]
user_nicks = ["멍멍이집사", "고양이매니아", "귀염토끼", "햄스터짱", "새사랑해"]
regions = [1, 2, 3, 4, 5]
now = datetime(2024, 7, 1, 9, 0, 0)

# 미리 정의된 태그 리스트 (id: 1~10, name: ... 순서 고정)
tag_dict = {
    1: "dog",
    2: "cat",
    3: "산책",
    4: "사료",
    5: "훈련",
    6: "입양",
    7: "미용",
    8: "병원",
    9: "케어",
    10: "장난감"
}
tag_ids = list(tag_dict.keys())

feeds = []
for i in range(1, 101):
    idx = random.randint(0, 4)
    category = random.choice(categories)
    # 태그는 id값으로 최대 3개 랜덤 선택(중복없음)
    num_tags = random.randint(1, 3)
    tags = random.sample(tag_ids, num_tags)
    feeds.append({
        "id": i,
        "content": f"{animals[idx]} 관련 게시글 #{i} ({category})",
        "uid": (idx + 1) * 10 + i % 5,
        "user_nick": user_nicks[idx],
        "user_img": f"/profile/{(idx + 1) * 10 + i % 5}.jpg",
        "rid": random.choice(regions),
        "category": category,
        "tags": tags,  # ← 여기 숫자 배열로
        "created_at": (now + timedelta(hours=i)).isoformat(),
        "like": random.randint(0, 30),
        "view": random.randint(10, 500)
    })

with open("feed_dummy_100.json", "w", encoding="utf-8") as f:
    json.dump(feeds, f, ensure_ascii=False, indent=2)
