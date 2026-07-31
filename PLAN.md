# Plan — API de Cajeros (NextDigital, prueba técnica back)

Presupuesto: **3 h**. Stack: Spring Boot 3 + Java 21, PostgreSQL + docker-compose, Flyway, JPA, BCrypt, JUnit 5 + MockMvc + Testcontainers.

> Las decisiones de este plan, su deuda técnica y el plan B de cada atajo están fechadas en
> [`DECISIONS.md`](DECISIONS.md). Las referencias `[A-n]` / `[B-n]` / `[C-n]` apuntan ahí.

---

## 1. Decisiones tomadas

| Punto | Decisión | Razón |
|---|---|---|
| Persistencia | PostgreSQL en docker-compose, Flyway para schema + seed | Cubre el criterio "Docker" del enunciado y es realista |
| Sesión | `POST /sessions` con `{cardNumber, pin, atmId}` → **token opaco UUID en memoria, TTL 120 s** `[A-6]` | El PIN viaja una sola vez; el contexto (tarjeta + cuenta + cajero) queda en el token, como un cajero real. Opaco y no JWT: un cajero necesita poder revocar la sesión |
| Auth framework | Filtro propio ligero, **no** Spring Security completo | Spring Security se come 45 min sin aportar valor evaluable aquí |
| Tests | **E2E sobre los flujos** + unitarios puros de las reglas de dominio, **sobre Testcontainers Postgres** `[A-4]` | El enunciado pide unitarios e integración; los E2E dan cobertura real, los unitarios cubren las reglas de dinero sin arrancar contexto. H2 descartado: divergiría del `V1__schema.sql` real |
| PIN | BCrypt, nunca en claro, nunca en logs ni en respuestas | Criterio explícito del cliente |
| Dinero | `BigDecimal` escala 2, `RoundingMode.HALF_UP` | Nada de `double` en un dominio financiero |

---

## 2. Modelo de dominio

```
Customer 1─* Account 1─* Card
Bank     1─* Account
Bank     1─* Atm
Account  1─* Movement
```

**Bank** — `id, code (4 chars: código de entidad), name`
**Customer** — `id, name, documentId`
**Account** — `id, iban (único), customerId, bankId, balance, currency ('EUR')ᵈ, @Version`
**Card** — `id, cardNumber (único), accountId, type {DEBIT, CREDIT}, status {INACTIVE, ACTIVE, BLOCKED}, pinHash, pinChangeRequired, withdrawalLimit, creditLimit, creditUsed, failedAttempts, @Version`
**Atm** — `id, bankId, location`
**Movement** — `id, accountId, cardId, atmIdᵈ, operationId, type {DEPOSIT, WITHDRAWAL, FEE, TRANSFER_IN, TRANSFER_OUT}, amount (>0), settlesAccount, balanceAfter (NULL si no liquida), description, counterpartyIban, createdAt`
**FeePolicy** — entidad: `bankCode (banco que cobra), operationType, fixedAmount, percentage, validFromᵈ, validToᵈ`

ᵈ = **semilla deliberada**: columna que la v1.0.0 **no lee**, puesta para que la migración
correspondiente sea un `WHERE` distinto y no un backfill arqueológico (`DECISIONS.md` Parte D).
`cardId`, `operationId` y `settlesAccount` **no llevan la marca**: la v1.0.0 sí los usa —
son los argumentos de las invariantes de §6 y del enlace retirada↔comisión.

⚠️ `code` **no es el prefijo del IBAN**. En un IBAN español (`ES91 2100 …`) el prefijo son país +
dígitos de control; el código de entidad son las posiciones **5–8**. Comparar prefijos compararía
dígitos de control. **La igualdad de bancos se resuelve siempre por `bankId`**, y solo se recurre a
extraer el código del IBAN cuando la cuenta destino no existe en nuestra base (transferencia externa).

### Notas de modelado

