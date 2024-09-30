package com.GASB.slack_func.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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

    @Value("${rabbitmq.O365_DELETE_QUEUE}")
    private String o365DeleteQueue;
    @Value("${rabbitmq.o365_delete_routing_key}")
    private String o365DeleteRoutingKey;

    @Value("${rabbitmq.GOOGLE_DELETE_QUEUE}")
    private String googleDriveDeleteQueue;
    @Value("${rabbitmq.google_delete_routing_key}")
    private String googleDriveDeleteRoutingKey;

    // 첫 번째 큐 설정
    @Bean
    Queue fileQueue() {
        return new Queue(queueName, true, false, false);
    }
    @Bean
    Binding fileQueueBinding(@Qualifier("fileQueue") Queue fileQueue, DirectExchange exchange) {
        return BindingBuilder.bind(fileQueue).to(exchange).with(routingKey);
    }
    // 두 번째 큐 설정
    @Bean
    Queue groupingQueue() {
        return new Queue(groupingQueueName, true, false, false);
    }
    @Bean
    Binding groupingQueueBinding(@Qualifier("groupingQueue") Queue groupingQueue, DirectExchange exchange) {
        return BindingBuilder.bind(groupingQueue).to(exchange).with(groupingRoutingKey);
    }
    // 세 번째 큐 설정
    @Bean
    Queue o365DeleteQueue() {
        return new Queue(o365DeleteQueue, true, false, false);
    }
    @Bean
    Binding o365InitQueueBinding(@Qualifier("o365DeleteQueue") Queue o365InitQueue, DirectExchange exchange) {
        return BindingBuilder.bind(o365InitQueue).to(exchange).with(o365DeleteRoutingKey);
    }

    @Bean
    Queue googleDriveDeleteQueue() {
        return new Queue(googleDriveDeleteQueue, true, false, false);
    }
    @Bean
    Binding googleDriveInitQueueBinding(@Qualifier("googleDriveDeleteQueue") Queue googleDriveInitQueue, DirectExchange exchange) {
        return BindingBuilder.bind(googleDriveInitQueue).to(exchange).with(googleDriveDeleteRoutingKey);
    }

    // 교환기(Exchange) 설정
    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    // 직렬화 설정
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
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

    // RabbitTemplate 설정 (O365 라우팅 키 사용)
    @Bean
    public RabbitTemplate o365deleteTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(o365DeleteRoutingKey);
        return rabbitTemplate;
    }

    // RabbitTemplate 설정 (GoogleDrive 라우팅 키 사용)
    @Bean
    public RabbitTemplate googleDriveDeleteTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(googleDriveDeleteRoutingKey);
        return rabbitTemplate;
    }
}
