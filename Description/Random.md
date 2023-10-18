## 📘 Event Simulator 개발

**구현 세부 사항**

- AWS EC2 (Amazon Linux)를 생성하여 Docker 설치
- RabbitMQ Container 4개 띄우기 (각각 다른 포트로 포워딩)
- 4개의 RabbitMQ에 주기적으로 특정 포맷의 데이터를 생성 & 전달하는 시뮬레이터 개발
- 해당 시뮬레이터도 EC2 내부에 개발
- 120개의 감시 카메라를 기준으로 1개의 RabbitMQ당 30개의 카메라 인스턴스의 데이터 담당
- RabbitMQ Container 당 초당 1건 정도로 이벤트 데이터 전달
- 예를 들어, 30초 동안 카메라당 1건씩 이벤트 데이터 생성 후 RabbitMQ Container로 전달

---

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
```

---

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

---

## 📘 Application.yml

RabbitMQ 4대의 포트를 다 적기보다 Config에서 단순히 포트에 +1, +2를 할거고 host/id/pw는 동일하기 때문에,

RabbitMQ 설정은 1개만 작성해줍니다.

```yaml
server:  
  servlet:  
    encoding:  
      charset: UTF-8  
      force-response: true  
  port: 8100  
  
spring:  
  
  # RabbitMQ 설정
  rabbitmq:  
    host: 1.1.1.1  
    port: 5672  
    username: guest  
    password: guest  
  
  # H2 설정
  h2:  
    console:  
      enabled: true  
      path: /h2  
  datasource:  
    url: jdbc:h2:mem:test  
    username: root  
    password: 1234  
  
  # JPA 설정
  jpa:  
    open-in-view: false  
    hibernate:  
      ddl-auto: create  
    show-sql: false  
    properties:  
      hibernate:  
        format_sql: true  
  
# Logging  
logging:  
  pattern:  
    dateformat: "yyyy-MM-dd HH:mm:ss"  
  level:  
    org:  
      hibernate: info
```

---

## 📘 RabbitConfig

@Value를 이용해서 application.yml에 있는 RabbitMQ 의 정보를 변수에 저장합니다.

그리고 4개의 RabbitMQ `ConnectionFactory`, `RabbitTemplate`, `SimpleRabbitListenerContainerFactory` Bean을 만들어 줍니다.

Rabbit 1, 2, 3, 4의 포트는 **5672, 5673, 5674, 5675**로 설정하였습니다.

```java
@Slf4j  
@Configuration  
@RequiredArgsConstructor  
public class RabbitConfig {  
  
    @Value("${spring.rabbitmq.host}")  
    private String host;  
  
    @Value("${spring.rabbitmq.port}")  
    private int port;  
  
    @Value("${spring.rabbitmq.username}")  
    private String id;  
  
    @Value("${spring.rabbitmq.password}")  
    private String pw;  
  
    // Message Converter Bean 주입  
    @Bean  
    MessageConverter converter() { return new Jackson2JsonMessageConverter(); }  
  
    @Bean  
    @Primary    @Qualifier("factory1")  
    public org.springframework.amqp.rabbit.connection.ConnectionFactory factory1() {  
        CachingConnectionFactory factory = new CachingConnectionFactory();  
        factory.setHost(host);  
        factory.setPort(port);  
        factory.setUsername(id);  
        factory.setPassword(pw);  
  
        log.info("[Bean] Connection Factory 1 연결 성공 - Port : {}", factory.getPort());  
        return factory;  
    }  
  
    @Bean  
    @Qualifier("factory2")  
    public org.springframework.amqp.rabbit.connection.ConnectionFactory factory2() {  
        CachingConnectionFactory factory = new CachingConnectionFactory();  
        factory.setHost(host);  
        factory.setPort(port+1);  
        factory.setUsername(id);  
        factory.setPassword(pw);  
  
        log.info("[Bean] Connection Factory 2 연결 성공 - Port : {}", factory.getPort());  
        return factory;  
    }  
  
    @Bean  
    @Qualifier("factory3")  
    public org.springframework.amqp.rabbit.connection.ConnectionFactory factory3() {  
        CachingConnectionFactory factory = new CachingConnectionFactory();  
        factory.setHost(host);  
        factory.setPort(port+2);  
        factory.setUsername(id);  
        factory.setPassword(pw);  
  
        log.info("[Bean] Connection Factory 3 연결 성공 - Port : {}", factory.getPort());  
        return factory;  
    }  
  
