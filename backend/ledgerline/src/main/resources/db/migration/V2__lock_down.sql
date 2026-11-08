-- 1. Block TRUNCATE on the tables that must never be wiped
CREATE TRIGGER audit_log_no_truncate BEFORE TRUNCATE ON audit_log
  FOR EACH STATEMENT EXECUTE FUNCTION block_changes();
CREATE TRIGGER ledger_no_truncate BEFORE TRUNCATE ON ledger_entries
  FOR EACH STATEMENT EXECUTE FUNCTION block_changes();
CREATE TRIGGER transactions_no_truncate BEFORE TRUNCATE ON transactions
  FOR EACH STATEMENT EXECUTE FUNCTION block_changes();

-- 2. Every transaction's ledger entries must add up to zero (checked at commit)
CREATE FUNCTION check_ledger_balanced() RETURNS trigger AS $$
BEGIN
  IF (SELECT COALESCE(SUM(amount_paise), 0)
        FROM ledger_entries
       WHERE transaction_id = NEW.transaction_id) <> 0 THEN
    RAISE EXCEPTION 'Ledger entries for transaction % do not sum to zero', NEW.transaction_id;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER ledger_balanced
  AFTER INSERT ON ledger_entries
  DEFERRABLE INITIALLY DEFERRED
  FOR EACH ROW EXECUTE FUNCTION check_ledger_balanced();

-- 3. The app user gets only what it needs
GRANT USAGE ON SCHEMA public TO ledgerline_app;
GRANT SELECT, INSERT ON users TO ledgerline_app;
GRANT SELECT, INSERT, UPDATE ON accounts TO ledgerline_app;
GRANT SELECT, INSERT ON transactions TO ledgerline_app;
GRANT SELECT, INSERT ON ledger_entries TO ledgerline_app;
GRANT SELECT, INSERT ON audit_log TO ledgerline_app;
GRANT SELECT, INSERT, UPDATE ON idempotency_keys TO ledgerline_app;
GRANT USAGE, SELECT ON SEQUENCE ledger_entries_id_seq, audit_log_id_seq TO ledgerline_app;