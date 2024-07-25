package com.GASB.slack_func.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    // 큐 설정
    // receiver: 큐 설정 해야됨
    // sender: 큐 설정이 필요 없음
    @Bean
    Queue queue() {
        // durable: 큐의 메타데이터와 메시지의 내구성을 보장
        // exclusive: true일 경우 큐는 오직 하나의 연결에서만 사용되며, 이 연결이 종료되면 큐도 자동으로 삭제
        // autoDelete: true일 경우 큐에 대한 모든 소비자가 없을 때 자동으로 큐가 삭제
        return new Queue(queueName, true, false, false);
    }

    // 교환기(Exchange) 설정
    // sender, receiver 둘 다 설정 필요
    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    // 바인딩 설정
    // sender, receiver 둘 다 큐와 exchange를 바인딩하고, routing key를 설정하여 메시지가 올바른 큐로 전달되도록 합니다.
    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    // RabbitTemplate 설정
    // sender: 메시지를 전송하기 위해 RabbitTemplate을 설정
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(routingKey);
        return rabbitTemplate;
    }

    // 메시지 리스너 컨테이너 설정
    // receiver: 메시지를 수신하기 위한 SimpleMessageListenerContainer를 설정. 큐에서 메시지를 가져와 처리
//    @Bean
//    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
//                                             MessageListenerAdapter listenerAdapter) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames(queueName);
//        container.setMessageListener(listenerAdapter);
//        return container;
//    }

    // 메시지 리스너 어댑터 설정
    // receiver: MessageListenerAdapter를 사용하여 수신한 메시지를 RabbitMQReceive 서비스의 특정 메서드로 전달
//    @Bean
//    MessageListenerAdapter listenerAdapter(RabbitMQReceive receiver) {
//        return new MessageListenerAdapter(receiver, "receive");
//    }
}