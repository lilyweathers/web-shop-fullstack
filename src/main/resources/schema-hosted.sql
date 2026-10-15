-- Hibernate's unindexed List join tables need explicit primary keys on Aiven.
-- Each order/line belongs to only one parent; these keys preserve that mapping.
CREATE TABLE IF NOT EXISTS person_orders (
    customer_id BIGINT NOT NULL,
    orders_id BIGINT NOT NULL PRIMARY KEY
);
CREATE TABLE IF NOT EXISTS customer_order_items (
    customer_order_id BIGINT NOT NULL,
    items_id BIGINT NOT NULL PRIMARY KEY
);
