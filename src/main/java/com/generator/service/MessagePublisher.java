package com.generator.service;

import co.kr.dains.crowd.estimation.common.domain.svc.SvcInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generator.dto.EstimationDto;
import com.generator.dto.TripwireDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

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

        ClassPathResource tripwire = new ClassPathResource("sample/tripwire-counting.json");
        ClassPathResource estimation = new ClassPathResource("sample/crowd-estimation.json");
        List<SvcInstance> randomInstances = instanceService.getRandomInstances();

        for (SvcInstance instance : randomInstances) {

            if (instance.getInstanceName().startsWith("S")) {
                try {
                    TripwireDto message = mapper.readValue(tripwire.getInputStream(), TripwireDto.class);

                    template.convertAndSend(exchange, routingKey, message);
                    log.info("[Rabbit {}] - 데이터 전송 완료", server);
                } catch (Exception e) {
                    log.error("[데이터 전송 실패 - Tripwire] Instance ID : {}, Error : {}", instance.getInstanceId(), e.getMessage());
                }
            } else if (instance.getInstanceName().startsWith("E")) {
                try {
                    EstimationDto message = mapper.readValue(estimation.getInputStream(), EstimationDto.class);

                    template.convertAndSend(exchange, routingKey, message);
                } catch (Exception e) {
                    log.error("[데이터 전송 실패 - Estimation] Instance ID : {}, Error : {}", instance.getInstanceId(), e.getMessage());
                }
            }
        }
    }
}