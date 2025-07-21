from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
import pickle
import cv2
import numpy as np
import torch
import torchvision.models as models
import torch.nn as nn
from PIL import Image
import torchvision.transforms as transforms
import io
import os
from typing import Optional

app = FastAPI(title="Dog Breed Predictor", description="Find out which dog breed you look like!")

# 전역 변수들
loaded_model = None
class_names = None

def load_model_from_pth(pth_path: str):
    """pth 파일에서 모델 패키지 로드"""
    print(f"🔍 Loading model from {pth_path}")
    try:
        # CPU로 모델 로드
        model_package = torch.load(pth_path, map_location=torch.device('cpu'))
        print(f"✅ Model package loaded successfully!")
        return model_package
    except FileNotFoundError:
        print(f"❌ Model file {pth_path} not found")
        return None
    except Exception as e:
        print(f"❌ Error loading model package: {e}")
        return None

def rebuild_mobilenet_model(model_package: dict, use_cuda: bool = True):
    """패키지에서 MobileNet V2 모델 재구성"""
    if model_package is None:
        return None

    try:
        # MobileNet V2 모델 구조 재생성
        model = models.mobilenet_v2(weights=None) # 가중치는 불러올 것이므로 None
        
        # 마지막 분류층을 저장된 클래스 수에 맞게 변경
        n_inputs = model.classifier[1].in_features
        num_classes = model_package['num_classes']
        model.classifier[1] = nn.Linear(n_inputs, num_classes)

        # 저장된 가중치 로드
        model.load_state_dict(model_package['model_state_dict'])

        # 평가 모드로 설정
        model.eval()

        # GPU 사용 설정
        if use_cuda and torch.cuda.is_available():
            model = model.to("cuda")
            print("✅ MobileNetV2 Model loaded on GPU")
        else:
            model = model.to("cpu")
            print("✅ MobileNetV2 Model loaded on CPU")

        return model
    except Exception as e:
        print(f"❌ Error rebuilding MobileNetV2 model: {e}")
        return None

def face_detector(image_bytes: bytes) -> bool:
    """이미지에서 얼굴 검출"""
    try:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if img is None:
            return False

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
        faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5)

        return len(faces) > 0
    except Exception as e:
        print(f"❌ Face detection error: {e}")
        return False

def predict_breed_from_image(image_bytes: bytes) -> Optional[str]:
    """이미지에서 개 품종 예측"""
    if loaded_model is None or class_names is None:
        return None

    try:
        image = Image.open(io.BytesIO(image_bytes)).convert('RGB')

        # 이미지 전처리 (MobileNetV2와 VGG16 모두 224x224 사용)
        transformations = transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406],
                                 std=[0.229, 0.224, 0.225])
        ])

        image_tensor = transformations(image).unsqueeze(0)

        device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
        image_tensor = image_tensor.to(device)
        
        with torch.no_grad():
            output = loaded_model(image_tensor)
            _, pred_tensor = torch.max(output, 1)
            pred = pred_tensor.cpu().numpy()[0]

        return class_names[pred]
    except Exception as e:
        print(f"❌ Prediction error: {e}")
        return None

@app.on_event("startup")
async def startup_event():
    global loaded_model, class_names

    print("🚀 Starting up FastAPI Dog Breed Predictor...")

    # 새로운 MobileNet 모델 로드 시도
    model_package = load_model_from_pth('mobilenet_dog_classifier.pth')
    if model_package:
        use_cuda = torch.cuda.is_available()
        loaded_model = rebuild_mobilenet_model(model_package, use_cuda=use_cuda)
        class_names = model_package['class_names']
        
        if loaded_model:
            print(f"📝 Loaded {len(class_names)} class names for MobileNetV2 model.")
        
        if not use_cuda:
            print("⚠️ CUDA not available. Running on CPU mode.")
    else:
        print("⚠️ Failed to load any model. Some features may not work.")

