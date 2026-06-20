package com.service.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateOrderRequest(
        @NotBlank
        String customerName,
        @NotEmpty
        List<OrderItemRequest> items
) {}
