package com.service.order.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long id,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice
) {}
