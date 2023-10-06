## 📘 Event Simulator 개발

**요구 사항**

- AWS EC2 (Amazon Linux)를 생성하여 Docker 설치
- RabbitMQ Container 4개 띄우기 (각각 다른 포트로 포워딩)
- 4개의 RabbitMQ에 주기적으로 특정 포맷의 데이터를 생성 & 전달하는 시뮬레이터 개발
- 해당 시뮬레이터도 EC2 내부에 개발
- 120개의 감시 카메라를 기준으로 1개의 RabbitMQ당 30개의 카메라 인스턴스의 데이터 담당
- RabbitMQ Container 당 초당 1건 정도로 이벤트 데이터 전달
- 예를 들어, 30초 동안 카메라당 1건씩 이벤트 데이터 생성 후 RabbitMQ Container로 전달

---

## 📘 구현 정리

- [EC2 서버 세팅](https://github.com/spacedustz/Event-Simulator/blob/main/Description/EC2.md)
- [application.yml 설정](https://github.com/spacedustz/Event-Simulator/blob/main/Description/Yaml.md)
- [RabbitMQ Management Console - Queue & Exchange 바인딩](https://github.com/spacedustz/Event-Simulator/blob/main/Description/Rabbit-Console.md)
- [RabbitConfig 설정](https://github.com/spacedustz/Event-Simulator/blob/main/Description/RabbitConfig.md)
- [Message Publisher - 단순 테스트 메시지 (String) 발행](https://github.com/spacedustz/Event-Simulator/blob/main/Description/Publisher.md)
- [Message Publisher - 원본 메시지 역직렬화 -> 발행](https://github.com/spacedustz/Event-Simulator/blob/main/Description/Publisher2.md)
- [Message Receiver - 메시지 수신 테스트](https://github.com/spacedustz/Event-Simulator/blob/main/Description/Receiver.md)

---

## 📘 중간 진행 상황

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-queue.png)