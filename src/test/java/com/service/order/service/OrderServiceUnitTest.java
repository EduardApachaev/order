package com.service.order.service;

import com.service.order.dto.request.CreateOrderRequest;
import com.service.order.dto.request.OrderItemRequest;
import com.service.order.dto.request.UpdateStatusRequest;
import com.service.order.dto.response.OrderItemResponse;
import com.service.order.dto.response.OrderResponse;
import com.service.order.mapper.OrderMapper;
import com.service.order.messaging.OrderMessageProducer;
import com.service.order.messaging.message.OrderCreatedMessage;
import com.service.order.model.Order;
import com.service.order.model.OrderItem;
import com.service.order.model.OrderStatus;
import com.service.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderMessageProducer messageProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndSendMessage() {
        // Given
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Laptop")
                .quantity(2)
                .price(new BigDecimal("999.99")).build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("John Doe")
                .items(List.of(itemRequest))
                .build();

        Order savedOrder = createMockOrder();
        OrderResponse expectedResponse = createMockResponse(UUID.randomUUID());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        // When
        OrderResponse actualResponse = orderService.createOrder(request);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.id()).isEqualTo(expectedResponse.id());

        // Verify repository called
        verify(orderRepository, times(1)).save(any(Order.class));

        // Verify message producer called with correct data
        ArgumentCaptor<OrderCreatedMessage> messageCaptor =
                ArgumentCaptor.forClass(OrderCreatedMessage.class);
        verify(messageProducer, times(1)).sendOrderCreatedMessage(messageCaptor.capture());

        OrderCreatedMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getOrderId()).isEqualTo(savedOrder.getId());
        assertThat(capturedMessage.getCustomerName()).isEqualTo("John Doe");
    }

    @Test
    void shouldGetOrderById() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = createMockOrder();
        order.setId(orderId);

        OrderResponse expectedResponse = createMockResponse(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        // When
        OrderResponse actualResponse = orderService.getOrderById(orderId);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.id()).isEqualTo(orderId);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void shouldUpdateOrderStatus() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = createMockOrder();
        order.setId(orderId);
        order.setStatus(OrderStatus.CREATED);

        UpdateStatusRequest request = new UpdateStatusRequest(OrderStatus.PROCESSING);

        OrderResponse expectedResponse = createMockResponse(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        // When
        OrderResponse actualResponse = orderService.updateStatus(orderId, request);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.status()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    private Order createMockOrder() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerName("John Doe");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductName("Laptop");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("999.99"));
        item.setOrder(order);

        order.setItems(List.of(item));
        return order;
    }

    private OrderResponse createMockResponse(UUID id) {
        return OrderResponse.builder()
                .id(UUID.randomUUID())
                .customerName("John Doe")
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.CREATED)
                .totalAmount(new BigDecimal("1999.98"))
                .items(List.of(OrderItemResponse.builder()
                        .id(1L)
                        .productName("Laptop")
                        .quantity(2)
                        .price(new BigDecimal("999.99"))
                        .totalPrice(new BigDecimal("1999.98"))
                        .build()))
                .build();
    }
}
