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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public ConnectionFactory connectionFactory1() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port+1);
        factory.setUsername(id);
        factory.setPassword(pw);

        return factory.getRabbitConnectionFactory();
    }

    @Bean
    public ConnectionFactory connectionFactory2() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port+2);
        factory.setUsername(id);
        factory.setPassword(pw);

        return factory.getRabbitConnectionFactory();
    }

    @Bean
    public ConnectionFactory connectionFactory3() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port+3);
        factory.setUsername(id);
        factory.setPassword(pw);

        return factory.getRabbitConnectionFactory();
    }

    @Bean
    public ConnectionFactory connectionFactory4() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(id);
        factory.setPassword(pw);

        return factory.getRabbitConnectionFactory();
    }

    // Rabbit Template 생성
    @Bean
    public RabbitTemplate rabbitTemplate1(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory1) {
        return new RabbitTemplate(connectionFactory1);
    }

    @Bean
    public RabbitTemplate rabbitTemplate2(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory2) {
        return new RabbitTemplate(connectionFactory2);
    }

    @Bean
    public RabbitTemplate rabbitTemplate3(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory3) {
        return new RabbitTemplate(connectionFactory3);
    }

    @Bean
    public RabbitTemplate rabbitTemplate4(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory4) {
        return new RabbitTemplate(connectionFactory4);
    }

    // Subscriber Listen Container
    @Bean
    SimpleRabbitListenerContainerFactory listener1(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory1) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory1);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }

    @Bean
    SimpleRabbitListenerContainerFactory listener2(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory2) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory2);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }

    @Bean
    SimpleRabbitListenerContainerFactory listener3(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory3) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory3);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }

    @Bean
    SimpleRabbitListenerContainerFactory listener4(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory4) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory4);
        factory.setMessageConverter(converter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }
}
