FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    TZ=Asia/Seoul

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential curl libgl1 libglib2.0-0 \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip \
 && pip install --no-cache-dir -r requirements.txt

COPY app.py .
COPY yolo11n.pt .
COPY best_efficientnetb5_stanforddogs.pth .
COPY class_names.txt .

RUN mkdir -p /app/index_data
VOLUME ["/app/index_data"]

EXPOSE 8083
CMD ["uvicorn","app:app","--host","0.0.0.0","--port","8083"]
