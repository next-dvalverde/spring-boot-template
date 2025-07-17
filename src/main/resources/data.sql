-- ==========================
-- 1️⃣ Bancos
-- ==========================
INSERT INTO banco (id, nombre, comision_cajero_ajeno) VALUES
  (1, 'Banco Ejemplo', 0.02),
  (2, 'Banco Secundario', 0.015);

-- ==========================
-- 2️⃣ Cuentas
-- ==========================
INSERT INTO cuenta (iban, saldo, banco_id) VALUES
  ('ES1000000000000000000001', 5000.00, 1),
  ('ES1000000000000000000002', 2500.00, 1),
  ('ES1000000000000000000003', 10000.00, 2);

-- ==========================
-- 3️⃣ Movimientos
-- ==========================
INSERT INTO movimiento (fecha, cantidad, tipo, descripcion, cuenta_id) VALUES
  ('2025-07-01T10:00:00', 1000.00, 'INGRESO', 'Ingreso inicial', 'ES1000000000000000000001'),
  ('2025-07-05T12:30:00', -200.00, 'RETIRADA', 'Retirada en cajero', 'ES1000000000000000000001'),
  ('2025-07-10T09:15:00', -50.00, 'COMISION', 'Comisión cajero ajeno', 'ES1000000000000000000001'),

  ('2025-07-02T14:00:00', 500.00, 'INGRESO', 'Ingreso en ventanilla', 'ES1000000000000000000002'),
  ('2025-07-06T16:45:00', -100.00, 'RETIRADA', 'Retirada en cajero', 'ES1000000000000000000002'),

  ('2025-07-03T08:30:00', 3000.00, 'TRANSFERENCIA_ENTRANTE', 'Transferencia recibida', 'ES1000000000000000000003'),
  ('2025-07-07T19:20:00', -500.00, 'TRANSFERENCIA_SALIENTE', 'Pago de alquiler', 'ES1000000000000000000003');

-- ==========================
-- 4️⃣ Tarjetas
-- ==========================
INSERT INTO tarjeta (
  numero, tipo, activada, pin_hash, limite_retirada,
  maximo_credito, credito_usado,
  cuenta_id, banco_id
) VALUES
  -- Tarjeta DEBITO asociada a cuenta 1
  ('4111111111111111', 'DEBITO', TRUE,
   '$2a$10$7Q2lFyf7s1nxIMr6CzKX3OWHqKpCFUdC6QehFe/hCX3Ys3UuP6N5m',
   1000.00, NULL, NULL,
   'ES1000000000000000000001', 1),

  -- Tarjeta CREDITO asociada a cuenta 2
  ('5500000000000004', 'CREDITO', TRUE,
   '$2a$10$7Q2lFyf7s1nxIMr6CzKX3OWHqKpCFUdC6QehFe/hCX3Ys3UuP6N5m',
   2000.00, 3000.00, 1500.00,
   'ES1000000000000000000002', 1),

  -- Otra tarjeta DEBITO asociada a cuenta 3 (Banco Secundario)
  ('4000000000000002', 'DEBITO', FALSE,
   '$2a$10$7Q2lFyf7s1nxIMr6CzKX3OWHqKpCFUdC6QehFe/hCX3Ys3UuP6N5m',
   500.00, NULL, NULL,
   'ES1000000000000000000003', 2);