    @Bean  
    @Qualifier("factory4")  
    public org.springframework.amqp.rabbit.connection.ConnectionFactory factory4() {  
        CachingConnectionFactory factory = new CachingConnectionFactory();  
        factory.setHost(host);  
        factory.setPort(port+3);  
        factory.setUsername(id);  
        factory.setPassword(pw);  
  
        log.info("[Bean] Connection Factory 4 연결 성공 - Port : {}", factory.getPort());  
        return factory;  
    }  
  
    // Rabbit Template 생성  
    @Bean  
    @Primary    @Qualifier("template1")  
    public RabbitTemplate template1() {  
        RabbitTemplate template = new RabbitTemplate(factory1());  
        template.setMessageConverter(converter());  
  
        log.info("[Bean] Template 1 연결 성공");  
        return template;  
    }  
  
    @Bean  
    @Qualifier("template2")  
    public RabbitTemplate template2() {  
        RabbitTemplate template = new RabbitTemplate(factory2());  
        template.setMessageConverter(converter());  
  
        log.info("[Bean] Template 2 연결 성공");  
        return template;  
    }  
  
    @Bean  
    @Qualifier("template3")  
    public RabbitTemplate template3() {  
        RabbitTemplate template = new RabbitTemplate(factory3());  
        template.setMessageConverter(converter());  
  
        log.info("[Bean] Template 3 연결 성공");  
        return template;  
    }  
  
    @Bean  
    @Qualifier("template4")  
    public RabbitTemplate template4() {  
        RabbitTemplate template = new RabbitTemplate(factory4());  
        template.setMessageConverter(converter());  
  
        log.info("[Bean] Template 4 연결 성공");  
        return template;  
    }  
  
    // Subscriber Listen Container  
    @Bean  
    @Primary    @Qualifier("listener1")  
    SimpleRabbitListenerContainerFactory listener1() {  
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();  
        factory.setConnectionFactory(factory1());  
        factory.setMessageConverter(converter());  
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);  
  
        return factory;  
    }  
  
    @Bean  
    @Qualifier("listener2")  
    SimpleRabbitListenerContainerFactory listener2() {  
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();  
        factory.setConnectionFactory(factory2());  
        factory.setMessageConverter(converter());  
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);  
  
        return factory;  
    }  
  
    @Bean  
    @Qualifier("listener3")  
    SimpleRabbitListenerContainerFactory listener3() {  
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();  
        factory.setConnectionFactory(factory3());  
        factory.setMessageConverter(converter());  
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);  
  
        return factory;  
    }  
  
    @Bean  
    @Qualifier("listener4")  
    SimpleRabbitListenerContainerFactory listener4() {  
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();  
        factory.setConnectionFactory(factory4());  
        factory.setMessageConverter(converter());  
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);  
  
        return factory;  
    }  
}
```

---

## 📘 MessagePublisher - Test Data

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

<br>

**Multi-Threading**

```java
private final ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(30);  
  