@app.get("/", response_class=HTMLResponse)
async def main_page():
    # HTML content (이전과 동일)
    html_content = """ 
    <!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>강아지 닮은꼴 찾기 🐾</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans+KR&display=swap" rel="stylesheet">

    <style>
        * {
            box-sizing: border-box;
        }
        
        body {
            font-family: "IBM Plex Sans KR", sans-serif;
            font-weight: 450;
            font-style: normal;
            background-color: #FFF4F2;
            text-align: center;
            padding: 10px;
            margin: 0;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: flex-start;
        }
        
        .container {
            width: 100%;
            max-width: 500px;
            margin: 0 auto;
        }
        
        h1 {
            color: #FF4C8B;
            font-size: 2.2em;
            margin: 20px 0;
            font-weight: 400;
        }
        
        .subheading {
            background-color: #FFB5A7;
            color: white;
            padding: 15px;
            margin: 0 10px 30px 10px;
            font-size: 1.1em;
            border-radius: 12px;
            font-weight: 400;
            line-height: 1.4;
        }
        
        .upload-form {
            background-color: #FFE5DC;
            border: 3px dashed #FFB5A7;
            padding: 30px 20px;
            margin: 0 10px;
            border-radius: 15px;
            cursor: pointer;
            transition: all 0.3s ease;
            width: calc(100% - 20px);
        }
        
        .upload-form:hover {
            border-color: #FF6F91;
            background-color: #FFD5CC;
        }
        
        .upload-form p {
            margin: 10px 0;
            font-weight: 500;
        }
        
        input[type="file"] {
            display: none;
        }
        
        .upload-btn {
            background-color: #FF6F91;
            color: white;
            border: none;
            padding: 15px 30px;
            font-size: 1.1em;
            border-radius: 12px;
            cursor: pointer;
            margin: 20px 10px;
            transition: background-color 0.3s;
            font-weight: bold;
            font-family: inherit;
            width: calc(100% - 20px);
            max-width: 300px;
        }
        
        .upload-btn:hover {
            background-color: #FF4C8B;
        }
        
        .upload-btn:disabled {
            background-color: #ccc;
            cursor: not-allowed;
        }
        
        #preview {
            margin: 20px 10px;
        }
        
        .preview-img {
            max-width: calc(100% - 20px);
            max-height: 350px;
            border-radius: 15px;
            margin: 20px auto;
            display: block;
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        
        .result {
            margin: 30px 10px;
            padding: 25px;
            background-color: white;
            border-radius: 15px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        
        .result h2 {
            color: #FF4C8B;
            font-size: 1.8em;
            margin-bottom: 20px;
            font-weight: bold;
        }
        
        .result-animal {
            font-size: 2.2em;
            color: #FF6F91;
            font-weight: 400;
            margin: 20px 0;
            line-height: 1.2;
        }
        
        .loading {
            color: #FF6F91;
            font-size: 1.2em;
            margin: 20px 10px;
            font-weight: 500;
        }
        
        .error {
            color: #ff4444;
            background-color: #ffe6e6;
            border: 2px solid #ff4444;
            padding: 20px;
            border-radius: 12px;
            margin: 20px 10px;
            font-weight: 500;
        }
        
        .survey {
            margin: 50px 10px 20px 10px;
            background-color: #fff;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
            width: calc(100% - 20px);
        }
        
        .survey p {
            font-weight: 400;
            line-height: 1.5;
            margin-bottom: 15px;
        }
        
        .survey a {
            display: inline-block;
            margin-top: 15px;
            background-color: #FF4C8B;
            color: white;
            padding: 12px 25px;
            border-radius: 10px;
            text-decoration: none;
            font-weight: 600;
            transition: background-color 0.3s;
        }
        
        .survey a:hover {
            background-color: #FF6F91;
        }
        
        .status-indicator {
            position: fixed;
            top: 15px;
            right: 15px;
            padding: 8px 12px;
            border-radius: 10px;
            font-size: 0.85em;
            font-weight: 600;
            z-index: 1000;
        }
        
        .status-ok {
            background: rgba(76, 175, 80, 0.9);
            color: white;
        }
        
        .status-error {
            background: rgba(244, 67, 54, 0.9);
            color: white;
        }
        
        /* 모바일 최적화 */
        @media (max-width: 480px) {
            body {
                padding: 5px;
            }
            
            h1 {
                font-size: 1.8em;
                font-weight: bold;
                margin: 15px 0;
            }
            
            .subheading {
                font-size: 1em;
                font-weight: bold;
                padding: 12px;
                margin: 0 5px 25px 5px;
            }
            
            .upload-form {
                padding: 25px 15px;
                margin: 0 5px;
                width: calc(100% - 10px);
            }
            
            .upload-btn {
                padding: 12px 20px;
                font-size: 1em;
                margin: 15px 5px;
                width: calc(100% - 10px);
            }
            
            #preview {
                margin: 15px 5px;
            }
            
            .preview-img {
                max-width: calc(100% - 10px);
                max-height: 300px;
            }
            
            .result {
                margin: 25px 5px;
                padding: 20px;
            }
            
            .result h2 {
                font-size: 1.6em;
            }
            
            .result-animal {
                font-size: 1.8em;
            }
            
            .survey {
                margin: 40px 5px 15px 5px;
                padding: 20px;
                width: calc(100% - 10px);
            }
            
            .status-indicator {
                top: 10px;
                right: 10px;
                padding: 6px 10px;
                font-size: 0.8em;
            }
            
            .loading {
                font-size: 1.1em;
                margin: 15px 5px;
            }
            
            .error {
                margin: 15px 5px;
                padding: 15px;
            }
        }
        
        /* 아주 작은 화면 (320px 이하) */
        @media (max-width: 320px) {
            h1 {
                font-size: 1.6em;
            }
            
            .result-animal {
                font-size: 1.6em;
            }
            
            .upload-form {
                padding: 20px 10px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div id="statusIndicator" class="status-indicator">모델 상태 확인 중...</div>
        
        <h1>강아지 닮은꼴 찾기 🐾🐕</h1>
        <div class="subheading">나는 어떤 강아지와 가장 닮았을까? <br>
            선명하게 나온 셀카 사진을 업로드 하면, <br>
            AI를 통해 나와 닮은 강아지 품종을 확인할 수 있습니다.
            <br>
            <br>
            - 업로드한 사진은 분석 후 삭제됩니다.
        </div>
        
        <div class="upload-form" onclick="document.getElementById('fileInput').click()">
            <p>📷 여기를 클릭해서 사진을 업로드하세요</p>
            <p style="font-size: 0.9em; color: #666;">JPG, PNG 파일 지원</p>
            <input type="file" id="fileInput" accept="image/*" onchange="previewImage()">
        </div>

        <div id="preview"></div>

        <button class="upload-btn" id="analyzeBtn" onclick="analyzeImage()" disabled>
            🔍 나와 닮은 강아지 찾기!
        </button>

        <div id="result"></div>

        <div class="survey">
            <p>설문조사에 참여해주시면<br>추첨을 통해 소정의 기프티콘을 드립니다.<br>감사합니다 😊</p>
            <a href="https://docs.google.com/forms/d/e/1FAIpQLSfM0CURwgynFKiXDLbLxwsHoBIdyhgKRsPZrGSlI-_ScEU1NA/viewform" target="_blank">설문조사 바로가기</a>
        </div>

        <footer class="footer">
            <p>SSAFY 13기 구미 1반 민적4고 팀</p>
        </footer>
    </div>

    <script>
        let selectedFile = null;

        // 페이지 로드 시 모델 상태 확인
        window.onload = function() {
            checkModelStatus();
        };

        async function checkModelStatus() {
            const statusIndicator = document.getElementById('statusIndicator');
            try {
                const response = await fetch('/health');
                const status = await response.json();
                
                if (status.model_status === 'loaded') {
                    statusIndicator.textContent = '✅ AI 준비완료';
                    statusIndicator.className = 'status-indicator status-ok';
                } else {
                    statusIndicator.textContent = '❌ AI 로딩 중';
                    statusIndicator.className = 'status-indicator status-error';
                }
            } catch (error) {
                statusIndicator.textContent = '❌ 서버 오류';
                statusIndicator.className = 'status-indicator status-error';
            }
        }

        function previewImage() {
            const fileInput = document.getElementById('fileInput');
            const preview = document.getElementById('preview');
            const analyzeBtn = document.getElementById('analyzeBtn');

            if (fileInput.files && fileInput.files[0]) {
                selectedFile = fileInput.files[0];
                const reader = new FileReader();

                reader.onload = function(e) {
                    preview.innerHTML = '<img src="' + e.target.result + '" class="preview-img" alt="미리보기">';
                    analyzeBtn.disabled = false;
                }

                reader.readAsDataURL(fileInput.files[0]);
            }
        }

        async function analyzeImage() {
            if (!selectedFile) {
                alert('먼저 사진을 선택해주세요!');
                return;
            }

            const resultDiv = document.getElementById('result');
            const analyzeBtn = document.getElementById('analyzeBtn');

            // 로딩 상태
            resultDiv.innerHTML = '<div class="loading">🔍 사진을 분석 중입니다... 잠시만 기다려주세요!</div>';
            analyzeBtn.disabled = true;

            const formData = new FormData();
            formData.append('file', selectedFile);

            try {
                const response = await fetch('/predict', {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (response.ok) {
                    if (result.success) {
                        resultDiv.innerHTML = `
                            <div class="result">
                                <h2>🎉 분석 완료!</h2>
                                <p>당신과 닮은 동물은:</p>
                                <div class="result-animal">${result.breed}</div>
                                <p style="color: #666; margin-top: 20px;">
                                    이 품종이 당신의 특징과 가장 잘 어울려요! 🐾
                                </p>
                            </div>
                        `;
                    } else {
                        resultDiv.innerHTML = `
                            <div class="error">
                                <h3>😅 ${result.message}</h3>
                                <p>얼굴이 선명하게 나온 사진을 다시 업로드해주세요!</p>
                            </div>
                        `;
                    }
                } else {
                    throw new Error(result.detail || '알 수 없는 오류가 발생했습니다.');
                }
            } catch (error) {
                resultDiv.innerHTML = `
                    <div class="error">
                        <h3>❌ 오류가 발생했습니다</h3>
                        <p>${error.message}</p>
                        <p>잠시 후 다시 시도해주세요.</p>
                    </div>
                `;
            } finally {
                analyzeBtn.disabled = false;
            }
        }
    </script>
</body>
</html>
    """
    return HTMLResponse(content=html_content)

