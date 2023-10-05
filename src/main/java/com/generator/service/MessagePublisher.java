package com.generator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
