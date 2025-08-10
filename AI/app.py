# app.py
import io, os, time, json
from typing import Dict, Tuple, List, Optional

import numpy as np
from PIL import Image
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse

import torch
from torchvision import transforms
import timm

import faiss
import open_clip
from ultralytics import YOLO


# ---------------------------
# 환경설정
# ---------------------------
EMBED_BACKBONE = os.getenv("EMBED_BACKBONE", "ViT-B-32")
EMBED_PRETRAIN = os.getenv("EMBED_PRETRAIN", "openai")
INDEX_DIR = os.getenv("INDEX_DIR", "/app/index_data")
USE_SERVER_YOLO = os.getenv("USE_SERVER_YOLO", "1") == "1"
W_FACE_DEFAULT = float(os.getenv("W_FACE", "0.6"))

os.makedirs(INDEX_DIR, exist_ok=True)

# HNSW 파라미터
DIM, HNSW_M, HNSW_EF = 512, 32, 128

# ---------------------------
# 디바이스
# ---------------------------
device = "cuda" if torch.cuda.is_available() else "cpu"

en_to_ko = {
    'affenpinscher': '아펜핀셔',
    'afghan_hound': '아프간 하운드',
    'airedale': '에어데일 테리어',
    'akita': '아키타',
    'appenzeller': '아펜젤러',
    'australian_terrier': '오스트레일리언 테리어',
    'basenji': '바센지',
    'basset': '바셋 하운드',
    'beagle': '비글',
    'bedlington_terrier': '베들링턴 테리어',
    'bernese_mountain_dog': '버니즈 마운틴 독',
    'black-and-tan_coonhound': '블랙 앤 탄 쿤하운드',
    'blenheim_spaniel': '블레넘 스패니얼',
    'bloodhound': '블러드하운드',
    'border_collie': '보더 콜리',
    'border_terrier': '보더 테리어',
    'borzoi': '보르조이',
    'boston_bull': '보스턴 테리어',
    'bouvier_des_flandres': '부비에 데 플랑드르',
    'boxer': '복서',
    'brabancon_griffon': '브라반손 그리폰',
    'briard': '브리아드',
    'brittany_spaniel': '브리타니 스패니얼',
    'bull_mastiff': '불마스티프',
    'cairn': '케언 테리어',
    'cardigan': '카디건 웰시 코기',
    'chesapeake_bay_retriever': '체서피크 베이 리트리버',
    'chihuahua': '치와와',
    'chow': '차우차우',
    'clumber': '클럼버 스패니얼',
    'cocker_spaniel': '코커 스패니얼',
    'collie': '콜리',
    'curly-coated_retriever': '컬리 코티드 리트리버',
    'dandie_dinmont': '댄디 딘몬트 테리어',
    'dhole': '도레(아시아 야생개)',
    'dingo': '딩고',
    'doberman': '도베르만',
    'english_foxhound': '잉글리시 폭스하운드',
    'english_setter': '잉글리시 세터',
    'english_springer': '잉글리시 스프링거 스패니얼',
    'entlebucher': '엔틀부허 마운틴 독',
    'eskimo_dog': '에스키모 도그',
    'flat-coated_retriever': '플랫 코티드 리트리버',
    'french_bulldog': '프렌치 불도그',
    'german_shepherd': '저먼 셰퍼드',
    'german_short-haired_pointer': '저먼 쇼트헤어드 포인터',
    'giant_schnauzer': '자이언트 슈나우저',
    'golden_retriever': '골든 리트리버',
    'gordon_setter': '고든 세터',
    'great_dane': '그레이트 데인',
    'great_pyrenees': '그레이트 피레니즈',
    'greater_swiss_mountain_dog': '그레이터 스위스 마운틴 독',
    'groenendael': '그루넨달',
    'ibizan_hound': '이비자 하운드',
    'irish_setter': '아이리시 세터',
    'irish_terrier': '아이리시 테리어',
    'irish_water_spaniel': '아이리시 워터 스패니얼',
    'irish_wolfhound': '아이리시 울프하운드',
    'italian_greyhound': '이탈리안 그레이하운드',
    'japanese_spaniel': '일본 스패니얼',
    'keeshond': '키스혼드',
    'kelpie': '켈피',
    'kerry_blue_terrier': '케리 블루 테리어',
    'komondor': '코몬도르',
    'kuvasz': '쿠바즈',
    'labrador_retriever': '래브라도 리트리버',
    'lakeland_terrier': '레이클랜드 테리어',
    'leonberg': '레온베르거',
    'lhasa': '라사 압소',
    'malamute': '알래스칸 말라뮤트',
    'malinois': '말리노이즈',
    'maltese_dog': '말티즈',
    'mexican_hairless': '멕시칸 헤어리스 도그',
    'miniature_pinscher': '미니어처 핀셔',
    'miniature_poodle': '미니어처 푸들',
    'miniature_schnauzer': '미니어처 슈나우저',
    'newfoundland': '뉴펀들랜드',
    'norfolk_terrier': '노퍽 테리어',
    'norwegian_elkhound': '노르웨이 엘크하운드',
    'norwich_terrier': '노리치 테리어',
    'old_english_sheepdog': '올드 잉글리시 쉽독',
    'otterhound': '오터하운드',
    'papillon': '파피용',
    'pekinese': '페키니즈',
    'pembroke': '펨브록 웰시 코기',
    'pomeranian': '포메라니안',
    'pug': '퍼그',
    'redbone': '레드본 쿤하운드',
    'rhodesian_ridgeback': '로디지안 리지백',
    'rottweiler': '로트와일러',
    'saint_bernard': '세인트 버나드',
    'saluki': '살루키',
    'samoyed': '사모예드',
    'schipperke': '스키퍼키',
    'scotch_terrier': '스코티시 테리어',
    'scottish_deerhound': '스코티시 디어하운드',
    'sealyham_terrier': '실리햄 테리어',
    'shetland_sheepdog': '셰틀랜드 쉽독',
    'shih-tzu': '시추',
    'siberian_husky': '시베리안 허스키',
    'silky_terrier': '실키 테리어',
    'soft-coated_wheaten_terrier': '소프트 코티드 휘튼 테리어',
    'staffordshire_bullterrier': '스태퍼드셔 불테리어',
    'standard_poodle': '스탠다드 푸들',
    'standard_schnauzer': '스탠다드 슈나우저',
    'sussex_spaniel': '서식스 스패니얼',
    'tibetan_mastiff': '티베탄 마스티프',
    'tibetan_terrier': '티베탄 테리어',
    'toy_poodle': '토이 푸들',
    'toy_terrier': '토이 테리어',
    'vizsla': '비즐라',
    'walker_hound': '워커 하운드',
    'weimaraner': '와이마라너',
    'welsh_springer_spaniel': '웰시 스프링거 스패니얼',
    'west_highland_white_terrier': '웨스트 하이랜드 화이트 테리어',
    'whippet': '휘핏',
    'wire-haired_fox_terrier': '와이어헤어드 폭스 테리어',
    'yorkshire_terrier': '요크셔 테리어'
}
# ---------------------------
# 1) YOLO (탐지)
# ---------------------------
yolo = YOLO("yolo11n.pt") if USE_SERVER_YOLO else None