@Scheduled(fixedDelay = 1000) // 1초마다 실행  
public void simulate1() throws InterruptedException {  
    // 스레드 풀 30개로 설정  
  
    // 1개의 RabbitMQ당 1개의 스레드를 만들어 1개의 스레드당 1초에 메시지 30개를 보냅니다.  
    // 즉, 스레드당 1초에 메시지를 30개씩 만들어 각각의 RabbitMQ로 보냅니다.  
    while (true) {  
        for (int i = 0; i < 30; i++) {  
            executorService.submit(() -> {  
                sendData(template1, "ex.one", "one");  
            });  
            executorService.submit(() -> {  
                sendData(template2, "ex.two", "two");  
            });  
            executorService.submit(() -> {  
                sendData(template3, "ex.three", "three");  
            });  
            executorService.submit(() -> {  
                sendData(template4, "ex.four", "four");  
            });  
        }  
  
        // 1초 대기  
        Thread.sleep(1000);  
  
        // "q"를 입력하면 데이터 전송 중단  
        try {  
            if (System.in.read() == 'q') {  
                break;  
            }  
        } catch (IOException e) {  
            throw new RuntimeException(e);  
        }  
    }  
  
    executorService.shutdown();  
    executorService.awaitTermination(1, TimeUnit.HOURS);  
}
```

---

## 📘 MessagePublisher - Original Data

Message를 Publish할때 Message를 단순한 String 값인 "Test Message"라는 문자열을 RabbitMQ에 보냈었습니다.

이번엔 Test Message 대신 이벤트 데이터(Json)의 구조를 계층화한 DTO를 작성합니다.

이유는 원본 Json 내부의 데이터 값을 DTO로 역직렬화 해서 Java 객체로 변환하기 위함 입니다.

<br>

> 📌 **Tripwire-Crossing.json**

아래는 **원본 Json 데이터** 입니다.

```json
{  
  "events": [  
    {  
      "extra": {  
        "bbox": {  
          "height": 0.1276407390832901,  
          "width": 0.02904696948826313,  
          "x": 0.6992628574371338,  
          "y": 0.43387532234191895  
        },  
        "class": "Person",  
        "count": 2,  
        "crossing_direction": "down",  
        "external_id": "4e9ea30a-86a1-4c5d-a485-17a598f83c3b",  
        "track_id": "PersonTracker_42",  
        "tripwire": {  
          "check_anchor_point": "bottom_center",  
          "color": [  
            0,  
            0,  
            1,  
            1  
          ],  
          "cooldown_bandwidth": 0.07000000029802322,  
          "cross_bandwidth": 0.029999999329447746,  
          "crowding_min_count": 4,  
          "detect_animals": true,  
          "detect_people": true,  
          "detect_unknowns": false,  
          "detect_vehicles": true,  
          "direction": "Both",  
          "groupby": "tripwire_counting",  
          "id": "91c21599-1d71-4455-a1d4-0fd2e9d70cf6",  
          "ignore_stationary_objects": true,  
          "inference_strategy": "full_frame",  
          "name": "Wire-Test",  
          "restrict_object_max_size": false,  
          "restrict_object_min_size": false,  
          "restrict_person_attributes": false,  
          "restrict_vehicle_type": false,  
          "timestamp": 1694047141888.0,  
          "trigger_crossing": true,  
          "trigger_crowding": false,  
          "trigger_loitering": false,  
          "trigger_on_enter": false,  
          "trigger_on_exit": false,  
          "vertices": [  
            {  
              "x": 0.7106109261512756,  
              "y": 0.5444126129150391  
            },  
            {  
              "x": 0.9437298774719238,  
              "y": 0.6217765212059021  
            }  
          ]  
        }  
      },  
      "id": "057baaa0-94c4-432f-8051-a5615f34b980",  
      "label": "Tripwire crossed",  
      "type": "tripwire_crossing"  
    }  
  ],  
  "frame_id": 739,  
  "frame_time": 24.633333333333333,  
  "system_date": "Thu Sep 7 09:42:26 2023",  
  "system_timestamp": 1694047346  
}
```

<br>

> 📌 **TripwireDto**

위 Json 데이터의 계층 구조에 맞게 내부 Static Class로 정의해줍니다.

Extra 클래스의 wireClass 필드는 원본 데이터에서 키 값이 `class`인데, 자바는 예약어를 변수명으로 선언을 하지 못하기 때문에,

@JsonProperty()에 원본 키 값을 써주고 Java에서의 필드명은 임의로 wireClass라고 지정 하였습니다.

@ToString은 이제 곧 만들 MessageReceiver에서 DTO에 Json의 값들이 전부 잘 들어갔는지 확인하기 위해 달아주었습니다.

```java
@Getter  
@ToString  
public class TripwireDto {  
    private List<TripwireDto.Event> events;  
    private int frame_id;  
    private double frame_time;  
    private String system_date;  
    private long system_timestamp;  
  
    @Getter  
    @ToString    public static class Event {  
        private TripwireDto.Event.Extra extra;  
        private String id;  
        private String label;  
        private String type;  
  
        @Getter  
        @ToString        public static class Extra {  
            private TripwireDto.Event.Extra.Bbox bbox;  
            @JsonProperty("class")  
            private String wireClass;  
            private int count;  
            private String crossing_direction;  
            private String external_id;  
            private String track_id;  
            private TripwireDto.Event.Extra.Tripwire tripwire;  
  
            @Getter  
            @ToString            public static class Bbox {  
                private double height;  
                private double width;  
                private double x;  
                private double y;  
            }  
  
            @Getter  
            @ToString            public static class Tripwire {  
                private String check_anchor_point;  
                private List<Double> color;  
                private double cooldown_bandwidth;  
                private double cross_bandwidth;  
                private int crowding_min_count;  
                private boolean detect_animals;  
                private boolean detect_people;  
                private boolean detect_unknowns;  
                private boolean detect_vehicles;  
                private String direction;  
                private String groupby;  
                private String id;  
                private boolean ignore_stationary_objects;  
                private String inference_strategy;  
                private String name;  
                private boolean restrict_object_max_size;  
                private boolean restrict_object_min_size;  
                private boolean restrict_person_attributes;  
                private boolean restrict_vehicle_type;  
                private double timestamp;  
                private boolean trigger_crossing;  
                private boolean trigger_crowding;  
                private boolean trigger_loitering;  
                private boolean trigger_on_enter;  
                private boolean trigger_on_exit;  
                private List<TripwireDto.Event.Extra.Tripwire.Vertices> vertices;  
  
