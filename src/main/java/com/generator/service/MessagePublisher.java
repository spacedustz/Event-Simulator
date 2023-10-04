package com.generator.service;

import com.generator.dto.MessageDto;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MessagePublisher {

    private static final String QUEUE_NAME = "test_queue";
    private static final String[] EXCHANGE_NAMES = {"exchange1", "exchange2", "exchange3", "exchange4"};
    private final List<MessageDto> messages;
    private final RabbitTemplate template;

    @Scheduled(fixedRate = 1000)
    public void publishData1(int instanceNumber) {
        MessageDto dto = messages.get(instanceNumber);
        dto.setInstanceNumber(dto.getInstanceNumber() + 1);
        template.convertAndSend("t1", dto);
    }

    @Scheduled(fixedRate = 1000)
    public void publishData2(int instanceNumber) {
        MessageDto dto = messages.get(instanceNumber);
        dto.setInstanceNumber(dto.getInstanceNumber() + 1);
        template.convertAndSend("t2", dto);
    }

    @Scheduled(fixedRate = 1000)
    public void publishData3(int instanceNumber) {
        MessageDto dto = messages.get(instanceNumber);
        dto.setInstanceNumber(dto.getInstanceNumber() + 1);
        template.convertAndSend("t3", dto);
    }

    @Scheduled(fixedRate = 1000)
    public void publishData4(int instanceNumber) {
        MessageDto dto = messages.get(instanceNumber);
        dto.setInstanceNumber(dto.getInstanceNumber() + 1);
        template.convertAndSend("t4", dto);
    }

    private static class ProducerTask implements Runnable {

        private final int index;
        private final MessageDto[] messages;
        private final int[] messageCounts;

        public ProducerTask(int index, MessageDto[] messages, int[] messageCounts) {
            this.index = index;
            this.messages = messages;
            this.messageCounts = messageCounts;
        }

        @Override
        public void run() {
            // RabbitMQ 연결 설정
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");

            try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
                // Exchange 설정
                for (String exchangeName : EXCHANGE_NAMES) {
                    channel.exchangeDeclare(exchangeName, "direct");


                    // 메시지 발송
                    for (int i = 0; i < messageCounts[index]; i++) {
                        MessageDto message = messages[index * 30 + i];
                        message.setTestNumber(String.valueOf(i + 1));

                        channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // RabbitMQ 연결 설정
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        // 메시지 생성
        MessageDto[] messages = new MessageDto[120];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = new MessageDto(i + 1);
        }

        // 메시지 분배
        int[] messageCounts = new int[4];
        for (int i = 0; i < messages.length; i++) {
            int index = i % 4;
            messageCounts[index]++;
        }

        // 메시지 발송
        for (int i = 0; i < 4; i++) {
            executorService.execute(new ProducerTask(i, messages, messageCounts));
        }

        executorService.shutdown();
    }
}
