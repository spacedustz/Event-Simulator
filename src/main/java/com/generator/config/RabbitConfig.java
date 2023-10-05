package com.generator.config;

import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    @Primary
    @Qualifier("factory1")
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
    @Primary
    @Qualifier("template1")
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
    @Primary
    @Qualifier("listener1")
    SimpleRabbitListenerContainerFactory listener1(org.springframework.amqp.rabbit.connection.ConnectionFactory factory1) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(factory1);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }

    @Bean
    @Qualifier("listener2")
    SimpleRabbitListenerContainerFactory listener2(org.springframework.amqp.rabbit.connection.ConnectionFactory factory2) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(factory2);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }

    @Bean
    @Qualifier("listener3")
    SimpleRabbitListenerContainerFactory listener3(org.springframework.amqp.rabbit.connection.ConnectionFactory factory3) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(factory3);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }

    @Bean
    @Qualifier("listener4")
    SimpleRabbitListenerContainerFactory listener4(org.springframework.amqp.rabbit.connection.ConnectionFactory factory4) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(factory4);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }
}
