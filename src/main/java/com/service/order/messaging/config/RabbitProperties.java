package com.service.order.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitProperties(
        String exchange,
        String queue,
        String routingKey
) {}