- **Un solo `Movement`** con enum de tipo → resuelve "que sepa de qué tipo es cada movimiento" sin endpoints separados. Un único `GET` de movimientos.
- **Convención de signo — decidida, no implícita.** `amount` es **siempre positivo** (`CHECK (amount > 0)` en el schema). La dirección la da el tipo, en una función pura del dominio:
  ```java
  static int signOf(MovementType t) {
      return switch (t) {
          case DEPOSIT, TRANSFER_IN            -> +1;
          case WITHDRAWAL, FEE, TRANSFER_OUT   -> -1;
      };
  }
  ```
  Sin esta convención escrita, las dos invariantes de §6 son incompatibles entre sí: la del balance
  necesita que una retirada reste, la de `creditUsed` necesita que sume. El `CHECK` de base de datos
  es además la última línea de defensa de `[B-2]`.
- La **comisión es un movimiento propio** (`FEE`), no un descuento silencioso dentro de la retirada. Es lo que hace un banco real y hace el extracto auditable. `operationId` la enlaza con la retirada que la originó.
- **Crédito**: `creditUsed` en la tarjeta, no en la cuenta. Una retirada con tarjeta de crédito **no toca el balance de la cuenta**, incrementa `creditUsed`. Es la diferencia conceptual clave entre débito y crédito y probablemente lo que el evaluador está mirando.
  - Sus apuntes se escriben con **`settlesAccount = false`** `[A-1]`. Sin esa marca, la invariante de §6 sería falsa por construcción para toda tarjeta de crédito.
- `@Version` en `Account` **y en `Card`** → bloqueo optimista. Responde a "¿y si dos cajeros operan a la vez?". **`Card` lo necesita igual que `Account`**: una retirada de crédito no toca `account.balance`, solo `card.creditUsed`; sin versión en `Card`, dos retiradas simultáneas pasan las dos el `creditLimit` y se pierde una actualización. Lo mismo con `failedAttempts`, que muta en cada validación de PIN.
- El conflicto se traduce a `409 CONCURRENT_MODIFICATION`, nunca a un 500 `[B-3]`. **La excepción a capturar es `org.springframework.orm.ObjectOptimisticLockingFailureException`**, no `jakarta.persistence.OptimisticLockException`: Spring Data traduce la de Hibernate y un handler sobre la de JPA no la ve.
- `FeeCalculator` **no consulta la tabla**: el servicio carga la entidad `FeePolicy` y pasa al dominio un record puro `FeeRule(fixedAmount, percentage)` `[A-2]`. Nombres distintos a propósito — entidad y value object no comparten identificador.
- `balanceAfter` es **`NULL` en los apuntes con `settlesAccount = false`**: no mueven el saldo de la cuenta, así que no hay saldo posterior que registrar. Repetir el saldo anterior sería mentir en el extracto.

---

## 3. Reglas de negocio (el núcleo evaluable)

**Regla previa a todo** (`[B-2]`, crítica): `amount > 0` en las tres operaciones de dinero. Un
ingreso negativo sería una retirada sin límite ni comisión. Tres capas: `@Positive` en el DTO,
`AmountPolicy.requirePositive()` en `domain/` — invocada por las **tres** operaciones, no solo por
la retirada — y `CHECK (amount > 0)` en el schema.

**Guardas previas a toda operación** (orden importa):
1. Tarjeta existe → si no, `404 CARD_NOT_FOUND`
2. `status == ACTIVE` → si `INACTIVE`, `409 CARD_NOT_ACTIVATED`; si `BLOCKED`, `409 CARD_BLOCKED`.
   **Son dos códigos distintos**: decir "no activada" a una tarjeta bloqueada induce al usuario a
   intentar activarla
3. `pinChangeRequired == false` → si no: `409 PIN_CHANGE_REQUIRED` (solo deja pasar `PUT /cards/pin`)
4. PIN correcto → si no, `401 INVALID_PIN`; al 3.er fallo `status = BLOCKED` y la respuesta pasa a
   ser `409 CARD_BLOCKED`. **Un PIN correcto resetea `failedAttempts` a 0** `[B-1]`

