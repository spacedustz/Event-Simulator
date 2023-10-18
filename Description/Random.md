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