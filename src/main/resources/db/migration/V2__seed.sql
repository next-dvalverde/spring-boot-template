-- V2: datos de partida para los 7 escenarios E2E de PLAN.md §6.
-- UUIDs con prefijo por tabla (1=bank, 2=customer, 3=account, 4=card, 5=atm,
-- 6=fee_policy) para que los tests se lean sin tabla de correspondencias.
-- Los PIN van en BCrypt; el valor en claro se documenta aqui porque este seed
-- es la fixture de los E2E, no un dato de produccion.

INSERT INTO bank (id, code, name) VALUES
    ('10000000-0000-0000-0000-000000000001', '1500', 'NextDigital Bank'),
    ('10000000-0000-0000-0000-000000000002', '2905', 'Otro Banco');

INSERT INTO customer (id, name, document_id) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Alice Garcia', '11111111A'),
    ('20000000-0000-0000-0000-000000000002', 'Bob Lopez', '22222222B');

-- Alice tiene dos cuentas (escenario 7: el titular puede consultar cualquiera
-- de las suyas); la de Bob es la ajena que debe responder 404, y el destino de
-- la transferencia interna del escenario 6.
INSERT INTO account (id, iban, customer_id, bank_id, balance, currency) VALUES
    ('30000000-0000-0000-0000-000000000001', 'ES0515000001000000000001',
     '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 1000.00, 'EUR'),
    ('30000000-0000-0000-0000-000000000002', 'ES7515000001000000000002',
     '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 500.00, 'EUR'),
    ('30000000-0000-0000-0000-000000000003', 'ES4815000001000000000003',
     '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 250.00, 'EUR');

-- PIN en claro (solo fixture): 4001...0001 -> 1975 · 4001...0002 -> 2846 ·
-- 4001...0003 -> 4821 (el "PIN del sobre" de la tarjeta sin activar, escenario 1).
INSERT INTO card (id, card_number, account_id, type, status, pin_hash, pin_change_required,
                  withdrawal_limit, credit_limit, credit_used, failed_attempts) VALUES
    ('40000000-0000-0000-0000-000000000001', '4001000000000001',
     '30000000-0000-0000-0000-000000000001', 'DEBIT', 'ACTIVE',
     '$2a$10$4PKGQi47bYJ7/2wcs9zvFOOXQUijdo1LVPbTN2dvIIGCrLmujr9i6', FALSE,
     500.00, NULL, 0, 0),
    ('40000000-0000-0000-0000-000000000002', '4001000000000002',
     '30000000-0000-0000-0000-000000000001', 'CREDIT', 'ACTIVE',
     '$2a$10$3inQmhHr/zV7lmsdlkTb8OnlMp4OfpahxPxvqOX5Xg8hZGWX17Og6', FALSE,
     500.00, 500.00, 0, 0),
    ('40000000-0000-0000-0000-000000000003', '4001000000000003',
     '30000000-0000-0000-0000-000000000002', 'DEBIT', 'INACTIVE',
     '$2a$10$tuBtu5EV71h01M95gFMkrOjwl9I7bxnkiHJ7lribGMZtcuo.19l1G', FALSE,
     500.00, NULL, 0, 0);

INSERT INTO atm (id, bank_id, location) VALUES
    ('50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'Madrid, Gran Via 1'),
    ('50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002',
     'Barcelona, Avinguda Diagonal 100');

-- percentage en puntos porcentuales: 1.0000 = 1 %. bank_code = banco que cobra:
-- Otro Banco cobra la retirada en su cajero (escenario 3); NextDigital cobra la
-- transferencia externa de sus clientes (escenario 6).
INSERT INTO fee_policy (id, bank_code, operation_type, fixed_amount, percentage) VALUES
    ('60000000-0000-0000-0000-000000000001', '2905', 'WITHDRAWAL', 1.50, 1.0000),
    ('60000000-0000-0000-0000-000000000002', '1500', 'TRANSFER', 2.50, 0.2500);
