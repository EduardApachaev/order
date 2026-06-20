package com.service.order;

import com.service.order.messaging.listener.OrderMessageListener;
import com.service.order.messaging.message.OrderCreatedMessage;
import com.service.order.model.Order;
import com.service.order.model.OrderItem;
import com.service.order.model.OrderStatus;
import com.service.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderMessageListenerTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMessageListener listener;

    @Test
    void shouldProcessMessageAndUpdateStatus() {
        // Given
        Order order = createTestOrder();
        Order savedOrder = orderRepository.save(order);

        OrderCreatedMessage message = OrderCreatedMessage.builder()
                .orderId(savedOrder.getId())
                .customerName("Test Customer")
                .totalAmount(new BigDecimal("299.99"))
                .build();

        // When
        listener.handleOrderCreated(message);

        // Then
        Order processedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(processedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setCustomerName("Test Customer");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        OrderItem item = OrderItem.builder()
                .productName("Test Product")
                .quantity(1)
                .price(new BigDecimal("299.99"))
                .order(order)
                .build();

        order.setItems(List.of(item));
        return order;
    }
}
