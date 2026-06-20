package com.service.order.messaging.listener;

import com.service.order.messaging.message.OrderCreatedMessage;
import com.service.order.model.Order;
import com.service.order.model.OrderStatus;
import com.service.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = "${rabbitmq.queue}")
    @Transactional
    public void handleOrderCreated(OrderCreatedMessage message) {
        Order order = orderRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + message.getOrderId()));

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }
}
