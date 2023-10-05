## 📘 MessagePublisher

@Qualifyer를 이용해 각각의 ConnectionFactory를 주입받은 각각의 RabbitTemlate를 가져옵니다.

그리고 `sendData()` 함수를 만들어 들어오는 Connection Factory의 포트에 따라 어떤 Rabbit에 메시지가 들어간지 로깅해줍니다.

이 후, @Scheduled를 이용한 함수 4개를 만들어 1초마다 각각의 RabbitMQ에 Exchange, Routing Key, Message를 넣고 Publish 해줍니다.

```java
@Slf4j
@Service
public class MessagePublisher {
    @Autowired
    @Qualifier("template1")
    private RabbitTemplate template1;

    @Autowired
    @Qualifier("template2")
    private RabbitTemplate template2;

    @Autowired
    @Qualifier("template3")
    private RabbitTemplate template3;

    @Autowired
    @Qualifier("template4")
    private RabbitTemplate template4;

    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate1() {
        sendData(template1, "ex.one", "one");
    }

    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate2() {
        sendData(template2, "ex.two", "two");
    }

    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate3() {
        sendData(template3, "ex.three", "three");
    }

    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate4() {
        sendData(template4, "ex.four", "four");
    }

    public void sendData(RabbitTemplate template, String exchange, String routingKey) {
        String server = switch (template.getConnectionFactory().getPort()) {
            case 5672 -> "1";
            case 5673 -> "2";
            case 5674 -> "3";
            case 5675 -> "4";
            default -> "";
        };

        try {
            String message = "Test Message";
            template.convertAndSend(exchange, routingKey, message);
            log.info("[Rabbit {}] - 데이터 전송 완료", server);
        } catch (Exception e) {
            log.error("[Simulator Error] : {}", e.getMessage());
        }
    }
}
```

<br>

Spring Boot Log를 보면 1초마다 각각의  RabbitMQ에 메시지를 1개씩 Publishing 합니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-log.png)

<br>

RabbitMQ Management Console을 보면, 4대의 서로 다른 포트에 ConnectionFactory가 연결되었고,

각 Rabbit Server Queue에 동일하게 7개의 메시지가 저장되어 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-queue.png)

<br>

현재는 아주 간단하게 각 RabbitMQ에 1개당 1개의 메시지만 받게 만들었지만

이후 스레드를 늘려 1개의 RabbitMQ당 30개의 카메라 인스턴스에서 1초당 각각 다른 스레드에서 30개의 메시지를 받게 수정할 것입니다.