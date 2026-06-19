package com.service.order.mapper;

import com.service.order.dto.request.CreateOrderRequest;
import com.service.order.dto.response.OrderItemResponse;
import com.service.order.dto.response.OrderResponse;
import com.service.order.model.Order;
import com.service.order.model.OrderItem;
import com.service.order.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public Order toEntity(CreateOrderRequest dto) {
        List<OrderItem> items = dto.items()
                .stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setProductName(itemRequest.productName());
                    item.setQuantity(itemRequest.quantity());
                    item.setPrice(itemRequest.price());
                    return item;
                })
                .collect(Collectors.toList());

        Order order = Order.builder()
                .customerName(dto.customerName())
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.CREATED)
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));

        return order;
    }

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .orderDate(order.getOrderDate())
                .status(order.getOrderStatus())
                .totalAmount(calculateTotal(order))
                .items(order.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