                @Getter  
                @ToString                public static class Vertices {  
                    private double x;  
                    private double y;  
                }  
            }  
        }  
    }  
}
```

<br>

> 📌 **MessagePublisher**

이제 원본 Json을 담을 틀인 TripwireDto를 작성했으니 테스트 메시지인 "Test Message" 대신 Tripwire 클래스를 내보내 봅시다.

- tripwire 객체를 static으로 선언해, 메시지를 보낼때 마다 반복되는 객체 생성을 피합니다.
- 원본 데이터인 Json은 프로젝트 내부 resources/sample 디렉터리 내부에 있으므로 ClassPathResource를 이용해 가져와 줍니다.
- 원본 뎅터를 가져온 후 ObjectMapper를 이용해 Json -> Java 객체로 역직렬화 해서 Rabbit Template에 Publish 해줍니다.

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
  
    @Autowired  
    private ObjectMapper mapper;  
  
    private static final TripwireDto tripwire = new TripwireDto(); // 샘플로 보낼 데이터  
  
  
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
  
        ClassPathResource resource = new ClassPathResource("sample/tripwire-counting.json");  
  
        try {  
            TripwireDto message = mapper.readValue(resource.getInputStream(), tripwire.getClass());  
  
            template.convertAndSend(exchange, routingKey, message);  
            log.info("[Rabbit {}] - 데이터 전송 완료", server);  
        } catch (Exception e) {  
            log.error("[Simulator Error] : {}, Cause : {}", e.getMessage(), e.getCause());  
        }  
    }  
}
```

---

## 📘 MessageReceiver

메시지를 RabbitMQ 4대 각각의 Queue에 1초마다 데이터를 Publish해서 Queue에 쌓아 놓았습니다.

이 Queue에 쌓인 데이터를 RabbitMQ 1대에서만 테스트로 가져와서 데이터의 `system_date`를 가져왔습니다.

만약 데이터가 잘못됬다면 Publish 단계부터 Exception이 나므로, 로그에 Message의 `system_date`가 출력된다면 잘 된겁니다.

<br>

1초마다 4개의 RabbitMQ에 데이터를 잘 보내고 있고, 그 중 1번 RabbitMQ의 Queue에서만 데이터를 빼와서 출력합니다.

**(메시지가 길기 때문에 확인 용으로 잠시 2,3,4번은 주석 처리)**

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
SimpleRabbitListenerContainerFactory listener1() {  
    final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();  
    factory.setConnectionFactory(factory1());  
    factory.setMessageConverter(converter());  
    factory.setAcknowledgeMode(AcknowledgeMode.AUTO);  
  
    return factory;  
}
```

<br>

**이제 시뮬레이터 (Spring Boot)를 실행 시켜 보겠습니다.**

값을 확인해보면, 원본 Json 데이터의 값이 모두 잘 들어가 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-receive2.png)

<br>

위 Receiver에서 1,2,3,4번의 Rabbit중 1번의 Rabbit Queue 에서만 데이터를 받아, 1번의 데이터만 가져오게 했습니다.

사진을 보면 1번 RabbitMQ의 Queue에 쌓여있던 데이터만 빠져나간 걸 볼 수 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-receive.png)

---
## 📘 Publish Random Instance/Values with Base64 Encoding Image Data

카메라 1개당 2개의 인스턴스를 가지며, 총 120개의 카메라가 있습니다.

그럼 총 120개의 카메라, 240개의 인스턴스가 있는데, 이걸 Map으로 그룹핑 해줍니다.

그 후, 카메라 ID당 2개의 인스턴스가 들어있는 Map을 돌며, 랜덤 인스턴스를 골라 새로운 리스트로 넣어줍니다.

<br>

> 📌 **InstanceService**

```java  
@Slf4j  
@Service  
@RequiredArgsConstructor  
public class InstanceService {  
    private final SvcInstanceRepository svcInstanceRepository;  
  
