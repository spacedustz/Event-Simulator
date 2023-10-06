## 📘 MessageReceiver

메시지를 RabbitMQ 4대 각각의 Queue에 1초마다 데이터를 Publish해서 Queue에 쌓아 놓았습니다.

이 Queue에 쌓인 데이터를 RabbitMQ 1대에서만 테스트로 가져와서 데이터의 `system_date`를 가져왔습니다.

만약 데이터가 잘못됬다면 Publish 단계부터 Exception이 나므로, 로그에 Message의 `system_date`가 출력된다면 잘 된겁니다.

```java
@Slf4j  
@Service  
@Transactional  
@RequiredArgsConstructor  
public class MessageReceiver {  
  
    @RabbitListener(queues = "q.one")  
    public void receive1(TripwireDto message) {  
      log.info("Message : {}", message.getSystem_date());  
    }
}
```

<br>

**이제 시뮬레이터 (Spring Boot)를 실행 시켜 보겠습니다.**

1초마다 4개의 RabbitMQ에 데이터를 잘 보내고 있고,

그 중 1번 RabbitMQ의 Queue에서만 데이터를 빼와, 그 데이터의 `system_date`를 잘 출력하고 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-receive2.png)

<br>

위 Receiver에서 1,2,3,4번의 Rabbit중 1번의 Rabbit Queue 에서만 데이터를 받아, 그 데이터의 System Date만 가져오게 했습니다.

사진을 보면 1번 RabbitMQ의 Queue에 쌓여있던 데이터만 빠져나간 걸 볼 수 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-receive.png)