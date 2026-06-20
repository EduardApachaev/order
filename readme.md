
```markdown


## API Endpoints

### Создание заказа
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "items": [
      {
        "productName": "Laptop",
        "quantity": 1,
        "price": 999.99
      },
      {
        "productName": "Mouse",
        "quantity": 2,
        "price": 29.99
      }
    ]
  }'
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "John Doe",
  "orderDate": "2024-01-15T10:30:00",
  "status": "CREATED",
  "totalAmount": 1059.97,
  "items": [
    {
      "id": 1,
      "productName": "Laptop",
      "quantity": 1,
      "price": 999.99,
      "totalPrice": 999.99
    },
    {
      "id": 2,
      "productName": "Mouse",
      "quantity": 2,
      "price": 29.99,
      "totalPrice": 59.98
    }
  ]
}
```

### Получение списка заказов с фильтрацией и пагинацией
```bash
# Все заказы
curl "http://localhost:8080/api/orders?page=0&size=20&sort=orderDate,desc"

# Фильтрация по статусу
curl "http://localhost:8080/api/orders?status=PROCESSING"

# Пагинация + сортировка по customerName
curl "http://localhost:8080/api/orders?page=0&size=10&sort=customerName,asc"
```

### Получение заказа по ID
```bash
curl "http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000"
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "John Doe",
  "orderDate": "2024-01-15T10:30:00",
  "status": "PROCESSING",
  "totalAmount": 1059.97,
  "items": [
    {
      "id": 1,
      "productName": "Laptop",
      "quantity": 1,
      "price": 999.99,
      "totalPrice": 999.99
    },
    {
      "id": 2,
      "productName": "Mouse",
      "quantity": 2,
      "price": 29.99,
      "totalPrice": 59.98
    }
  ]
}
```

### Обновление статуса заказа
```bash
curl -X PUT "http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "COMPLETED"}'
```

### Получение общей суммы заказов клиента
```bash
curl "http://localhost:8080/api/orders/customer/John%20Doe/total"
```

**Response (200 OK):**
```
1059.97
```

### Обработка ошибок

**404 Not Found:**
```bash
curl "http://localhost:8080/api/orders/00000000-0000-0000-0000-000000000000"
```
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 00000000-0000-0000-0000-000000000000",
  "path": "/api/orders/00000000-0000-0000-0000-000000000000"
}
```

**400 Bad Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName": "", "items": []}'
```
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Customer name is required, Order must contain at least one item",
  "path": "/api/orders"
}
```


## Структура проекта

```
src/main/java/com/example/order/
├── config/
│   └── RabbitMQConfig.java              # Конфигурация RabbitMQ
├── controller/
│   ├── OrderController.java             # REST контроллер
│   └── OrderStatisticsController.java   # Контроллер статистики
├── dto/
│   ├── CreateOrderRequest.java          # DTO создания заказа
│   ├── OrderItemRequest.java            # DTO позиции заказа
│   ├── OrderResponse.java               # DTO ответа заказа
│   ├── OrderItemResponse.java           # DTO позиции в ответе
│   └── UpdateStatusRequest.java         # DTO обновления статуса
├── exception/
│   ├── ErrorResponse.java               # DTO ошибки
│   ├── GlobalExceptionHandler.java      # Глобальный обработчик ошибок
│   └── OrderNotFoundException.java      # Исключение "заказ не найден"
├── mapper/
│   └── OrderMapper.java                 # Маппер Entity -> DTO
├── messaging/
│   ├── OrderCreatedMessage.java         # Сообщение в RabbitMQ
│   ├── OrderMessageListener.java        # Listener RabbitMQ (@RabbitListener)
│   └── OrderMessageProducer.java        # Продюсер сообщений
├── model/
│   ├── Order.java                       # Entity заказа
│   ├── OrderItem.java                   # Entity позиции заказа
│   └── OrderStatus.java                 # Enum статусов
├── repository/
│   └── OrderRepository.java             # JPA репозиторий с кастомными запросами
├── service/
│   ├── OrderService.java                # Бизнес-логика заказов
│   └── OrderStatisticsService.java      # Сервис статистики
└── OrderApplication.java                # Main класс

src/main/resources/
├── db/migration/
│   ├── V1__create_initial_tables.sql
│   └── V2__add_indexes.sql
└── application.properties
```

## Docker команды

```bash
# Запуск инфраструктуры
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Остановка
docker-compose down

# Остановка с удалением данных
docker-compose down -v
```