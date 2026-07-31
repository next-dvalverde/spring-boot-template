-- V1: schema completo de la v1.0.0.
-- Las columnas marcadas "seed [C-n]" las escribe la v1 pero no las lee: son las
-- semillas de DECISIONS.md Parte D, puestas para que cada migracion futura sea un
-- WHERE distinto y no un backfill sin informacion. No son codigo muerto.

CREATE TABLE bank (
    id      UUID PRIMARY KEY,
    -- Codigo de entidad (posiciones 5-8 del IBAN espanol). NO es el prefijo del
    -- IBAN: el prefijo son pais + digitos de control y compararlo compara ruido.
    code    CHAR(4)      NOT NULL UNIQUE,
    name    VARCHAR(100) NOT NULL
);

CREATE TABLE customer (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    document_id VARCHAR(20)  NOT NULL UNIQUE
);

CREATE TABLE account (
    id          UUID PRIMARY KEY,
    iban        VARCHAR(34)   NOT NULL UNIQUE,
    customer_id UUID          NOT NULL REFERENCES customer (id),
    bank_id     UUID          NOT NULL REFERENCES bank (id),
    balance     NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),
    -- seed [C-10]: fijar la divisa hoy evita adivinarla fila a fila el dia 400.
    currency    CHAR(3)       NOT NULL DEFAULT 'EUR',
    version     BIGINT        NOT NULL DEFAULT 0
);

CREATE TABLE card (
    id                  UUID PRIMARY KEY,
    card_number         VARCHAR(19)    NOT NULL UNIQUE,
    account_id          UUID           NOT NULL REFERENCES account (id),
    type                VARCHAR(10)    NOT NULL CHECK (type IN ('DEBIT', 'CREDIT')),
    status              VARCHAR(10)    NOT NULL CHECK (status IN ('INACTIVE', 'ACTIVE', 'BLOCKED')),
    pin_hash            VARCHAR(72)    NOT NULL,
    pin_change_required BOOLEAN        NOT NULL DEFAULT FALSE,
    withdrawal_limit    NUMERIC(19, 2) NOT NULL CHECK (withdrawal_limit > 0),
    credit_limit        NUMERIC(19, 2) CHECK (credit_limit > 0),
    credit_used         NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (credit_used >= 0),
    failed_attempts     INT            NOT NULL DEFAULT 0 CHECK (failed_attempts >= 0),
    -- @Version tambien aqui [B-8]: el camino de credito no toca account, y sin
    -- version en card dos retiradas simultaneas pasan las dos el credit_limit.
    version             BIGINT         NOT NULL DEFAULT 0,
    -- credit_limit es un atributo de tarjeta CREDIT, no un opcional generico.
    CONSTRAINT credit_limit_by_type CHECK (
        (type = 'CREDIT' AND credit_limit IS NOT NULL) OR
        (type = 'DEBIT' AND credit_limit IS NULL)
    )
);

CREATE TABLE atm (
    id       UUID PRIMARY KEY,
    bank_id  UUID         NOT NULL REFERENCES bank (id),
    location VARCHAR(200) NOT NULL
);

CREATE TABLE movement (
    id                UUID PRIMARY KEY,
    account_id        UUID           NOT NULL REFERENCES account (id),
    card_id           UUID           REFERENCES card (id),
    -- seed [C-10]: "que cajero dispenso esto" sin subsistema de auditoria.
    atm_id            UUID           REFERENCES atm (id),
    -- Agrupa todas las lineas de una operacion: la FEE dice de que retirada
    -- viene. Semilla de la idempotencia [C-2].
    operation_id      UUID           NOT NULL,
    type              VARCHAR(15)    NOT NULL CHECK (type IN
                          ('DEPOSIT', 'WITHDRAWAL', 'FEE', 'TRANSFER_IN', 'TRANSFER_OUT')),
    -- Siempre positivo [B-2]: el signo lo da el tipo via signOf(), nunca la fila.
    -- Ultima linea de defensa contra el ingreso negativo.
    amount            NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    -- FALSE en los apuntes de credito [A-1]: no liquidan contra account.balance.
    settles_account   BOOLEAN        NOT NULL DEFAULT TRUE,
    -- NULL cuando settles_account = FALSE [B-10]: repetir el saldo seria mentir.
    balance_after     NUMERIC(19, 2),
    description       VARCHAR(200),
    counterparty_iban VARCHAR(34),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT balance_after_only_when_settling CHECK (
        (settles_account AND balance_after IS NOT NULL) OR
        (NOT settles_account AND balance_after IS NULL)
    )
);

CREATE INDEX idx_movement_account_created ON movement (account_id, created_at);
CREATE INDEX idx_movement_operation ON movement (operation_id);
CREATE INDEX idx_movement_card ON movement (card_id);

CREATE TABLE fee_policy (
    id             UUID PRIMARY KEY,
    -- Banco QUE COBRA [A-3]: el del cajero en una retirada, el de la cuenta
    -- origen en una transferencia. Lo decide la capa de servicio.
    bank_code      CHAR(4)        NOT NULL,
    operation_type VARCHAR(15)    NOT NULL CHECK (operation_type IN ('WITHDRAWAL', 'TRANSFER')),
    fixed_amount   NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (fixed_amount >= 0),
    percentage     NUMERIC(7, 4)  NOT NULL DEFAULT 0 CHECK (percentage >= 0),
    -- seed [C-8]: fechar la vigencia desde V1 evita un backfill infechable.
    valid_from     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    valid_to       TIMESTAMPTZ,
    CONSTRAINT uq_fee_policy UNIQUE (bank_code, operation_type)
);
