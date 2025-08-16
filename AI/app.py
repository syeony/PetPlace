# app.py  (LEAN + YOLO fallback only when bbox missing)
import os, io, time
from typing import Dict, List, Optional

import numpy as np
from PIL import Image
from fastapi import FastAPI, UploadFile, File, Form, Header, HTTPException
from fastapi.responses import JSONResponse

import torch
import faiss
import open_clip
from ultralytics import YOLO  # 폴백용만 사용

# =========================
# 환경설정
# =========================
EMBED_BACKBONE = os.getenv("EMBED_BACKBONE", "ViT-B-32")
EMBED_PRETRAIN = os.getenv("EMBED_PRETRAIN", "openai")
W_FACE_DEFAULT = float(os.getenv("W_FACE", "0.6"))

BREED_ALPHA = float(os.getenv("BREED_ALPHA", "0.25"))   # 같은 품종 가중치(25% 보너스)
HARD_BREED_FILTER = os.getenv("HARD_BREED_FILTER", "0") == "1"  # 하드필터(선택)

# image_id -> 정규화된 품종 문자열
BREED: Dict[int, str] = {}

def _canon(s: Optional[str]) -> Optional[str]:
    if not s: return None
    return s.strip().lower().replace(" ", "").replace("-", "").replace("_", "")

BASE_IMAGE_DIR = os.getenv("BASE_IMAGE_DIR", "/data/images")   # 공유 볼륨
SERVICE_TOKEN  = os.getenv("SERVICE_TOKEN", "")                # 내부 토큰

# bbox 없을 때 동작 선택지
USE_SERVER_YOLO     = os.getenv("USE_SERVER_YOLO", "0") == "1"  # 1이면 서버에서만 폴백 탐지
REQUIRE_BBOX        = os.getenv("REQUIRE_BBOX", "0") == "1"     # 1이면 bbox 없으면 400
ALLOW_WHOLE_IMAGE   = os.getenv("ALLOW_WHOLE_IMAGE", "1") == "1" # yolo도 못쓰면 전체 이미지 허용?

# YOLO는 “정말 bbox가 없을 때만” 쓸 폴백
yolo = YOLO("yolo11n.pt") if USE_SERVER_YOLO else None

def _box_provided(xmin, ymin, xmax, ymax) -> bool:
    return None not in (xmin, ymin, xmax, ymax)

def _box_is_zero(xmin, ymin, xmax, ymax) -> bool:
    # 0,0,0,0 같이 '없음'으로 쓰는 케이스
    return _box_provided(xmin, ymin, xmax, ymax) and (xmin, ymin, xmax, ymax) == (0, 0, 0, 0)

def _clamp_box(xmin, ymin, xmax, ymax, W, H):
    # PIL crop은 (left, upper, right, lower)에서 right/lower == W/H까지 허용
    xmin = max(0, min(int(xmin), W))
    xmax = max(0, min(int(xmax), W))
    ymin = max(0, min(int(ymin), H))
    ymax = max(0, min(int(ymax), H))
    return xmin, ymin, xmax, ymax

def _box_valid(xmin, ymin, xmax, ymax, W, H, min_frac: float = 1e-4) -> bool:
    # 좌상단 < 우하단, 면적 > 0, 너무 작은 상자 거르기(이미지의 0.01% 미만이면 무효로 간주)
    xmin, ymin, xmax, ymax = _clamp_box(xmin, ymin, xmax, ymax, W, H)
    if not (xmax > xmin and ymax > ymin):
        return False
    img_area = max(W * H, 1)
    box_area = (xmax - xmin) * (ymax - ymin)
    return (box_area / img_area) >= min_frac


# =========================
# 디바이스 & 임베딩
# =========================
device = "cuda" if torch.cuda.is_available() else "cpu"
clip_model, _, clip_preprocess = open_clip.create_model_and_transforms(
    EMBED_BACKBONE, pretrained=EMBED_PRETRAIN
)
clip_model = clip_model.to(device).eval()

