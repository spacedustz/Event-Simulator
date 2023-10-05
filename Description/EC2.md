## 📘 EC2 Setting

쉘 스크립트를 작성해 한번에 모든 세팅이 준비되게 하였습니다.

```bash
#!/bin/bash

# APT 업그레이드 & 업데이트
apt -y upgrade && apt -y update
echo ----- APT Update 종료 ---- | tee setting_logs

# HTTPS 관련 패키지 & 유틸 패키지 설치
apt install -y firewalld net-tools curl wget gnupg lsb-release ca-certificates apt-transport-https software-properties-common gnupg-agent openjdk-17-jdk
echo ----- 기본 패키지 설치 완료 ----- >> setting_logs

# OpenJDK 전역변수 설정
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
echo ----- $JAVA_HOME ----- >> setting_logs

# Firewalld 시작 & 서비스 등록
systemctl start firewalld && systemctl enable firewalld
echo ----- Firewalld 서비스 등록 ----- >> setting_logs

# Firewall 방화벽 포트 오픈
firewall-cmd --permanent --add-port=22/tcp
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --permanent --add-port=1833/tcp
firewall-cmd --permanent --add-port=3000/tcp
firewall-cmd --permanent --add-port=4369/tcp
firewall-cmd --permanent --add-port=5672/tcp
firewall-cmd --permanent --add-port=15672/tcp
firewall-cmd --permanent --add-port=5673/tcp
firewall-cmd --permanent --add-port=15673/tcp
firewall-cmd --permanent --add-port=5674/tcp
firewall-cmd --permanent --add-port=15674/tcp
firewall-cmd --permanent --add-port=5675/tcp
firewall-cmd --permanent --add-port=15675/tcp
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --permanent --add-port=18080/tcp
firewall-cmd --reload
echo ----- Firewalld 포트 오픈 ----- >> setting_logs

# 도커 GPG Key 추가
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

# 도커 저장소 설정
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

# 도커 엔진 설치
apt install -y docker-ce docker-ce-cli containerd.io
echo ----- 도커 설치 완료 ----- >> setting_logs

# ec2-user에 Docker 명령 권한 부여
usermod -aG docker ec2-user

# 도커 시작 & 서비스 등록
systemctl start docker && systemctl enable docker
echo ----- 도커 시작 ----- >> setting_logs

# RabbitMQ Container 실행
sudo docker run -d --name rabbitmq1 -p 5672:5672 -p 15672:15672 rabbitmq:3-management &&
sudo docker run -d --name rabbitmq2 -p 5673:5672 -p 15673:15672 rabbitmq:3-management &&
sudo docker run -d --name rabbitmq3 -p 5674:5672 -p 15674:15672 rabbitmq:3-management &&
sudo docker run -d --name rabbitmq4 -p 5675:5672 -p 15675:15672 rabbitmq:3-management