**Excepción documentada:** `POST /cards/activation` invierte la guarda 2 — exige `INACTIVE` y
rechaza `ACTIVE` con `409 CARD_ALREADY_ACTIVE`. Sí ejecuta la guarda 4 con el mismo contador `[B-4]`.
Es el único endpoint que no cumple el orden anterior, y es a propósito.

**Retirada** `amount + fee`:
- Cualquier tipo de tarjeta
- `amount <= card.withdrawalLimit` (límite **por operación**, débito y crédito por igual) → `LIMIT_EXCEEDED`
  - Acumulado diario: fuera de alcance `[C-3]`
- Débito: `account.balance >= amount + fee` → `INSUFFICIENT_FUNDS`
- Crédito: `creditUsed + amount + fee <= creditLimit` → `CREDIT_LIMIT_EXCEEDED`
- Cajero de otro banco ⇒ comisión **del banco del cajero**
- Genera `WITHDRAWAL` + (si aplica) `FEE`, ambos con el mismo `operationId` y el mismo `settlesAccount`

**Ingreso**:
- **Solo tarjeta `DEBIT`** → si `CREDIT`: `409 OPERATION_NOT_SUPPORTED_FOR_CARD_TYPE` `[B-5]`
  (un ingreso con tarjeta de crédito sería una amortización, y eso es `[C-7]`)
- `atm.bankId == account.bankId`, si no → `403 FOREIGN_ATM_DEPOSIT_NOT_ALLOWED`
- Sin comisión. Genera `DEPOSIT`

**Transferencia** (**solo tarjeta `DEBIT`**, mismo código de error que el ingreso `[B-5]`):
- IBAN destino válido: formato (estructura **ISO 13616**) + dígito de control **MOD 97-10 de ISO 7064**. Implementación propia, ~15 líneas, mejor que meter Apache Commons por una función
- Destino ≠ origen
- **Resolución del banco destino, en este orden:** (1) buscar la cuenta por IBAN en nuestra base → si existe, comparar `bankId` con el de origen; (2) si no existe, extraer el código de entidad de las posiciones 5–8 del IBAN y compararlo con `bank.code`. Nunca comparar "prefijos" de IBAN, que son país + dígitos de control
- Mismo banco ⇒ sin comisión; otro banco ⇒ comisión **del banco de la cuenta origen**
- Saldo suficiente incluyendo comisión
- Genera `TRANSFER_OUT` (+ `FEE`) en origen y, **si la cuenta destino existe en nuestro banco**, `TRANSFER_IN` en destino. Si es externa, solo se registra la salida (no tenemos el otro core bancario) `[C-5]`

**Activación**:
- Solo si `INACTIVE`. Valida PIN inicial (el "del sobre", sembrado en Flyway) → pasa a `ACTIVE` + `pinChangeRequired = true`
- **Comparte el contador `failedAttempts`** con el login: 3 intentos y la tarjeta se bloquea `[B-4]`. Sin esto el endpoint es un oráculo de PIN sin throttling, y es el único sin token

**Cambio de PIN**:
- PIN nuevo ≠ actual, formato 4 dígitos, **sin secuencias triviales ni dígitos repetidos** (`1234`, `0000`) → si no, `INVALID_PIN_FORMAT` `[B-7]`
- → `pinChangeRequired = false`

### Comisiones
Tabla `fee_policy (bank_code, operation_type, fixed_amount, percentage, valid_from, valid_to)` sembrada
por Flyway. **`bank_code` = el banco que cobra** `[A-3]`: el del cajero en una retirada, el de la
cuenta origen en una transferencia. El servicio la carga y se la pasa al `FeeCalculator` puro `[A-2]`;
si no hay política, comisión 0. Configurable sin recompilar y trivial de testear.

`valid_from` / `valid_to` se crean pero no se consultan en v1.0.0 `[C-8]`.

---

## 4. API

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