@torch.no_grad()
def embed_pil(pil: Image.Image) -> np.ndarray:
    t = clip_preprocess(pil).unsqueeze(0).to(device)
    e = clip_model.encode_image(t)
    e = e / e.norm(dim=-1, keepdim=True)
    return e.squeeze(0).detach().cpu().numpy().astype("float32")  # (512,)

# =========================
# 크롭 & 보안 유틸
# =========================
def pad_box(xmin:int, ymin:int, xmax:int, ymax:int, W:int, H:int, pad:float=0.15):
    w, h = xmax - xmin, ymax - ymin
    px, py = int(w*pad), int(h*pad)
    return max(0, xmin-px), max(0, ymin-py), min(W, xmax+px), min(H, ymax+py)

def face_like(xmin:int, ymin:int, xmax:int, ymax:int, ratio:float=0.65):
    h = ymax - ymin
    return xmin, ymin, xmax, int(ymin + h*ratio)

def _check_token(x_service_token: Optional[str]):
    if SERVICE_TOKEN and x_service_token != SERVICE_TOKEN:
        raise HTTPException(status_code=403, detail="forbidden")

def _resolve_path(path_or_websrc: str) -> str:
    if not path_or_websrc:
        raise HTTPException(status_code=400, detail="empty path")
    p = path_or_websrc
    if p.startswith("/images/"):
        rel  = p[len("/images/"):]
        full = os.path.join(BASE_IMAGE_DIR, rel)
    else:
        full = p
    real = os.path.realpath(full)
    base = os.path.realpath(BASE_IMAGE_DIR) + os.sep
    if not real.startswith(base):
        raise HTTPException(status_code=400, detail="invalid path")
    if not os.path.isfile(real):
        raise HTTPException(status_code=404, detail="file not found")
    return real

# =========================
# FAISS (dog/cat × body/face) - 메모리만
# =========================
DIM, HNSW_M, HNSW_EF = 512, 32, 128
def make_hnsw():
    base = faiss.IndexHNSWFlat(DIM, HNSW_M, faiss.METRIC_INNER_PRODUCT)
    base.hnsw.efSearch = HNSW_EF
    return faiss.IndexIDMap(base)

IDX = {
    "dog": {"body": make_hnsw(), "face": make_hnsw()},
    "cat": {"body": make_hnsw(), "face": make_hnsw()},
}
V_BODY: Dict[int, np.ndarray] = {}
V_FACE: Dict[int, np.ndarray] = {}

# =========================
# 공통 로직: bbox 결정(온디 주면 사용 → 없으면 옵션에 따라 폴백)
# =========================
def decide_bbox(img: Image.Image, species: str,
                xmin: Optional[int], ymin: Optional[int],
                xmax: Optional[int], ymax: Optional[int]) -> tuple[int,int,int,int]:
    W, H = img.size

    # 0) 0,0,0,0은 "미제공"으로 취급
    if _box_provided(xmin, ymin, xmax, ymax) and _box_is_zero(xmin, ymin, xmax, ymax):
        xmin = ymin = xmax = ymax = None

    # 1) 온디바이스가 bbox를 줬고, 유효하면 사용
    if _box_provided(xmin, ymin, xmax, ymax):
        if _box_valid(xmin, ymin, xmax, ymax, W, H):
            x1, y1, x2, y2 = _clamp_box(xmin, ymin, xmax, ymax, W, H)
            return pad_box(x1, y1, x2, y2, W, H, 0.15)
        # 유효하지 않은 bbox: 필수모드면 400, 아니면 폴백
        if REQUIRE_BBOX:
            raise HTTPException(status_code=400, detail="invalid bbox (zero/negative area or out of bounds)")
        # 계속 폴백 진행

    # 2) bbox 필수 모드인데 미제공
    if REQUIRE_BBOX:
        raise HTTPException(status_code=400, detail="bbox required (xmin,ymin,xmax,ymax)")

    # 3) 서버 YOLO 폴백
    if yolo is not None:
        r = yolo.predict(img, verbose=False)[0]
        if len(r.boxes) > 0:
            names = r.names
            for i in range(len(r.boxes)):
                cls = int(r.boxes.cls[i].item())
                if names[cls] == species:
                    bxmin, bymin, bxmax, bymax = list(map(int, r.boxes.xyxy[i].tolist()))
                    return pad_box(bxmin, bymin, bxmax, bymax, W, H, 0.15)

    # 4) 최후: 전체 이미지 허용?
    if ALLOW_WHOLE_IMAGE:
        return (0, 0, W, H)

    raise HTTPException(status_code=400, detail="no bbox and fallback disabled")

