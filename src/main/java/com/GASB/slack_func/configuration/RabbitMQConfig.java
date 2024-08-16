package com.GASB.slack_func.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey}")
    private String routingKey;

    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.GROUPING_QUEUE}")
    private String groupingQueueName;

    @Value("${rabbitmq.GROUPING_ROUTING_KEY}")
    private String groupingRoutingKey;

    // 첫 번째 큐 설정
    @Bean
    Queue fileQueue() {
        return new Queue(queueName, true, false, false);
    }

    // 두 번째 큐 설정
    @Bean
    Queue groupingQueue() {
        return new Queue(groupingQueueName, true, false, false);
    }

    // 교환기(Exchange) 설정
    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    // 첫 번째 바인딩 설정
    @Bean
    Binding fileQueueBinding(@Qualifier("fileQueue") Queue fileQueue, DirectExchange exchange) {
        return BindingBuilder.bind(fileQueue).to(exchange).with(routingKey);
    }

    @Bean
    Binding groupingQueueBinding(@Qualifier("groupingQueue") Queue groupingQueue, DirectExchange exchange) {
        return BindingBuilder.bind(groupingQueue).to(exchange).with(groupingRoutingKey);
    }
    // RabbitTemplate 설정 (기본 라우팅 키 사용)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(routingKey);
        return rabbitTemplate;
    }

    // RabbitTemplate 설정 (그룹 라우팅 키 사용)
    @Bean
    public RabbitTemplate groupingRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(groupingRoutingKey);
        return rabbitTemplate;
    }
}
