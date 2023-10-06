package com.generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generator.dto.TripwireDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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