# =========================
# FastAPI
# =========================
app = FastAPI(title="LostPet Similarity Service", version="1.0")

@app.get("/health")
def health():
    return {
        "ok": True,
        "device": device,
        "embed": f"{EMBED_BACKBONE}/{EMBED_PRETRAIN}",
        "base_image_dir": BASE_IMAGE_DIR,
        "use_server_yolo": USE_SERVER_YOLO,
        "require_bbox": REQUIRE_BBOX,
        "allow_whole_image": ALLOW_WHOLE_IMAGE,
    }

# ---------- 인덱싱 (경로) ----------
@app.post("/index/add_path")
async def index_add_path(
    image_id: int = Form(...),
    breed_eng: Optional[str] = Form(None),

    species: str = Form(...),     # "dog" | "cat"
    path: str = Form(...),        # "/images/xxx.jpg" or "/data/images/xxx.jpg"
    xmin: Optional[int] = Form(None),
    ymin: Optional[int] = Form(None),
    xmax: Optional[int] = Form(None),
    ymax: Optional[int] = Form(None),
    w_face: float = Form(W_FACE_DEFAULT),
    x_service_token: Optional[str] = Header(None),
):
    _check_token(x_service_token)
    species = species.lower()
    if species not in ("dog","cat"):
        return JSONResponse({"ok":False,"msg":"species must be dog or cat"}, status_code=400)

    img = Image.open(_resolve_path(path)).convert("RGB")
    bxmin, bymin, bxmax, bymax = decide_bbox(img, species, xmin, ymin, xmax, ymax)

    fxmin, fymin, fxmax, fymax = face_like(bxmin, bymin, bxmax, bymax, 0.65)
    body = img.crop((bxmin, bymin, bxmax, bymax))
    face = img.crop((fxmin, fymin, fxmax, fymax))

    vb, vf = embed_pil(body), embed_pil(face)
    IDX[species]["body"].add_with_ids(vb.reshape(1,-1), np.array([image_id], np.int64))
    IDX[species]["face"].add_with_ids(vf.reshape(1,-1), np.array([image_id], np.int64))
    V_BODY[image_id], V_FACE[image_id] = vb, vf

    return {"ok": True, "id": image_id, "w_face": w_face}

# ---------- 인덱싱 (바이너리) ----------
@app.post("/index/add")
async def index_add(
    image_id: int = Form(...),
    species: str = Form(...),
    breed_eng: Optional[str] = Form(None),

    file: UploadFile = File(...),
    xmin: Optional[int] = Form(None),
    ymin: Optional[int] = Form(None),
    xmax: Optional[int] = Form(None),
    ymax: Optional[int] = Form(None),
    w_face: float = Form(W_FACE_DEFAULT),
    x_service_token: Optional[str] = Header(None),
):
    _check_token(x_service_token)
    species = species.lower()
    if species not in ("dog","cat"):
        return JSONResponse({"ok":False,"msg":"species must be dog or cat"}, status_code=400)

    data = await file.read()
    img = Image.open(io.BytesIO(data)).convert("RGB")
    bxmin, bymin, bxmax, bymax = decide_bbox(img, species, xmin, ymin, xmax, ymax)

    fxmin, fymin, fxmax, fymax = face_like(bxmin, bymin, bxmax, bymax, 0.65)
    body = img.crop((bxmin, bymin, bxmax, bymax))
    face = img.crop((fxmin, fymin, fxmax, fymax))

    vb, vf = embed_pil(body), embed_pil(face)
    IDX[species]["body"].add_with_ids(vb.reshape(1,-1), np.array([image_id], np.int64))
    IDX[species]["face"].add_with_ids(vf.reshape(1,-1), np.array([image_id], np.int64))
    V_BODY[image_id], V_FACE[image_id] = vb, vf

    return {"ok": True, "id": image_id, "w_face": w_face}

