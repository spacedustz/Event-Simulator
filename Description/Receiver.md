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

    @RabbitListener(queues = "q.one", containerFactory = "listener1")
    public void receive1(TripwireDto message) {
        log.info("[Message Body] : {}", message.toString());
    }

//    @RabbitListener(queues = "q.two", containerFactory = "listener2")  
//    public void receive2(TripwireDto message) {  
//        log.info("[Message Body] : {}", message.toString());  
//    }  
//  
//    @RabbitListener(queues = "q.three", containerFactory = "listener3")  
//    public void receive3(TripwireDto message) {  
//        log.info("[Message Body] : {}", message.toString());  
//    }  
//  
//    @RabbitListener(queues = "q.four", containerFactory = "listener4")  
//    public void receive4(TripwireDto message) {  
//        log.info("[Message Body] : {}", message.toString());  
//    }  
}
```

<br>

위 코드의 @RabbitListener의 옵션으로 containerFactory를 지정해 준 이유는,

앞서 작성했던 RabbitConfig에서 각 RabbitMQ 마다 Container Factory에게 @Qualifier를 설정해 줬었습니다.

그때의 Bean 이름을 넣어주면 각 RabbitMQ의 Queue를 찾아서 데이터를 Receive 하게 됩니다.

<br>

**예: 1번 Rabbit의 Container Factory**

```java
@Bean
@Primary
@Qualifier("listener1")  
SimpleRabbitListenerContainerFactory listener1(org.springframework.amqp.rabbit.connection.ConnectionFactory factory1) {
final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(factory1);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
        }
```

<br>

**이제 시뮬레이터 (Spring Boot)를 실행 시켜 보겠습니다.**

1초마다 4개의 RabbitMQ에 데이터를 잘 보내고 있고, 그 중 1번 RabbitMQ의 Queue에서만 데이터를 빼와서 출력합니다.

값을 확인해보면, 원본 Json 데이터의 값이 모두 잘 들어가 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-receive2.png)

<br>

위 Receiver에서 1,2,3,4번의 Rabbit중 1번의 Rabbit Queue 에서만 데이터를 받아, 1번의 데이터만 가져오게 했습니다.

사진을 보면 1번 RabbitMQ의 Queue에 쌓여있던 데이터만 빠져나간 걸 볼 수 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-receive.png)