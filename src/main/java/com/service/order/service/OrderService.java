package com.service.order.service;

import com.service.order.dto.request.CreateOrderRequest;
import com.service.order.dto.request.UpdateStatusRequest;
import com.service.order.dto.response.OrderResponse;
import com.service.order.model.OrderStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    Page<OrderResponse> getOrders(OrderStatus status, int page, int size, String sort);

    OrderResponse getOrderById(UUID id);

    OrderResponse updateStatus(UUID id, @Valid UpdateStatusRequest request);
}
