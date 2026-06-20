package com.service.order.messaging.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;

import org.springframework.amqp.core.Queue;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMQConfig {

    private final RabbitProperties properties;

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(
                properties.exchange()
        );
    }


    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(
                properties.queue(),
                true
        );
    }

    @Bean
    public Binding orderCreatedBinding(
            Queue queue,
            TopicExchange exchange
    ) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(properties.routingKey());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
