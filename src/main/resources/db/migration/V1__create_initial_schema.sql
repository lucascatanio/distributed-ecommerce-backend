-- =============================================
-- V1: Initial Schema
-- E-Commerce Monolith — Sprint 1
-- =============================================

-- =============================================
-- USERS
-- Nota: UNIQUE em email já cria B-tree index
--       automaticamente. Nenhum índice adicional
--       necessário nesta tabela.
-- =============================================
CREATE TABLE users
(
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email              VARCHAR(254) NOT NULL UNIQUE,
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100) NOT NULL,
    password_hash      VARCHAR(255) NOT NULL,
    role               VARCHAR(20)  NOT NULL DEFAULT 'CLIENT',
    address_street     VARCHAR(255),
    address_number     VARCHAR(20),
    address_complement VARCHAR(100),
    address_city       VARCHAR(100),
    address_state      VARCHAR(50),
    address_zip_code   VARCHAR(20),
    address_country    VARCHAR(50),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- =============================================
-- CATEGORIES
-- Nota: UNIQUE em name já cria B-tree index
--       automaticamente. Nenhum índice adicional
--       necessário nesta tabela.
-- =============================================
CREATE TABLE categories
(
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- =============================================
-- PRODUCTS
-- =============================================
CREATE TABLE products
(
    id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(200)   NOT NULL,
    description    VARCHAR(2000),
    price          NUMERIC(19, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER        NOT NULL CHECK (stock_quantity >= 0),
    category_id    UUID           NOT NULL REFERENCES categories (id),
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ
);

-- Suporta: SELECT * FROM products WHERE category_id = ?
-- (queries admin incluindo produtos deletados)
CREATE INDEX idx_products_category_id
    ON products (category_id);

-- Suporta: queries de existência/filtragem geral por soft delete
-- Partial index: só indexa linhas ativas (deleted_at IS NULL)
CREATE INDEX idx_products_active
    ON products (deleted_at)
    WHERE deleted_at IS NULL;

-- Suporta: browse de catálogo — query mais executada do sistema
-- SELECT * FROM products WHERE category_id = ? AND deleted_at IS NULL ORDER BY price
-- Index Only Scan possível: category_id + price cobertos pelo índice
-- Partial: exclui produtos deletados → índice menor e mais rápido
CREATE INDEX idx_products_catalog
    ON products (category_id, price)
    WHERE deleted_at IS NULL;

-- =============================================
-- ORDERS
-- =============================================
CREATE TABLE orders
(
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users (id),
    status     VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Suporta: histórico de pedidos de um usuário com ordenação por data
-- SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC
-- Regra de ouro respeitada: coluna de maior seletividade (user_id) primeiro
CREATE INDEX idx_orders_user_id_created_at
    ON orders (user_id, created_at);

-- =============================================
-- ORDER ITEMS
-- Nota: order_id é FK — PostgreSQL NÃO cria
--       índice em FK automaticamente.
-- =============================================
CREATE TABLE order_items
(
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID           NOT NULL REFERENCES orders (id),
    product_id   UUID           NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    unit_price   NUMERIC(19, 2) NOT NULL,
    quantity     INTEGER        NOT NULL CHECK (quantity > 0)
);

-- CRÍTICO: sem este índice, cada Order.getItems() resulta em
-- Seq Scan em order_items inteira.
-- Suporta: SELECT * FROM order_items WHERE order_id = ?
CREATE INDEX idx_order_items_order_id
    ON order_items (order_id);

-- =============================================
-- CARTS
-- Nota: UNIQUE em user_id já cria B-tree index
--       automaticamente. Nenhum índice adicional
--       necessário nesta tabela.
-- =============================================
CREATE TABLE carts
(
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL UNIQUE REFERENCES users (id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL
);

-- =============================================
-- CART ITEMS
-- Nota: cart_id é FK — PostgreSQL NÃO cria
--       índice em FK automaticamente.
-- =============================================
CREATE TABLE cart_items
(
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id      UUID           NOT NULL REFERENCES carts (id),
    product_id   UUID           NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    unit_price   NUMERIC(19, 2) NOT NULL,
    quantity     INTEGER        NOT NULL CHECK (quantity > 0 AND quantity <= 99)
);

-- CRÍTICO: sem este índice, cada Cart.getItems() resulta em
-- Seq Scan em cart_items inteira.
-- Suporta: SELECT * FROM cart_items WHERE cart_id = ?
CREATE INDEX idx_cart_items_cart_id
    ON cart_items (cart_id);