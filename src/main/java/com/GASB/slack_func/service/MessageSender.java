package com.GASB.slack_func.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitTemplate groupingRabbitTemplate;

    @Autowired
    public MessageSender(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate,
                         @Qualifier("groupingRabbitTemplate") RabbitTemplate groupingRabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.groupingRabbitTemplate = groupingRabbitTemplate;
    }

    public void sendMessage(Long message) {
        rabbitTemplate.convertAndSend(message);
        System.out.println("Sent message to default queue: " + message);
    }

    public void sendGroupingMessage(Long message) {
        groupingRabbitTemplate.convertAndSend(message);
        System.out.println("Sent message to grouping queue: " + message);
    }

    public void sendMessageToQueue(Long message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("Sent message to queue " + queueName + ": " + message);
    }
}
