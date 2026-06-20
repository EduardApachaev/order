package com.service.order.messaging;

import com.service.order.messaging.message.OrderCreatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    public void sendOrderCreatedMessage(OrderCreatedMessage message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
