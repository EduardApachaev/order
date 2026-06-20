package com.service.order;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
@TestConfiguration
public class RabbitTestConfig {

    private EmbeddedRabbitMq rabbitMQ;

    @Bean
    public EmbeddedRabbitMq embeddedRabbitMQ() {
        EmbeddedRabbitMqConfig config =
                new EmbeddedRabbitMqConfig.Builder()
                        .rabbitMqServerInitializationTimeoutInMillis(10000)
                        .defaultRabbitMqCtlTimeoutInMillis(10000)
                        .build();

        rabbitMQ = new EmbeddedRabbitMq(config);
        rabbitMQ.start();
        return rabbitMQ;
    }

    @PreDestroy
    public void stopRabbitMQ() {
        if (rabbitMQ != null) {
            rabbitMQ.stop();
        }
    }
}