# ---------------------------
# 2) EfficientNet-B5 (품종)
#    - class_names.txt (120줄)
#    - best_efficientnetb5_stanforddogs.pth
# ---------------------------
IMAGE_SIZE = 456
val_transform = transforms.Compose([
    transforms.Resize(IMAGE_SIZE + 32),
    transforms.CenterCrop(IMAGE_SIZE),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
])

# 클래스 이름 로드
CLASS_NAMES: List[str] = []
with open("class_names.txt", encoding="utf-8") as f:
    CLASS_NAMES = [line.strip() for line in f if line.strip()]

# 영→한 매핑(선택)
EN2KO: Dict[str, str] = {}
if os.path.isfile("en_to_ko.json"):
    with open("en_to_ko.json", "r", encoding="utf-8") as f:
        EN2KO = json.load(f)
else:
    EN2KO = en_to_ko

breed_model = timm.create_model("efficientnet_b5", pretrained=False, num_classes=len(CLASS_NAMES))
state = torch.load("best_efficientnetb5_stanforddogs.pth", map_location="cpu")
breed_model.load_state_dict(state)
breed_model = breed_model.to(device).eval()

@torch.no_grad()
def predict_breed(pil: Image.Image) -> Dict[str, str]:
    x = val_transform(pil).unsqueeze(0).to(device)
    logits = breed_model(x)
    pred = int(logits.argmax(dim=1).item())
    cls = CLASS_NAMES[pred]
    # Stanford Dogs 형식이 'n02085936-Maltese_dog' 이런 경우도 있어서 뒤쪽 이름만 뽑기
    eng = cls.split("-")[-1].lower().replace(" ", "_")
    ko = EN2KO.get(eng, eng)
    return {"eng": eng, "ko": ko}


