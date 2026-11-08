CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN','HOLDER','AUDITOR')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES users(id),
  account_type VARCHAR(10) NOT NULL DEFAULT 'CUSTOMER' CHECK (account_type IN ('CUSTOMER','SYSTEM')),
  balance_paise BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT no_negative_customer_balance
    CHECK (account_type = 'SYSTEM' OR balance_paise >= 0)
);

CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  type VARCHAR(12) NOT NULL CHECK (type IN ('DEPOSIT','WITHDRAWAL','TRANSFER')),
  amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
  created_by UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries (
  id BIGSERIAL PRIMARY KEY,
  transaction_id UUID NOT NULL REFERENCES transactions(id),
  account_id UUID NOT NULL REFERENCES accounts(id),
  amount_paise BIGINT NOT NULL CHECK (amount_paise <> 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE idempotency_keys (
  user_id UUID NOT NULL REFERENCES users(id),
  idem_key VARCHAR(100) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  response_status INT,
  response_body TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, idem_key)
);

CREATE TABLE audit_log (
  id BIGSERIAL PRIMARY KEY,
  actor_id UUID,
  action VARCHAR(50) NOT NULL,
  entity VARCHAR(50),
  entity_id VARCHAR(64),
  details TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE FUNCTION block_changes() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'This table is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_log_append_only BEFORE UPDATE OR DELETE ON audit_log
  FOR EACH ROW EXECUTE FUNCTION block_changes();
CREATE TRIGGER ledger_append_only BEFORE UPDATE OR DELETE ON ledger_entries
  FOR EACH ROW EXECUTE FUNCTION block_changes();
CREATE TRIGGER transactions_append_only BEFORE UPDATE OR DELETE ON transactions
  FOR EACH ROW EXECUTE FUNCTION block_changes();