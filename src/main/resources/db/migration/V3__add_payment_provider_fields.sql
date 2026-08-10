ALTER TABLE payments ADD COLUMN provider_name VARCHAR(40);
ALTER TABLE payments ADD COLUMN provider_transaction_id VARCHAR(100);