-- IgirePay database schema (PostgreSQL)

-- Drop the tables first so you can run this file again without errors.
-- The order matters: tables that point to others are dropped first.
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS processed_requests;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;


-- ------------------------------------------------------------
-- 1. customers
-- ------------------------------------------------------------
CREATE TABLE customers (
                           id SERIAL PRIMARY KEY,
                           full_name VARCHAR(100) NOT NULL,
                           email VARCHAR(100) UNIQUE NOT NULL,
                           phone_number VARCHAR(20)
);

-- ------------------------------------------------------------
-- 2. accounts
-- ------------------------------------------------------------
CREATE TABLE accounts (
                          id SERIAL PRIMARY KEY,
                          customer_id INT NOT NULL,
                          account_type VARCHAR(50) NOT NULL,
                          balance NUMERIC(12,2) DEFAULT 0.00,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- ------------------------------------------------------------
-- 3. transactions
-- ------------------------------------------------------------
CREATE TABLE transactions (
                              id SERIAL PRIMARY KEY,
                              account_id INT NOT NULL,
                              reference_id VARCHAR(50) UNIQUE NOT NULL,
                              transaction_type VARCHAR(20) NOT NULL,
                              amount NUMERIC(12,2) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (account_id) REFERENCES accounts(id)
);


-- ------------------------------------------------------------
-- 4. processed_requests
-- ------------------------------------------------------------
CREATE TABLE processed_requests (
    id SERIAL PRIMARY KEY,
    reference_id VARCHAR(50) UNIQUE NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);