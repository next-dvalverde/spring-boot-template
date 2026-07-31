# API de Cajeros — NextDigital (prueba técnica)

API REST que modela las operaciones de un cajero automático: activación de tarjeta, cambio de PIN,
retirada, ingreso, transferencia y consulta de movimientos, con distinción real entre tarjeta de
débito y de crédito.

- Plan de trabajo y reglas de negocio: [`PLAN.md`](PLAN.md)
- Registro de decisiones y deuda técnica: [`DECISIONS.md`](DECISIONS.md) — **vale tanto como el código**

## Stack

Java 21 · Spring Boot 3.5 · Maven · PostgreSQL · Flyway · Spring Data JPA · BCrypt ·
JUnit 5 + MockMvc + Testcontainers · springdoc-openapi

## Puesta en marcha

```bash
docker compose up --build      # Postgres + API (imagen multi-stage) en :8080
```

Para desarrollo:

```bash
docker compose up -d postgres  # Postgres en :5433 del host
./mvnw spring-boot:run         # app en :8080
```

Swagger UI en `/swagger-ui.html`. Para probar sin escribir peticiones, importa
[`postman/ATM-API.postman_collection.json`](postman/ATM-API.postman_collection.json): trae las
tarjetas del seed como variables y la petición de sesión guarda el token automáticamente.

## Tests

```bash
./mvnw test              # levanta su propio Postgres 16 vía Testcontainers
./mvnw -q verify         # build completo
```

Requieren un demonio Docker corriendo. No dependen del `docker compose`: los E2E corren contra el
schema real de Flyway sobre la misma imagen de Postgres, sin H2 y sin mocks de repositorios.
La suite son 85 tests: unitarios de dominio (milisegundos, sin Spring) y los 7 escenarios E2E de
`PLAN.md` §6, cada uno afirmando las dos invariantes de dinero tras cada operación:

```
balance     == balance_inicial + Σ (signOf(type) · amount)   donde settles_account = TRUE
creditUsed  ==                − Σ (signOf(type) · amount)    donde settles_account = FALSE y card_id = ?
```

## Modelo de dominio

```
Customer 1─* Account 1─* Card      Bank 1─* Account
Account  1─* Movement              Bank 1─* Atm        FeePolicy (bank_code, operation_type)
```

Las reglas que importan, y dónde viven:

- **`domain/` es puro**: `FeeCalculator`, `WithdrawalPolicy`, `TransferPolicy`, `AmountPolicy`,
  `PinPolicy`, `IbanValidator` no tienen Spring ni I/O — reciben datos, devuelven decisiones.
  El servicio carga la entidad `FeePolicy` y pasa un `FeeRule` como argumento.
- **Dinero en `BigDecimal`**, escala 2, `HALF_UP`. `Movement.amount` es siempre positivo: el signo
  lo da el tipo vía `signOf(MovementType)`.
- **La comisión es un movimiento `FEE`**, enlazado con su operación por `operationId` — nunca un
  descuento silencioso.
- **Crédito ≠ débito**: una disposición de crédito no toca `account.balance`; sube
  `card.creditUsed` y sus apuntes llevan `settles_account = FALSE` y `balance_after` NULL.
- **PIN con BCrypt** y bloqueo al 3.er fallo (contador compartido con la activación: el único
  endpoint sin token no puede ser un oráculo de PIN). Un acierto resetea el contador.
- **Bloqueo optimista** (`@Version` en `Account` **y** `Card`) → `409 CONCURRENT_MODIFICATION`.
- La cuenta de toda operación de dinero **sale del token de sesión**, nunca del request: operar
  sobre una cuenta ajena es imposible por construcción. Consultar la cuenta de otro titular
  responde `404`, no `403` — un `403` confirmaría que existe.

## Endpoints

```
POST   /api/v1/sessions                 { cardNumber, pin, atmId }  → { token, pinChangeRequired }
POST   /api/v1/cards/activation         { cardNumber, pin, atmId }  → 200
PUT    /api/v1/cards/pin                { currentPin, newPin }      → 204     [token]
GET    /api/v1/accounts                                             → cuentas del titular  [token]
GET    /api/v1/accounts/{id}/movements  ?page&size                  → movimientos          [token]
POST   /api/v1/operations/withdrawals   { amount }                  → recibo               [token]
POST   /api/v1/operations/deposits      { amount }                  → recibo               [token]
POST   /api/v1/operations/transfers     { targetIban, amount, concept } → recibo           [token]
```

El token es un UUID opaco con TTL de 120 s y viaja en `Authorization: Bearer <token>`. Los errores
responden `{ code, message, timestamp }` con los códigos estables del catálogo de
[`CLAUDE.md`](CLAUDE.md) § "Convenciones de código".

### Datos de prueba (V2__seed.sql)

| Tarjeta | Tipo | Estado | PIN | Cuenta |
|---|---|---|---|---|
| `4001000000000001` | DEBIT | ACTIVE | `1975` | `ES05…0001` (Alice, 1000 €) |
| `4001000000000002` | CREDIT (límite 500 €) | ACTIVE | `2846` | `ES05…0001` (Alice) |
| `4001000000000003` | DEBIT | INACTIVE | `4821` (sobre) | `ES75…0002` (Alice, 500 €) |

Cajeros: `50000000-…-0001` (NextDigital) y `50000000-…-0002` (Otro Banco, cobra comisión de
retirada). Los PIN están sembrados en BCrypt; aquí van en claro porque son la fixture de los tests.

## Fuera de alcance

Cada recorte lleva su **semilla en el schema** y su plan B fechado en [`DECISIONS.md`](DECISIONS.md):

| Fuera de alcance | Semilla puesta hoy | Detalle |
|---|---|---|
| Idempotencia de reintentos | `movement.operation_id` agrupa las líneas de una operación | [C-2] |
| Reintento ante conflicto optimista | toda escritura de saldo pasa por un único método | [C-6] |
| Multi-divisa (todo EUR) | `account.currency` explícita desde V1 | [C-10] |
| Interbancario real (la salida no se liquida ni se devuelve) | `counterparty_iban` guardado | [C-5] |
| Límite diario acumulado (hoy es **por operación**) | `created_at` + `card_id` hacen la consulta escribible | [C-3] |
| Amortización de crédito (`creditUsed` solo sube) | invariante enunciada como suma, no contador | [C-7] |
| Doble partida (`balance_after` es saldo denormalizado) | la comisión ya es un apunte propio | [C-4] |
| Versionado de tarifas | `fee_policy.valid_from` / `valid_to` creadas | [C-8] |
| Pipeline de CI | `./mvnw -q verify` corre entero sin montaje manual | [C-9] |
| Escalado horizontal (sesiones en memoria) | interfaz `SessionStore` de 2 métodos | [C-1] |