- `GET /accounts/{id}/movements`: valida que la cuenta pertenece **al titular de la tarjeta** (cualquiera de sus cuentas), no solo a la cuenta de la tarjeta. Si no le pertenece → **`404 ACCOUNT_NOT_FOUND`, no `403`**: un `403` confirmaría que esa cuenta existe, y eso es un oráculo de enumeración que contradice la propia regla de no revelar la existencia de cuentas ajenas. El cliente no distingue "no es tuya" de "no existe", que es justo lo que se quiere.
- Operaciones de dinero: **solo** la cuenta ligada a la tarjeta de la sesión. El cliente nunca manda `accountId` → imposible operar sobre cuenta ajena por diseño.
- Errores: `@RestControllerAdvice` → `{ code, message, timestamp }` con códigos de negocio estables.
- `atmId` inexistente en `POST /sessions` y en la activación → `404 ATM_NOT_FOUND` `[B-6]`.

**El catálogo canónico de códigos de error vive en `CLAUDE.md` § "Convenciones de código".** Aquí no
se replica para que no haya dos listas que diverjan.

---

## 5. Estructura

```
es.nextdigital.atm
├── config/            (beans, filtro de sesión, OpenAPI)
├── domain/            (entidades + enums + reglas puras: FeeCalculator, IbanValidator,
│                       WithdrawalPolicy, AmountPolicy, PinPolicy)
├── repository/
├── service/           (SessionService, CardService, OperationService, MovementService)
├── web/               (controllers, DTOs, GlobalExceptionHandler)
└── resources/db/migration/  (V1__schema.sql, V2__seed.sql)
```

Capas clásicas con **dominio rico**: las reglas de dinero (comisión, límites, disponible) viven en clases puras sin Spring → unitarios instantáneos. Nada de hexagonal completo: en 3 h el coste de ceremonia no se recupera.

---

## 6. Tests

**Unitarios (rápidos, sin contexto).** Se escriben **dentro del bloque de su feature**, no al final `[A-5]`:
- `IbanValidatorTest` — válidos, checksum roto, longitud, nulos
- `FeeCalculatorTest` — mismo banco 0, banco externo fijo/porcentual, redondeo HALF_UP del porcentaje
- `WithdrawalPolicyTest` — débito sin saldo, crédito dentro/fuera de límite, límite de tarjeta, **comisión que empuja por encima del saldo** (el límite se compara contra `amount`, la comisión aparte)
- `AmountPolicyTest` — cero y negativo en las tres operaciones `[B-2]`
- `PinPolicyTest` — nuevo ≠ actual, formato, secuencias triviales `[B-7]`

**E2E (`@SpringBootTest` + MockMvc + Testcontainers Postgres, contra el schema real de Flyway):**
1. Tarjeta nueva → operar sin activar ⇒ 409 → activar → operar ⇒ 409 PIN_CHANGE_REQUIRED → cambiar PIN → operar ⇒ OK
2. Retirada débito: OK, saldo actualizado, movimiento creado / sin saldo ⇒ 409 / sobre límite ⇒ 409
3. Retirada en cajero ajeno ⇒ se crean 2 movimientos (WITHDRAWAL + FEE) y el saldo cuadra
4. Retirada crédito: no toca balance, sube `creditUsed`, tope respetado, apuntes con `settlesAccount = false`
5. Ingreso en cajero propio OK / en cajero ajeno ⇒ 403 / con tarjeta de crédito ⇒ 409
6. Transferencia interna: origen y destino cuadran / IBAN inválido ⇒ 400 / externa ⇒ con comisión
7. Movimientos: devuelve todos los tipos y no deja consultar cuenta de otro titular ⇒ 404

**Aserciones transversales** — las dos, en todo E2E que mueva dinero `[A-1]`:

```
balance     == balance_inicial + Σ (signOf(type) · amount)  donde settles_account = TRUE
creditUsed  ==               − Σ (signOf(type) · amount)    donde settles_account = FALSE y card_id = ?
```

El signo sale del tipo, no de la columna (`amount` siempre positivo, ver §2). El menos de la segunda
convierte en positivo lo que es una disposición; y el día que exista amortización `[C-7]`, un apunte
de signo `+1` reduce `creditUsed` **sin tocar la fórmula**. Son las dos afirmaciones que de verdad
prueban que el dominio es correcto, y ninguna se relaja jamás para que un test pase.

