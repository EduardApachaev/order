package com.service.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank
        String customerName,
        @NotEmpty
        List<OrderItemRequest> items
) {}
