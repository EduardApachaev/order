package com.service.order.dto.request;

import com.service.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateStatusRequest(
        @NotNull
        OrderStatus status
) {
}
