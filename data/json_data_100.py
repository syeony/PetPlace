import random
import json
from datetime import datetime, timedelta

categories = ["share", "info", "any", "review", "mypet"]
animals = ["dog", "cat", "rabbit", "hamster", "bird"]
tags_list = [
    ["dog", "산책"], ["cat", "사료"], ["rabbit", "입양"],
    ["hamster", "운동"], ["bird", "노래"], ["dog", "훈련"],
    ["cat", "미용"], ["rabbit", "케어"], ["hamster", "장난감"], ["bird", "병원"]
]
user_nicks = ["멍멍이집사", "고양이매니아", "귀염토끼", "햄스터짱", "새사랑해"]
regions = [1, 2, 3, 4, 5]
now = datetime(2024, 7, 1, 9, 0, 0)

feeds = []
for i in range(1, 101):
    idx = random.randint(0, 4)
    category = random.choice(categories)
    tags = tags_list[random.randint(0, len(tags_list)-1)]
    feeds.append({
        "id": i,
        "content": f"{animals[idx]} 관련 게시글 #{i} ({category})",
        "uid": (idx+1)*10 + i%5,
        "user_nick": user_nicks[idx],
        "user_img": f"/profile/{(idx+1)*10 + i%5}.jpg",
        "rid": random.choice(regions),
        "category": category,
        "tags": tags,
        "created_at": (now + timedelta(hours=i)).isoformat(),
        "like": random.randint(0, 30),
        "view": random.randint(10, 500)
    })

with open("feed_dummy_100.json", "w", encoding="utf-8") as f:
    json.dump(feeds, f, ensure_ascii=False, indent=2)
