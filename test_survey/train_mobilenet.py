import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import datasets, models, transforms
import os
import pandas as pd
from torch.utils.data import Dataset
from PIL import Image
import numpy as np
from torch.utils.data.sampler import SubsetRandomSampler

# 1. 데이터셋 경로 설정
data_dir = './'
train_dir = os.path.join(data_dir, 'train/')

# 2. 데이터 전처리 및 증강
data_transforms = {
    'train': transforms.Compose([
        transforms.RandomResizedCrop(224),
        transforms.RandomHorizontalFlip(),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ]),
    'valid': transforms.Compose([
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ]),
}

class DogBreedDataset(Dataset):
    def __init__(self, csv_file, root_dir, transform=None):
        self.labels_frame = pd.read_csv(csv_file)
        self.root_dir = root_dir
        self.transform = transform
        self.classes = sorted(self.labels_frame['breed'].unique())
        self.class_to_idx = {cls_name: i for i, cls_name in enumerate(self.classes)}

    def __len__(self):
        return len(self.labels_frame)

    def __getitem__(self, idx):
        img_name = os.path.join(self.root_dir, self.labels_frame.iloc[idx, 0] + '.jpg')
        image = Image.open(img_name).convert('RGB') # Ensure image is in RGB format
        label_name = self.labels_frame.iloc[idx, 1]
        label = self.class_to_idx[label_name]

        if self.transform:
            image = self.transform(image)

        return image, label

# 3. 데이터 로더 생성 (전체 수정)

# 전체 train 데이터셋 로드
full_dataset = DogBreedDataset(csv_file='labels.csv', root_dir=train_dir, transform=data_transforms['train'])

# 검증 세트 분할
validation_split = .2
shuffle_dataset = True
random_seed= 42

dataset_size = len(full_dataset)
indices = list(range(dataset_size))
split = int(np.floor(validation_split * dataset_size))
if shuffle_dataset :
    np.random.seed(random_seed)
    np.random.shuffle(indices)
train_indices, val_indices = indices[split:], indices[:split]

# 샘플러 생성
train_sampler = SubsetRandomSampler(train_indices)
valid_sampler = SubsetRandomSampler(val_indices)

# 데이터 로더 생성
dataloaders = {
    'train': torch.utils.data.DataLoader(full_dataset, batch_size=32, sampler=train_sampler, num_workers=4),
    'valid': torch.utils.data.DataLoader(full_dataset, batch_size=32, sampler=valid_sampler, num_workers=4)
}

# image_datasets 변수도 train과 valid만 있도록 수정
image_datasets = {
    'train': full_dataset, 
    'valid': full_dataset 
}

class_names = full_dataset.classes
num_classes = len(class_names)

print(f"데이터 로드 완료. 총 클래스: {num_classes}개")

# 4. MobileNet V2 모델 불러오기 및 수정
model = models.mobilenet_v2(pretrained=True)

# 기존 특징 추출 부분의 가중치를 동결
for param in model.parameters():
    param.requires_grad = False

# 새로운 분류기(classifier)로 교체
# MobileNetV2의 분류기는 `classifier[1]` 입니다.
n_inputs = model.classifier[1].in_features
model.classifier[1] = nn.Linear(n_inputs, num_classes)

# GPU 사용 설정
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
model = model.to(device)

print("MobileNet V2 모델 준비 완료. 학습은 새로운 분류기만 진행됩니다.")

# 5. 손실 함수 및 옵티마이저 설정
criterion = nn.CrossEntropyLoss()
# 옵티마이저에는 새로 추가된 분류기의 파라미터만 전달합니다.
optimizer = optim.SGD(model.classifier.parameters(), lr=0.001, momentum=0.9)

# 6. 모델 학습 함수
def train_model(model, criterion, optimizer, num_epochs=25):
    valid_loss_min = float('inf')

    for epoch in range(num_epochs):
        print(f'Epoch {epoch+1}/{num_epochs}')
        print('-' * 10)

        for phase in ['train', 'valid']:
            if phase == 'train':
                model.train()
            else:
                model.eval()

            running_loss = 0.0
            running_corrects = 0

            for inputs, labels in dataloaders[phase]:
                inputs = inputs.to(device)
                labels = labels.to(device)

                optimizer.zero_grad()

                with torch.set_grad_enabled(phase == 'train'):
                    outputs = model(inputs)
                    _, preds = torch.max(outputs, 1)
                    loss = criterion(outputs, labels)

                    if phase == 'train':
                        loss.backward()
                        optimizer.step()

                running_loss += loss.item() * inputs.size(0)
                running_corrects += torch.sum(preds == labels.data)

            epoch_loss = running_loss / len(image_datasets[phase])
            epoch_acc = running_corrects.double() / len(image_datasets[phase])

            print(f'{phase} Loss: {epoch_loss:.4f} Acc: {epoch_acc:.4f}')

            if phase == 'valid' and epoch_loss < valid_loss_min:
                valid_loss_min = epoch_loss
                # 모델 저장
                model_package = {
                    'num_classes': num_classes,
                    'class_names': class_names,
                    'model_state_dict': model.state_dict()
                }
                torch.save(model_package, 'mobilenet_dog_classifier.pth')
                print('Validation loss decreased. Saving model to mobilenet_dog_classifier.pth')

    return model

# 7. 학습 실행
if __name__ == '__main__':
    print("모델 학습을 시작합니다...")
    trained_model = train_model(model, criterion, optimizer, num_epochs=10) # 시간 관계상 epoch는 10으로 설정
    print("학습 완료!")