# ---------------------------
# 3) OpenCLIP (임베딩)
# ---------------------------
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


# ---------------------------
# 4) 보조 유틸 (크롭)
# ---------------------------
def pad_box(xmin:int, ymin:int, xmax:int, ymax:int, W:int, H:int, pad:float=0.15):
    w, h = xmax - xmin, ymax - ymin
    px, py = int(w*pad), int(h*pad)
    return max(0, xmin-px), max(0, ymin-py), min(W, xmax+px), min(H, ymax+py)

def face_like(xmin:int, ymin:int, xmax:int, ymax:int, ratio:float=0.65):
    h = ymax - ymin
    return xmin, ymin, xmax, int(ymin + h*ratio)


# ---------------------------
# 5) FAISS 인덱스 (dog/cat x body/face)
# ---------------------------
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

def save_index():
    for sp in ("dog", "cat"):
        for st in ("body","face"):
            path = os.path.join(INDEX_DIR, f"{sp}_{st}.faiss")
            faiss.write_index(IDX[sp][st].index, path)
def load_index():
    for sp in ("dog","cat"):
        for st in ("body","face"):
            path = os.path.join(INDEX_DIR, f"{sp}_{st}.faiss")
            if os.path.isfile(path):
                idx = faiss.read_index(path)
                # 읽어온 게 IndexIDMap이 아닐 수도 있어 대비
                if not isinstance(idx, faiss.IndexIDMap):
                    idx = faiss.IndexIDMap(idx)
                IDX[sp][st] = idx

# ---------------------------
# FastAPI
# ---------------------------
app = FastAPI(title="LostPet Similarity Service", version="1.0")

@app.get("/health")
def health():
    return {
        "ok": True,
        "device": device,
        "embed": f"{EMBED_BACKBONE}/{EMBED_PRETRAIN}",
        "index_dir": INDEX_DIR,
        "use_server_yolo": USE_SERVER_YOLO
    }

# 등록: 이미지 → (탐지) → 전신/얼굴 임베딩 → 인덱스 저장 + 품종(개일 때)
@app.post("/index/add")
async def index_add(
    image_id: int = Form(...),
    species: str = Form(...),   # "dog" / "cat"
    file: UploadFile = File(...),
    xmin: Optional[int] = Form(None),
    ymin: Optional[int] = Form(None),
    xmax: Optional[int] = Form(None),
    ymax: Optional[int] = Form(None),
    w_face: float = Form(W_FACE_DEFAULT),
):
    t0 = time.time()
    species = species.lower()
    if species not in ("dog","cat"):
        return JSONResponse({"ok":False, "msg":"species must be dog or cat"}, status_code=400)

    data = await file.read()
    img = Image.open(io.BytesIO(data)).convert("RGB")
    W, H = img.size

    # bbox 결정
    if None not in (xmin,ymin,xmax,ymax):
        bxmin,bymin,bxmax,bymax = pad_box(xmin,ymin,xmax,ymax,W,H,0.15)
    elif yolo is not None:
        r = yolo.predict(img, verbose=False)[0]
        if len(r.boxes)==0: return JSONResponse({"ok":False,"msg":"no bbox"}, status_code=400)
        names = r.names
        found = None
        for i in range(len(r.boxes)):
            cls = int(r.boxes.cls[i].item())
            if names[cls]==species:
                found = list(map(int, r.boxes.xyxy[i].tolist()))
                break
        if not found: return JSONResponse({"ok":False,"msg":f"no {species} bbox"}, status_code=404)
        bxmin,bymin,bxmax,bymax = pad_box(*found, W,H,0.15)
    else:
        bxmin,bymin,bxmax,bymax = 0,0,W,H

    # 전신 & 얼굴 크롭
    fxmin,fymin,fxmax,fymax = face_like(bxmin,bymin,bxmax,bymax,0.65)
    body = img.crop((bxmin,bymin,bxmax,bymax))
    face = img.crop((fxmin,fymin,fxmax,fymax))

    # 임베딩
    vb, vf = embed_pil(body), embed_pil(face)

    # 인덱스 추가
    IDX[species]["body"].add_with_ids(vb.reshape(1,-1), np.array([image_id], np.int64))
    IDX[species]["face"].add_with_ids(vf.reshape(1,-1), np.array([image_id], np.int64))
    V_BODY[image_id], V_FACE[image_id] = vb, vf

    # 품종(개일 때만)
    breed = None
    if species=="dog":
        try:
            breed = predict_breed(body)
        except Exception:
            breed = None

    dt = int((time.time()-t0)*1000)
    return {"ok": True, "id": image_id, "latency_ms": dt, "breed": breed, "w_face": w_face}

