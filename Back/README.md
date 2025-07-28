# PetPlace BackEnd MD
## DB env 설정 (plugin  설치 버전)
1. build.gradle 위치에 .env 파일 생성
2. env 파일에 데이터 넣기
3. setting -> plugins -> EnvFile 설치
4.  Run/ EditConfigurations -> EnvFile 탭 체크 
5. .env 파일 경로 추가

6. 간단 테스트는 git bash 에서 export $(grep -v '^#' .env | xargs)  \ echo $DB_HOST