    public List<SvcInstance> getRandomInstances() {  
        List<SvcInstance> selectedInstances = new ArrayList<>();  
        List<SvcInstance> instances = svcInstanceRepository.findAll();  
  
        Map<Integer, List<SvcInstance>> groupedInstances = instances.stream().collect(Collectors.groupingBy(it -> it.getSvcCamera().getCameraId()));  
  
        groupedInstances.values().forEach(it -> {  
            if (it.size() >= 2) {  
                int randomIndex = new Random().nextInt(it.size());  
                selectedInstances.add(it.get(randomIndex));  
            }  
        });  
          
        return selectedInstances;  
    }  
}
```

<br>

> 📌 **MessagePublisher**

그 후, Publisher에서 InstanceService에서 랜덤 인스턴스가 담긴 리스트를 가져와, 30개씩 분리 후

각 RabbitMQ에 30개 카메라의 데이터를 1초마다 보냅니다.

이때, 각 데이터의 Count 수를 랜덤하게 보내기 위해 각 DTO의 count 값을 0~50까지로 설정하였습니다.

```java
@Slf4j  
@Service  
@RequiredArgsConstructor  
public class MessagePublisher {  
    @Autowired @Qualifier("template1")  
    private final RabbitTemplate template1;  
    @Autowired @Qualifier("template2")  
    private final RabbitTemplate template2;  
    @Autowired @Qualifier("template3")  
    private final RabbitTemplate template3;  
    @Autowired @Qualifier("template4")  
    private final RabbitTemplate template4;  
    private final InstanceService instanceService;  
    private final ObjectMapper mapper;  
  
    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate1() {  
        List<SvcInstance> randomInstances = getRandomInstances();  
        List<SvcInstance> instances = randomInstances.subList(0, 30);  
        sendData(instances, template1, "ex.one", "one");  
    }  
  
    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate2() {  
        List<SvcInstance> randomInstances = getRandomInstances();  
        List<SvcInstance> instances = randomInstances.subList(30, 60);  
        sendData(instances, template2, "ex.two", "two");  
    }  
  
    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate3() {  
        List<SvcInstance> randomInstances = getRandomInstances();  
        List<SvcInstance> instances = randomInstances.subList(60, 90);  
        sendData(instances, template3, "ex.three", "three");  
    }  
  
    @Scheduled(fixedDelay = 1000) // 1초마다 실행  
    public void simulate4() {  
        List<SvcInstance> randomInstances = getRandomInstances();  
        List<SvcInstance> instances = randomInstances.subList(90, 120);  
        sendData(instances, template4, "ex.four", "four");  
    }  
  
    public void sendData(List<SvcInstance> list, RabbitTemplate template, String exchange, String routingKey) {  
        String server = switch (template.getConnectionFactory().getPort()) {  
            case 5672 -> "1";  
            case 5673 -> "2";  
            case 5674 -> "3";  
            case 5675 -> "4";  
            default -> "";  
        };  
  
        ClassPathResource tripwire = new ClassPathResource("sample/tripwire-counting.json");  
        ClassPathResource estimation = new ClassPathResource("sample/crowd-estimation.json");  
        ClassPathResource areaCrowd = new ClassPathResource("sample/area-crowding-image.json");  
  
        for (SvcInstance instance : list) {  
  
            if (instance.getInstanceName().startsWith("S")) {  
                try {  
//                    TripwireDto message = mapper.readValue(tripwire.getInputStream(), TripwireDto.class);  
                    AreaCrowdImageDto message = mapper.readValue(areaCrowd.getInputStream(), AreaCrowdImageDto.class);  
                    message.getEvents().get(0).getExtra().setCurrentEntries(new Random().nextInt(51));  
                    template.convertAndSend(exchange, routingKey, message);  
  
                    log.info("[Rabbit {}] - Area Crowd 데이터 전송 완료", server);  
                } catch (Exception e) {  
                    log.error("[데이터 전송 실패 - AreaCrowd] Instance ID : {}, Error : {}", instance.getInstanceId(), e.getMessage());  
                }  
            } else if (instance.getInstanceName().startsWith("E")) {  
                try {  
                    EstimationDto message = mapper.readValue(estimation.getInputStream(), EstimationDto.class);  
                    message.setCount(new Random().nextInt(51));  
                    template.convertAndSend(exchange, routingKey, message);  
  
                    log.info("[Rabbit {}] - Estimation 데이터 전송 완료", server);  
                } catch (Exception e) {  
                    log.error("[데이터 전송 실패 - Estimation] Instance ID : {}, Error : {}", instance.getInstanceId(), e.getMessage());  
                }  
            }  
        }  
    }  
  
    @Cacheable  
    public List<SvcInstance> getRandomInstances() {  
        return instanceService.getRandomInstances();  
    }  
}
```

<br>

> 📌 **결과**

RabbitMQ 4개 중 1개만 들어가서 데이터를 가져오면 count, current_entries 값이 랜덤으로 설정된 걸 볼 수 있습니다.

![img](https://raw.githubusercontent.com/spacedustz/Obsidian-Image-Server/main/img2/simulator-random.png)