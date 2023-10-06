package com.generator.service;

import com.generator.dto.TripwireDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MessageReceiver {
    @RabbitListener(queues = "q.one", containerFactory = "listener1")
    public void receive1(TripwireDto message) {
        log.info("[Message Body] : {}", message.toString());
    }

    @RabbitListener(queues = "q.two", containerFactory = "listener2")
    public void receive2(TripwireDto message) {
        log.info("[Message Body] : {}", message.toString());
    }

    @RabbitListener(queues = "q.three", containerFactory = "listener3")
    public void receive3(TripwireDto message) {
        log.info("[Message Body] : {}", message.toString());
    }

    @RabbitListener(queues = "q.four", containerFactory = "listener4")
    public void receive4(TripwireDto message) {
        log.info("[Message Body] : {}", message.toString());
    }
}