@app.post("/predict")
async def predict_dog_breed(file: UploadFile = File(...)):
    """이미지 업로드 후 개 품종 예측"""
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="Please upload an image file")

    try:
        image_bytes = await file.read()

        if loaded_model is None:
            raise HTTPException(status_code=500, detail="Model not loaded. Please check server logs.")

        if not face_detector(image_bytes):
            return JSONResponse(content={
                "success": False,
                "message": "No human face detected in the image! 👤❌"
            })

        predicted_breed = predict_breed_from_image(image_bytes)

        if predicted_breed is None:
            raise HTTPException(status_code=500, detail="Failed to predict breed")

        return JSONResponse(content={
            "success": True,
            "breed": predicted_breed,
            "message": f"You look like a {predicted_breed}! 🐕"
        })

    except Exception as e:
        print(f"❌ Error in prediction: {e}")
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")

@app.get("/health")
async def health_check():
    """서버 상태 확인"""
    model_status = "loaded" if loaded_model is not None else "not loaded"
    return {
        "status": "healthy",
        "model_status": model_status,
        "classes_loaded": len(class_names) if class_names else 0
    }

if __name__ == "__main__":
    import uvicorn

    print("🚀 Starting Dog Breed Predictor Server...")
    print("📍 Server will be available at: http://localhost:8000")
    print("📋 API docs will be available at: http://localhost:8000/docs")
    uvicorn.run(app, host="0.0.0.0", port=8000)
