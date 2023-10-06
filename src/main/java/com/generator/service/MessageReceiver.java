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

    @RabbitListener(queues = "q.one")
    public void receive1(TripwireDto message) {
      log.info("Message : {}", message.getSystem_date());
    }

//    @RabbitListener(queues = "q.two")
//    public void receive2(Object message) {
//        log.info("Message : {}", message);
//    }
//
//    @RabbitListener(queues = "q.three")
//    public void receive3(Object message) {
//        log.info("Message : {}", message);
//    }
//
//    @RabbitListener(queues = "q.four")
//    public void receive4(Object message) {
//        log.info("Message : {}", message);
//    }
}
