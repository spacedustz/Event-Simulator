## 📘 Event Simulator 개발

**구현 세부 사항**

- AWS EC2 (Amazon Linux)를 생성하여 Docker 설치
- RabbitMQ Container 4개 띄우기 (각각 다른 포트로 포워딩)
- 4개의 RabbitMQ에 주기적으로 특정 포맷의 데이터를 생성 & 전달하는 시뮬레이터 개발
- 해당 시뮬레이터도 EC2 내부에 개발
- 120개의 감시 카메라를 기준으로 1개의 RabbitMQ당 30개의 카메라 인스턴스의 데이터 담당
- RabbitMQ Container 당 초당 1건 정도로 이벤트 데이터 전달
- 예를 들어, 30초 동안 카메라당 1건씩 이벤트 데이터 생성 후 RabbitMQ Container로 전달