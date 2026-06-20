CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        customer_name VARCHAR(100) NOT NULL,
                        order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(20) NOT NULL DEFAULT 'CREATED'
                            CHECK (status IN ('CREATED', 'PROCESSING', 'COMPLETED', 'CANCELED'))
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id UUID NOT NULL,
                             product_name VARCHAR(200) NOT NULL,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id)
                                     ON DELETE CASCADE
);

CREATE INDEX idx_orders_customer_name ON orders(customer_name);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);