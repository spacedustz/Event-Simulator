## 📘 RabbitMQ Console Setting

각 RabbitMQ Container마다 동일하게 설정하되, MQTT Producer쪽에서 Topic Message는 다르게 설정합니다.

- Exchange 명 : ex.one, ex.two ...
- Topic 명 : one, two, ...

<br>

Exchange와 Queue를 바인딩 하기 전, Default Exchange인 **amq.topic**과 만든 Exchange를 바인딩 합니다.

<br>

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-rabbit.png)

<br>

> 📌 **Exchange 생성**

- Exchange를 생성합니다. (Arguments 옵션은 필요에 따라 파라미터 설정)
- Default Exchange인 amq.topic과 생성한 Exchange를 바인딩해서 amq.topic -> 생성한 Exchange로 데이터가 가게 합니다.

<br>

> 📌 **Queue 생성**

- Queue를 생성합니다. (Arguments 옵션은 필요에 따라 파라미터 설정)
- 위에서 만든 Exchange와 Routing Key(Topic String)를 이용하여 Exchange와 Binding 해줍니다.