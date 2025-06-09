-- Crear tabla CLIENTE
CREATE TABLE CLIENTE (
    id INT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

-- Crear tabla CUENTA
CREATE TABLE CUENTA (
    id INT PRIMARY KEY,
    iban VARCHAR(34) NOT NULL,
    saldo DECIMAL(15, 2) NOT NULL,
    cliente_id INT NOT NULL,
    CONSTRAINT fk_cuenta_cliente FOREIGN KEY (cliente_id) REFERENCES CLIENTE(id)
);

-- Crear tabla TARJETA
CREATE TABLE TARJETA (
    id INT PRIMARY KEY,
    numero VARCHAR(16) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    activa BOOLEAN NOT NULL,
    pin VARCHAR(4) NOT NULL,
    limite DECIMAL(15, 2) NOT NULL,
    cuenta_id INT NOT NULL,
    cliente_id INT NOT NULL,
    CONSTRAINT fk_tarjeta_cuenta FOREIGN KEY (cuenta_id) REFERENCES CUENTA(id),
    CONSTRAINT fk_tarjeta_cliente FOREIGN KEY (cliente_id) REFERENCES CLIENTE(id)
);

CREATE TABLE MOVIMIENTO (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    cantidad DECIMAL(19, 2) NOT NULL,
    descripcion VARCHAR(255),
    tipo VARCHAR(255) NOT NULL,
    cuenta_id BIGINT,
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (cuenta_id) REFERENCES CUENTA(id)
);