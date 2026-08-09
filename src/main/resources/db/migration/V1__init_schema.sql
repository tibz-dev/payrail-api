CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE merchants (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           merchant_ref VARCHAR(40) NOT NULL UNIQUE,      -- e.g. mch_01HXYZ
                           user_id UUID NOT NULL REFERENCES users(id),
                           business_name VARCHAR(255) NOT NULL,
                           contact_name VARCHAR(255) NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE api_keys (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          merchant_id UUID NOT NULL REFERENCES merchants(id),
                          key_prefix VARCHAR(16) NOT NULL,               -- pk_test_
                          key_hash VARCHAR(255) NOT NULL,                -- hashed, never store raw
                          last_four VARCHAR(4) NOT NULL,                 -- for display
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | REVOKED
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          revoked_at TIMESTAMPTZ
);

CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          payment_ref VARCHAR(40) NOT NULL UNIQUE,       -- pay_01JXXXX
                          merchant_id UUID NOT NULL REFERENCES merchants(id),
                          amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
                          currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
                          reference VARCHAR(255) NOT NULL,
                          description VARCHAR(500),
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                          checkout_token VARCHAR(64) NOT NULL UNIQUE,
                          expires_at TIMESTAMPTZ NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_merchant ON payments(merchant_id);
CREATE INDEX idx_payments_status ON payments(status);

CREATE TABLE payment_events (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                payment_id UUID NOT NULL REFERENCES payments(id),
                                from_status VARCHAR(20),
                                to_status VARCHAR(20) NOT NULL,
                                metadata JSONB,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_accounts (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 merchant_id UUID REFERENCES merchants(id),     -- null for platform-level accounts
                                 account_type VARCHAR(40) NOT NULL,              -- CLEARING | MERCHANT_PAYABLE | PLATFORM_FEE
                                 currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                payment_id UUID NOT NULL REFERENCES payments(id),
                                account_id UUID NOT NULL REFERENCES ledger_accounts(id),
                                entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT','CREDIT')),
                                amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
                                currency VARCHAR(3) NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    -- append-only: no updated_at, no UPDATE/DELETE grants at the DB role level
);

CREATE TABLE webhooks (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          payment_id UUID NOT NULL REFERENCES payments(id),
                          merchant_id UUID NOT NULL REFERENCES merchants(id),
                          event_type VARCHAR(40) NOT NULL,
                          destination_url VARCHAR(500) NOT NULL,
                          payload JSONB NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING | DELIVERED | FAILED
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE webhook_attempts (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  webhook_id UUID NOT NULL REFERENCES webhooks(id),
                                  attempt_number INT NOT NULL,
                                  http_status INT,
                                  response_body TEXT,
                                  succeeded BOOLEAN NOT NULL,
                                  attempted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE idempotency_keys (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  merchant_id UUID NOT NULL REFERENCES merchants(id),
                                  idempotency_key VARCHAR(255) NOT NULL,
                                  request_hash VARCHAR(64) NOT NULL,              -- hash of the request body
                                  payment_id UUID NOT NULL REFERENCES payments(id),
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  UNIQUE (merchant_id, idempotency_key)
);