# ---------- 검색 (경로) ----------
@app.post("/search_path")
async def search_path(
    q_breed_eng: Optional[str] = Form(None),

    species: str = Form(...),
    path: str = Form(...),
    topk: int = Form(20),
    w_face: float = Form(W_FACE_DEFAULT),
    xmin: Optional[int] = Form(None),
    ymin: Optional[int] = Form(None),
    xmax: Optional[int] = Form(None),
    ymax: Optional[int] = Form(None),
    x_service_token: Optional[str] = Header(None),
):
    _check_token(x_service_token)
    species = species.lower()
    if species not in ("dog","cat"):
        return JSONResponse({"ok":False,"msg":"species must be dog or cat"}, status_code=400)

    img = Image.open(_resolve_path(path)).convert("RGB")
    bxmin, bymin, bxmax, bymax = decide_bbox(img, species, xmin, ymin, xmax, ymax)

    fxmin, fymin, fxmax, fymax = face_like(bxmin, bymin, bxmax, bymax, 0.65)
    body = img.crop((bxmin, bymin, bxmax, bymax))
    face = img.crop((fxmin, fymin, fxmax, fymax))

    vb, vf = embed_pil(body), embed_pil(face)
    D_b, I_b = IDX[species]["body"].search(vb.reshape(1,-1), topk)
    D_f, I_f = IDX[species]["face"].search(vf.reshape(1,-1), topk)

    cand = set(I_b[0].tolist()) | set(I_f[0].tolist())
    fused = []
    for cid in cand:
        if cid == -1 or cid not in V_BODY:
            continue
        sb = float(np.dot(vb, V_BODY[cid]))
        sf = float(np.dot(vf, V_FACE[cid]))
        fused.append({"id": int(cid), "score": w_face*sf + (1-w_face)*sb})
    fused.sort(key=lambda x: x["score"], reverse=True)
    return {"ok": True, "results": fused[:topk]}

# ---------- 검색 (바이너리) ----------
@app.post("/search")
async def search(
    q_breed_eng: Optional[str] = Form(None),

    species: str = Form(...),
    file: UploadFile = File(...),
    topk: int = Form(20),
    w_face: float = Form(W_FACE_DEFAULT),
    xmin: Optional[int] = Form(None),
    ymin: Optional[int] = Form(None),
    xmax: Optional[int] = Form(None),
    ymax: Optional[int] = Form(None),
    x_service_token: Optional[str] = Header(None),
):
    _check_token(x_service_token)
    species = species.lower()
    if species not in ("dog","cat"):
        return JSONResponse({"ok":False,"msg":"species must be dog or cat"}, status_code=400)

    data = await file.read()
    img = Image.open(io.BytesIO(data)).convert("RGB")
    bxmin, bymin, bxmax, bymax = decide_bbox(img, species, xmin, ymin, xmax, ymax)

    fxmin, fymin, fxmax, fymax = face_like(bxmin, bymin, bxmax, bymax, 0.65)
    body = img.crop((bxmin, bymin, bxmax, bymax))
    face = img.crop((fxmin, fymin, fxmax, fymax))

    vb, vf = embed_pil(body), embed_pil(face)
    D_b, I_b = IDX[species]["body"].search(vb.reshape(1,-1), topk)
    D_f, I_f = IDX[species]["face"].search(vf.reshape(1,-1), topk)

    cand = set(I_b[0].tolist()) | set(I_f[0].tolist())
    fused = []
    for cid in cand:
        if cid == -1 or cid not in V_BODY:
            continue
        sb = float(np.dot(vb, V_BODY[cid]))
        sf = float(np.dot(vf, V_FACE[cid]))
        fused.append({"id": int(cid), "score": w_face*sf + (1-w_face)*sb})
    fused.sort(key=lambda x: x["score"], reverse=True)
    return {"ok": True, "results": fused[:topk]}