---

## 7. Git

- `main` (release) ← `develop` ← `feature/*`
- Una rama por bloque: `feature/domain-model`, `feature/card-activation`, `feature/withdrawals`, `feature/deposits`, `feature/transfers`, `feature/movements`, `feature/docker-ci`
- Commits pequeños en conventional commits (`feat:`, `test:`, `chore:`)
- Merge a `develop` con `--no-ff` para que el grafo cuente la historia. Tag `v1.0.0` al final.
- Primer commit: `.gitignore` + README esqueleto. **Antes de escribir nada de código.**

---

## 8. Timeboxing

| Tramo | Bloque |
|---|---|
| 0:00–0:30 | Repo + git flow + Initializr + docker-compose + Flyway (schema **con semillas** + seed) |
| 0:30–0:50 | Sesión + activación + cambio de PIN (+ unitarios) |
| 0:50–1:20 | Retiradas (débito, crédito, límites, comisión de cajero ajeno) (+ unitarios) |
| 1:20–1:35 | Ingresos + consulta de movimientos |
| 1:35–2:05 | Transferencias + validador IBAN + comisión (+ unitarios) |
| 2:05–2:35 | **Solo E2E** — los 7 escenarios |
| 2:35–2:50 | Dockerfile multi-stage + OpenAPI |
| 2:50–3:00 | README + `DECISIONS.md` + merge + tag |

Justificación de cada ajuste y punto de corte: `DECISIONS.md` [Parte E]. Resumen: bloque 0 de 20 a
30 min, unitarios contabilizados una sola vez `[A-5]`, y **GitHub Actions fuera del alcance** `[C-9]`.

**Punto de corte declarado:** si a las 2:05 los E2E no han empezado, se sacrifican los escenarios 6
y 7. Nunca el 1 (guardas), el 3 (comisión) ni el 4 (crédito).

---

## 9. Fuera de alcance (documentado en el README)

Decir *por qué* no se hizo vale casi tanto como hacerlo. **Y dejar puesta la base para hacerlo vale
más todavía**: cada punto lleva su semilla en el schema y su coste de migración fechado en
[`DECISIONS.md`](DECISIONS.md).

| Fuera de alcance | Semilla puesta hoy | Detalle |
|---|---|---|
| **Idempotencia** — un cajero que pierde la respuesta reintenta y duplicaría la retirada | `movement.operation_id` agrupa las líneas de una operación | `[C-2]` |
| **Concurrencia** — optimista, sin reintento ni bloqueo pesimista | toda escritura de saldo pasa por un único método | `[C-6]` |
| **Multi-divisa** — todo EUR | `account.currency` explícita desde V1 | `[C-10]` |
| **Interbancario real** — la salida se registra, no se liquida | `counterparty_iban` guardado; falta `status` | `[C-5]` |
| **Auditoría** — solo movimientos, sin log separado | `movement.atm_id` (`card_id` ya lo usa la v1) | `[C-10]` |
| **Rate limiting** más allá del bloqueo a 3 fallos | el contador cubre también la activación `[B-4]` | — |
| **Límite diario acumulado** — hoy es por operación | `created_at` + `card_id` hacen la consulta escribible | `[C-3]` |
| **Amortización de crédito** — `creditUsed` solo sube | invariante enunciada como suma, no como contador | `[C-7]` |
| **Doble partida** — `balanceAfter` es un saldo denormalizado | la comisión ya es un apunte propio, no un neteo | `[C-4]` |
| **Versionado de tarifas** | `fee_policy.valid_from` / `valid_to` creadas | `[C-8]` |
| **CI** — sacado del alcance conscientemente | `./mvnw -q verify` corre entero sin montaje manual | `[C-9]` |
| **Escalado horizontal** — sesiones en memoria de proceso | interfaz `SessionStore` de 2 métodos | `[C-1]` |

El criterio con el que se aceptó cada una está al final de `DECISIONS.md`.
