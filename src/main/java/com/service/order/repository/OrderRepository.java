package com.service.order.repository;

import com.service.order.model.Order;
import com.service.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findAll(Pageable pageable);

    @Query("SELECT COALESCE(SUM(oi.price * oi.quantity), 0) " +
            "FROM Order o " +
            "JOIN o.items oi " +
            "WHERE o.customerName = :customerName")
    BigDecimal getTotalAmountByCustomer(@Param("customerName") String customerName);
}
