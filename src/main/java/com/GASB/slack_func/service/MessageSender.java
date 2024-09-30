package com.GASB.slack_func.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitTemplate groupingRabbitTemplate;

    private final RabbitTemplate o365deleteTemplate;
    private final RabbitTemplate googleDriveDeleteTemplate;

    @Autowired
    public MessageSender(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate,
                         @Qualifier("groupingRabbitTemplate") RabbitTemplate groupingRabbitTemplate,
                         @Qualifier("o365deleteTemplate") RabbitTemplate o365deleteTemplate,
                         @Qualifier("googleDriveDeleteTemplate") RabbitTemplate googleDriveDeleteTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.groupingRabbitTemplate = groupingRabbitTemplate;
        this.o365deleteTemplate = o365deleteTemplate;
        this.googleDriveDeleteTemplate = googleDriveDeleteTemplate;
    }

    public void sendMessage(Long message) {
        rabbitTemplate.convertAndSend(message);
        log.info("Sent message to default queue: " + message);
    }

    public void sendGroupingMessage(Long message) {
        groupingRabbitTemplate.convertAndSend(message);
        log.info("Sent message to grouping queue: " + message);
    }

    public void sendMessageToQueue(Long message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
        log.info("Sent message to queue " + queueName + ": " + message);
    }

    public void sendO365DeleteMessage(List<Map<String,String>> message) {
        o365deleteTemplate.convertAndSend(message);
        log.info("Sent message to o365 delete queue: " + message);
    }

    public void sendGoogleDriveDeleteMessage(List<Map<String,String>> message) {
        googleDriveDeleteTemplate.convertAndSend(message);
        log.info("Sent message to google drive delete queue: " + message);
    }
}
