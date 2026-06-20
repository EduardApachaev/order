package com.service.order.service.impl;

import com.service.order.dto.request.CreateOrderRequest;
import com.service.order.dto.request.UpdateStatusRequest;
import com.service.order.dto.response.OrderResponse;
import com.service.order.exception.OrderNotFoundException;
import com.service.order.mapper.OrderMapper;
import com.service.order.messaging.OrderMessageProducer;
import com.service.order.messaging.message.OrderCreatedMessage;
import com.service.order.model.Order;
import com.service.order.model.OrderStatus;
import com.service.order.repository.OrderRepository;
import com.service.order.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderMessageProducer messageProducer;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order savedOrder  = orderMapper.toEntity(request);
        orderRepository.save(savedOrder);

        OrderCreatedMessage message = OrderCreatedMessage.builder()
                .orderId(savedOrder.getId())
                .customerName(savedOrder.getCustomerName())
                .totalAmount(calculateTotalAmount(savedOrder))
                .build();

        messageProducer.sendOrderCreatedMessage(message);

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public Page<OrderResponse> getOrders(OrderStatus status, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse getOrderById(UUID id) {
        Order order = findOrderById(id);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateStatusRequest request) {
        Order order = findOrderById(id);
        order.setStatus(request.status());

        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toResponse(updatedOrder);
    }

    private Order findOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    private BigDecimal calculateTotalAmount(Order order) {
        return order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
