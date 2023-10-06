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