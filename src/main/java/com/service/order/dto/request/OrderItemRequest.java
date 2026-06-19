package com.service.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank
        String productName,

        @NotNull
        @Positive
        Integer quantity,

        @NotNull
        @Positive
        BigDecimal price
) {
}
