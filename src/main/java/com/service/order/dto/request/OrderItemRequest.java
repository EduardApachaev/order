package com.service.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
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
