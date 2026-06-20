package com.service.order;

import com.service.order.dto.request.CreateOrderRequest;
import com.service.order.dto.request.OrderItemRequest;
import com.service.order.model.Order;
import com.service.order.model.OrderStatus;
import com.service.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;


import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderAndProcessMessage() throws Exception {
        // Given

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Test Product")
                .quantity(3)
                .price(new BigDecimal("149.99")).build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Integration Test Customer")
                .items(List.of(itemRequest))
                .build();

        // When - отправляем POST запрос
        String responseBody = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Integration Test Customer"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(449.97))
                .andExpect(jsonPath("$.items[0].productName").value("Test Product"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - проверяем сохранение в БД
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);

        Order savedOrder = orders.get(0);
        assertThat(savedOrder.getCustomerName()).isEqualTo("Integration Test Customer");
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrder.getItems()).hasSize(1);

        // Ждем обработки сообщения и изменения статуса
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order processedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
            assertThat(processedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        });
    }

    @Test
    void shouldReturn400ForInvalidOrder() throws Exception {
        // Given - пустой запрос
        CreateOrderRequest invalidRequest = new CreateOrderRequest("", (List.of()));

        // When/Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // Проверяем, что ничего не сохранилось в БД
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // Given - сначала создаем заказ

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Product A")
                .quantity(1)
                .price(new BigDecimal("99.99")).build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Get Order Test")
                .items(List.of(itemRequest))
                .build();

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(createResponse).get("id").asText();

        // When/Then - получаем заказ по ID
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerName").value("Get Order Test"))
                .andExpect(jsonPath("$.status").exists());
    }
}