-- Acme Industrial database. Generated from the same definitions as the Nebula stub.
BEGIN;
DROP TABLE IF EXISTS contracts_agreement;
CREATE TABLE contracts_agreement (
  agreement_id varchar(24) NOT NULL,
  company_no varchar(16) NOT NULL,
  debtor_code varchar(20) NOT NULL,
  starts_on date NOT NULL,
  ends_on date NOT NULL,
  currency varchar(3) NOT NULL,
  status varchar(20) NOT NULL,
  PRIMARY KEY (agreement_id)
);
INSERT INTO contracts_agreement (agreement_id, company_no, debtor_code, starts_on, ends_on, currency, status) VALUES ('AGR-44018', '08392811', 'D001922', '2025-08-01', '2026-07-31', 'GBP', 'EXPIRED');
INSERT INTO contracts_agreement (agreement_id, company_no, debtor_code, starts_on, ends_on, currency, status) VALUES ('AGR-44077', '09115520', 'D004101', '2026-01-01', '2026-12-31', 'GBP', 'ACTIVE');
DROP TABLE IF EXISTS contracts_price_rule;
CREATE TABLE contracts_price_rule (
  rule_id varchar(24) NOT NULL,
  agreement_id varchar(24) NOT NULL,
  product_sku varchar(32) NOT NULL,
  unit_price numeric(12,2) NOT NULL,
  discount_pct numeric(6,2) NOT NULL,
  PRIMARY KEY (rule_id)
);
INSERT INTO contracts_price_rule (rule_id, agreement_id, product_sku, unit_price, discount_pct) VALUES ('PR-77101', 'AGR-44018', 'ACM-4400-B', 18.75, 6.25);
INSERT INTO contracts_price_rule (rule_id, agreement_id, product_sku, unit_price, discount_pct) VALUES ('PR-77102', 'AGR-44018', 'ACM-7710-X', 41.2, 8.0);
DROP TABLE IF EXISTS contracts_entitlement;
CREATE TABLE contracts_entitlement (
  agreement_id varchar(24) NOT NULL,
  category_id varchar(20) NOT NULL,
  max_qty integer NOT NULL,
  active_flag boolean NOT NULL,
  PRIMARY KEY (agreement_id, category_id)
);
INSERT INTO contracts_entitlement (agreement_id, category_id, max_qty, active_flag) VALUES ('AGR-44018', 'ELEC-CONTROL', 5000, FALSE);
DROP TABLE IF EXISTS contracts_renewal_audit;
CREATE TABLE contracts_renewal_audit (
  audit_id varchar(24) NOT NULL,
  agreement_id varchar(24) NOT NULL,
  changed_at timestamp with time zone NOT NULL,
  note text,
  PRIMARY KEY (audit_id)
);
INSERT INTO contracts_renewal_audit (audit_id, agreement_id, changed_at, note) VALUES ('AUD-9001', 'AGR-44018', '2026-07-15T10:00:00Z', 'Renewal reminder sent to account owner.');
DROP TABLE IF EXISTS contracts_account_terms;
CREATE TABLE contracts_account_terms (
  debtor_code varchar(20) NOT NULL,
  payment_terms varchar(20) NOT NULL,
  credit_review_date date NOT NULL,
  PRIMARY KEY (debtor_code)
);
INSERT INTO contracts_account_terms (debtor_code, payment_terms, credit_review_date) VALUES ('D001922', 'NET30', '2026-09-01');
DROP TABLE IF EXISTS inventory_sku_map;
CREATE TABLE inventory_sku_map (
  product_sku varchar(32) NOT NULL,
  warehouse_sku varchar(32) NOT NULL,
  supplier_id varchar(20) NOT NULL,
  supplier_part varchar(40) NOT NULL,
  legacy_sku varchar(30),
  PRIMARY KEY (product_sku, warehouse_sku)
);
INSERT INTO inventory_sku_map (product_sku, warehouse_sku, supplier_id, supplier_part, legacy_sku) VALUES ('ACM-4400-B', '4400B', 'SUP-88', 'XF-99281', 'NS-44B');
INSERT INTO inventory_sku_map (product_sku, warehouse_sku, supplier_id, supplier_part, legacy_sku) VALUES ('ACM-7710-X', '7710X', 'SUP-42', 'QZ-7710/UK', 'NX7710');
INSERT INTO inventory_sku_map (product_sku, warehouse_sku, supplier_id, supplier_part, legacy_sku) VALUES ('ACM-7710-XR', '7710XR', 'SUP-42', 'QZ-7710R/UK', NULL);
DROP TABLE IF EXISTS inventory_stock_snapshot;
CREATE TABLE inventory_stock_snapshot (
  warehouse_sku varchar(32) NOT NULL,
  warehouse_code varchar(12) NOT NULL,
  on_hand integer NOT NULL,
  reserved_qty integer NOT NULL,
  snapshot_at timestamp with time zone NOT NULL,
  PRIMARY KEY (warehouse_sku, warehouse_code)
);
INSERT INTO inventory_stock_snapshot (warehouse_sku, warehouse_code, on_hand, reserved_qty, snapshot_at) VALUES ('4400B', 'BHX', 240, 80, '2026-08-07T15:00:00Z');
INSERT INTO inventory_stock_snapshot (warehouse_sku, warehouse_code, on_hand, reserved_qty, snapshot_at) VALUES ('7710X', 'BHX', 20, 20, '2026-08-07T15:00:00Z');
INSERT INTO inventory_stock_snapshot (warehouse_sku, warehouse_code, on_hand, reserved_qty, snapshot_at) VALUES ('7710X', 'RTM', 55, 15, '2026-08-07T15:00:00Z');
INSERT INTO inventory_stock_snapshot (warehouse_sku, warehouse_code, on_hand, reserved_qty, snapshot_at) VALUES ('7710X', 'GLA', 8, 8, '2026-08-07T15:00:00Z');
DROP TABLE IF EXISTS inventory_reservation_ledger;
CREATE TABLE inventory_reservation_ledger (
  reservation_id varchar(24) NOT NULL,
  order_id varchar(24) NOT NULL,
  order_line_id varchar(24) NOT NULL,
  warehouse_sku varchar(32) NOT NULL,
  warehouse_code varchar(12) NOT NULL,
  reserved_qty integer NOT NULL,
  state varchar(20) NOT NULL,
  PRIMARY KEY (reservation_id)
);
INSERT INTO inventory_reservation_ledger (reservation_id, order_id, order_line_id, warehouse_sku, warehouse_code, reserved_qty, state) VALUES ('RES-2001', 'ORD-38291', 'L-20', '7710X', 'BHX', 20, 'ALLOCATED');
INSERT INTO inventory_reservation_ledger (reservation_id, order_id, order_line_id, warehouse_sku, warehouse_code, reserved_qty, state) VALUES ('RES-2002', 'ORD-38291', 'L-10', '4400B', 'BHX', 10, 'ALLOCATED');
DROP TABLE IF EXISTS inventory_warehouse;
CREATE TABLE inventory_warehouse (
  warehouse_code varchar(12) NOT NULL,
  name varchar(100) NOT NULL,
  address text NOT NULL,
  country_code varchar(2) NOT NULL,
  PRIMARY KEY (warehouse_code)
);
INSERT INTO inventory_warehouse (warehouse_code, name, address, country_code) VALUES ('BHX', 'Birmingham DC', 'Birmingham, UK', 'GB');
INSERT INTO inventory_warehouse (warehouse_code, name, address, country_code) VALUES ('RTM', 'Rotterdam DC', 'Rotterdam, NL', 'NL');
INSERT INTO inventory_warehouse (warehouse_code, name, address, country_code) VALUES ('GLA', 'Glasgow DC', 'Glasgow, UK', 'GB');
DROP TABLE IF EXISTS inventory_cycle_count;
CREATE TABLE inventory_cycle_count (
  count_id varchar(24) NOT NULL,
  warehouse_sku varchar(32) NOT NULL,
  warehouse_code varchar(12) NOT NULL,
  counted_qty integer NOT NULL,
  counted_at timestamp with time zone NOT NULL,
  PRIMARY KEY (count_id)
);
INSERT INTO inventory_cycle_count (count_id, warehouse_sku, warehouse_code, counted_qty, counted_at) VALUES ('CC-1001', '7710X', 'BHX', 20, '2026-08-07T07:00:00Z');
DROP TABLE IF EXISTS inventory_reorder_policy;
CREATE TABLE inventory_reorder_policy (
  warehouse_sku varchar(32) NOT NULL,
  warehouse_code varchar(12) NOT NULL,
  reorder_point integer NOT NULL,
  target_stock integer NOT NULL,
  PRIMARY KEY (warehouse_sku, warehouse_code)
);
INSERT INTO inventory_reorder_policy (warehouse_sku, warehouse_code, reorder_point, target_stock) VALUES ('7710X', 'BHX', 100, 400);
DROP TABLE IF EXISTS finance_cash_application;
CREATE TABLE finance_cash_application (
  transaction_ref varchar(24) NOT NULL,
  invoice_no varchar(24) NOT NULL,
  debtor_code varchar(20) NOT NULL,
  received_amount numeric(12,2) NOT NULL,
  received_on date NOT NULL,
  PRIMARY KEY (transaction_ref, invoice_no)
);
INSERT INTO finance_cash_application (transaction_ref, invoice_no, debtor_code, received_amount, received_on) VALUES ('TX-88292', 'INV-88291', 'D001922', 3228.5, '2026-08-06');
INSERT INTO finance_cash_application (transaction_ref, invoice_no, debtor_code, received_amount, received_on) VALUES ('TX-81110', 'INV-87001', 'D001922', 5120.0, '2026-06-18');
DROP TABLE IF EXISTS finance_payment_terms;
CREATE TABLE finance_payment_terms (
  debtor_code varchar(20) NOT NULL,
  terms_code varchar(20) NOT NULL,
  days_due integer NOT NULL,
  PRIMARY KEY (debtor_code)
);
INSERT INTO finance_payment_terms (debtor_code, terms_code, days_due) VALUES ('D001922', 'NET30', 30);
DROP TABLE IF EXISTS finance_credit_review;
CREATE TABLE finance_credit_review (
  review_id varchar(24) NOT NULL,
  debtor_code varchar(20) NOT NULL,
  reviewed_on date NOT NULL,
  risk_score numeric(5,2) NOT NULL,
  note text,
  PRIMARY KEY (review_id)
);
INSERT INTO finance_credit_review (review_id, debtor_code, reviewed_on, risk_score, note) VALUES ('CR-991', 'D001922', '2026-07-20', 31.5, 'Stable account; one invoice disputed.');
DROP TABLE IF EXISTS finance_dispute;
CREATE TABLE finance_dispute (
  dispute_id varchar(24) NOT NULL,
  invoice_no varchar(24) NOT NULL,
  opened_on date NOT NULL,
  amount numeric(12,2) NOT NULL,
  status varchar(20) NOT NULL,
  PRIMARY KEY (dispute_id)
);
INSERT INTO finance_dispute (dispute_id, invoice_no, opened_on, amount, status) VALUES ('DSP-73', 'INV-88014', '2026-07-30', 8240.0, 'OPEN');
DROP TABLE IF EXISTS finance_credit_note_ledger;
CREATE TABLE finance_credit_note_ledger (
  credit_note_no varchar(24) NOT NULL,
  invoice_no varchar(24) NOT NULL,
  issued_on date NOT NULL,
  amount numeric(12,2) NOT NULL,
  currency varchar(3) NOT NULL,
  PRIMARY KEY (credit_note_no)
);
INSERT INTO finance_credit_note_ledger (credit_note_no, invoice_no, issued_on, amount, currency) VALUES ('CN-1011', 'INV-87001', '2026-06-10', 120.0, 'GBP');
DROP TABLE IF EXISTS analytics_customer_signal;
CREATE TABLE analytics_customer_signal (
  signal_id varchar(24) NOT NULL,
  company_no varchar(16) NOT NULL,
  signal_type varchar(40) NOT NULL,
  score numeric(5,2) NOT NULL,
  observed_at timestamp with time zone NOT NULL,
  PRIMARY KEY (signal_id)
);
INSERT INTO analytics_customer_signal (signal_id, company_no, signal_type, score, observed_at) VALUES ('SIG-201', '08392811', 'ORDER_DELAY_RATE', 72.0, '2026-08-06T23:00:00Z');
INSERT INTO analytics_customer_signal (signal_id, company_no, signal_type, score, observed_at) VALUES ('SIG-202', '08392811', 'PAYMENT_RISK', 31.5, '2026-08-06T23:00:00Z');
DROP TABLE IF EXISTS analytics_order_event;
CREATE TABLE analytics_order_event (
  event_id varchar(24) NOT NULL,
  order_id varchar(24) NOT NULL,
  event_type varchar(40) NOT NULL,
  event_at timestamp with time zone NOT NULL,
  detail text,
  PRIMARY KEY (event_id)
);
INSERT INTO analytics_order_event (event_id, order_id, event_type, event_at, detail) VALUES ('EVT-1', 'ORD-38291', 'ALLOCATED_PARTIAL', '2026-08-06T10:11:00Z', 'Line L-20 only partially allocatable at BHX.');
INSERT INTO analytics_order_event (event_id, order_id, event_type, event_at, detail) VALUES ('EVT-2', 'ORD-38291', 'PICK_BLOCKED', '2026-08-06T10:20:00Z', 'Pick held until full-order policy satisfied.');
DROP TABLE IF EXISTS analytics_service_case_signal;
CREATE TABLE analytics_service_case_signal (
  signal_id varchar(24) NOT NULL,
  ticket_id varchar(24) NOT NULL,
  order_id varchar(24),
  severity numeric(5,2) NOT NULL,
  PRIMARY KEY (signal_id)
);
INSERT INTO analytics_service_case_signal (signal_id, ticket_id, order_id, severity) VALUES ('SIG-T1', 'TCK-7788', 'ORD-38291', 80.0);
DROP TABLE IF EXISTS analytics_supplier_performance;
CREATE TABLE analytics_supplier_performance (
  supplier_id varchar(20) NOT NULL,
  supplier_part varchar(40) NOT NULL,
  otif_pct numeric(5,2) NOT NULL,
  avg_delay_days numeric(5,2) NOT NULL,
  PRIMARY KEY (supplier_id, supplier_part)
);
INSERT INTO analytics_supplier_performance (supplier_id, supplier_part, otif_pct, avg_delay_days) VALUES ('SUP-42', 'QZ-7710/UK', 71.0, 3.4);
DROP TABLE IF EXISTS analytics_product_demand;
CREATE TABLE analytics_product_demand (
  product_sku varchar(32) NOT NULL,
  warehouse_code varchar(12) NOT NULL,
  weekly_demand integer NOT NULL,
  snapshot_date date NOT NULL,
  PRIMARY KEY (product_sku, warehouse_code)
);
INSERT INTO analytics_product_demand (product_sku, warehouse_code, weekly_demand, snapshot_date) VALUES ('ACM-7710-X', 'BHX', 155, '2026-08-03');
DROP TABLE IF EXISTS analytics_account_health;
CREATE TABLE analytics_account_health (
  company_no varchar(16) NOT NULL,
  health_score numeric(5,2) NOT NULL,
  calculated_at timestamp with time zone NOT NULL,
  explanation text,
  PRIMARY KEY (company_no)
);
INSERT INTO analytics_account_health (company_no, health_score, calculated_at, explanation) VALUES ('08392811', 63.0, '2026-08-07T02:00:00Z', 'Operational delays elevated; payment profile acceptable.');
COMMIT;
