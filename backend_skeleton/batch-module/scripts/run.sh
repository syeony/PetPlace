#!/bin/bash

DEPLOY_PATH=실행할 프로젝트 jar 파일 경로 [ex. /home/dev/projects/ssafy-batch/bin]
LOG_PATH=로그 경로 [ex. /home/dev/projects/ssafy-batch/log]
JAVA_OPTS="-XX:MaxMetaspaceSize=128m -XX:+UseG1GC -Xss1024k -Xms128m -Xmx128m -Dfile.encoding=UTF-8"
PORT=포트번호 [ex. 30001]
PROJECT=프로젝트 jar 파일 명 [ex. ssafy-batch-module-0.0.1.jar]
PROFILE=활성화 할 application [ex. local]

runPid=$(pgrep -f $PROJECT)
if [ -z $runPid ]; then
    echo "No servers are running"
fi

runPortCount=$(ps -ef | grep $PROJECT | grep -v grep | grep $PORT | wc -l)
if [ $runPortCount -gt 0 ]; then
   kill -9 $runPid
   echo "kill $runPid"
fi
echo "Server $PORT Starting..."


nohup java -jar -Duser.timezone=Asia/Seoul  -Dspring.profiles.active=$PROFILE -Dserver.port=$PORT $JAVA_OPTS $DEPLOY_PATH/$PROJECT --job.name=$1 version="$(date +'%y%m%d%H%M%S')" < /dev/null > ${LOG_PATH}/std.out 2> ${LOG_PATH}/std.err &

#java -jar -Duser.timezone=Asia/Seoul -Dserver.port=$PORT -Dspring.profiles.active=$PROFILE $JAVA_OPTS $DEPLOY_PATH/$PROJECT