# 검색: 이미지 → (탐지) → 전신/얼굴 임베딩 → dual-search → late-fusion
@app.post("/search")
async def search(
    species: str = Form(...),
    file: UploadFile = File(...),
    topk: int = Form(20),
    w_face: float = Form(W_FACE_DEFAULT),
    xmin: Optional[int] = Form(None),
    ymin: Optional[int] = Form(None),
    xmax: Optional[int] = Form(None),
    ymax: Optional[int] = Form(None),
):
    t0 = time.time()
    species = species.lower()
    if species not in ("dog","cat"):
        return JSONResponse({"ok":False, "msg":"species must be dog or cat"}, status_code=400)

    data = await file.read()
    img = Image.open(io.BytesIO(data)).convert("RGB")
    W, H = img.size

    # bbox 결정
    if None not in (xmin,ymin,xmax,ymax):
        bxmin,bymin,bxmax,bymax = pad_box(xmin,ymin,xmax,ymax,W,H,0.15)
    elif yolo is not None:
        r = yolo.predict(img, verbose=False)[0]
        if len(r.boxes)==0: return JSONResponse({"ok":False,"msg":"no bbox"}, status_code=400)
        names = r.names
        found = None
        for i in range(len(r.boxes)):
            cls = int(r.boxes.cls[i].item())
            if names[cls]==species:
                found = list(map(int, r.boxes.xyxy[i].tolist()))
                break
        if not found: return JSONResponse({"ok":False,"msg":f"no {species} bbox"}, status_code=404)
        bxmin,bymin,bxmax,bymax = pad_box(*found, W,H,0.15)
    else:
        bxmin,bymin,bxmax,bymax = 0,0,W,H

    # 전신/얼굴
    fxmin,fymin,fxmax,fymax = face_like(bxmin,bymin,bxmax,bymax,0.65)
    body = img.crop((bxmin,bymin,bxmax,bymax))
    face = img.crop((fxmin,fymin,fxmax,fymax))

    # 임베딩
    vb, vf = embed_pil(body), embed_pil(face)

    # dual-search
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

    # 쿼리의 품종(개일 때만) 참고용
    breed = None
    if species=="dog":
        try:
            breed = predict_breed(body)
        except Exception:
            breed = None

    dt = int((time.time()-t0)*1000)
    return {"ok": True, "results": fused[:topk], "latency_ms": dt, "breed_query": breed}

# 스냅샷 저장/로드
@app.post("/snapshot/save")
def snapshot_save():
    save_index()
    return {"ok": True}

@app.post("/snapshot/load")
def snapshot_load():
    load_index()
    return {"ok": True}

@app.post("/reset")
def reset():
    global IDX, V_BODY, V_FACE
    IDX = { "dog": {"body": make_hnsw(), "face": make_hnsw()},
            "cat": {"body": make_hnsw(), "face": make_hnsw()} }
    V_BODY, V_FACE = {}, {}
    return {"ok": True}
