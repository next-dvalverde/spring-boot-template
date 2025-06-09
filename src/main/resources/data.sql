-- Insertar datos en CLIENTE
INSERT INTO CLIENTE (id, nombre) VALUES (1, 'Ana García');

-- Insertar datos en CUENTA
INSERT INTO CUENTA (id, iban, saldo, cliente_id) VALUES (1, 'ES1000000000000000000001', 1000.00, 1);
INSERT INTO CUENTA (id, iban, saldo, cliente_id) VALUES (2, 'ES2000000000000000000002', 2500.00, 1);

-- Insertar datos en TARJETA
INSERT INTO TARJETA (id, numero, tipo, activa, pin, limite, cuenta_id, cliente_id)
VALUES (1, '1234567890123456', 'DEBITO', false, '1234', 500.00, 1, 1);

INSERT INTO TARJETA (id, numero, tipo, activa, pin, limite, cuenta_id, cliente_id)
VALUES (2, '9876543210987654', 'CREDITO', true, '5678', 1000.00, 2, 1);

-- Insertar datos en MOVIMIENTO (algunos ejemplos de movimientos asociados a las cuentas)
INSERT INTO MOVIMIENTO (fecha, cantidad, descripcion, tipo, cuenta_id) VALUES
('2025-06-01 10:00:00', 500.00, 'Ingreso en efectivo', 'INGRESO', 1),
('2025-06-02 14:30:00', -200.00, 'Retiro cajero', 'RETIRADA', 1),
('2025-06-03 09:15:00', -2.00, 'Comisión por cajero externo', 'COMISION', 1),
('2025-06-04 16:45:00', 300.00, 'Transferencia recibida', 'TRANSFERENCIA_ENTRANTE', 1),

('2025-06-01 11:00:00', 1500.00, 'Ingreso nómina', 'INGRESO', 2),
('2025-06-02 15:00:00', -100.00, 'Retiro en tienda', 'RETIRADA', 2),
('2025-06-03 12:00:00', -400.00, 'Transferencia a ES1000000000000000000001', 'TRANSFERENCIA_SALIENTE', 2),
('2025-06-04 18:00:00', -4.00, 'Comisión por transferencia externa', 'COMISION', 2);
