# DECISIONS.md — registro de decisiones y deuda técnica

**Proyecto:** API de Cajeros (NextDigital, prueba técnica)
**Fecha de redacción:** 2026-07-31
**Presupuesto:** 3 h

Este documento existe porque en una prueba con presupuesto cerrado la parte difícil no es elegir
el atajo, sino **dejar constancia de que era un atajo y de por dónde se sale de él**. Cada entrada
lleva fecha, el motivo real (casi siempre: el reloj), el plan B de producción y **el coste concreto
de migrar**.

Tres partes, tres cosas distintas:

- **[Parte A](#parte-a--contradicciones-del-plan-detectadas-y-resueltas)** — el plan se contradecía a sí mismo. No es deuda: es un arreglo, ya aplicado a
  `PLAN.md` y `CLAUDE.md`. Está aquí para que el porqué no haya que reconstruirlo del `git log`.
- **[Parte B](#parte-b--huecos-que-son-bugs-no-deuda-se-cierran-dentro-del-alcance)** — huecos que serían fallos si se dejaran. No tienen plan B porque no hay nada que
  posponer: se cierran dentro de las 3 h.
- **[Parte C](#parte-c--deuda-técnica-consciente)** — deuda de verdad. **Aquí sí, sin plan B y sin coste de migrar, la entrada no vale.**

Formato de las entradas de la parte C:

> **Contexto** → qué problema hay · **Decisión** → qué se hace hoy · **Por qué así** → el motivo
> honesto · **Plan B** → a qué se migra · **Semilla** → qué existe ya en el código/schema para que
> la migración no sea una reescritura · **Coste de migrar** → estimación real.

---

## Parte A — Contradicciones del plan, detectadas y resueltas

Estas no son deuda: eran incoherencias entre secciones de `PLAN.md` que habrían aparecido como un
test rojo o como una discusión a mitad de implementación. Se resolvieron **antes** de escribir
código, y `PLAN.md` y `CLAUDE.md` **ya reflejan la resolución** — lo que se cita abajo en pasado es
la redacción original, que ya no está en esos ficheros. Esta parte existe para que el porqué de cada
enunciado raro (`settles_account`, `signOf`, dos `@Version`) no haya que reconstruirlo del `git log`.

### A-1 · La invariante de balance era incompatible con el modelo de crédito

`PLAN.md` §2 decía que una retirada con tarjeta de crédito **no toca `account.balance`**, y §6
exigía como aserción transversal de todo E2E que **`balance == balance_inicial + Σ movimientos`**.
Ambas no podían ser ciertas a la vez: en el escenario 4 (retirada con crédito) se crea un
`WITHDRAWAL` contra la cuenta que el balance nunca refleja. El test correcto habría fallado, y la
tentación habría sido relajar la aserción — exactamente lo que `CLAUDE.md` prohíbe.

**Resolución adoptada (2026-07-31):** `Movement` gana dos columnas.

| Columna | Tipo | Para qué |
|---|---|---|
| `card_id` | `UUID NULL → card(id)` | qué tarjeta originó el apunte |
| `settles_account` | `BOOLEAN NOT NULL DEFAULT TRUE` | si el apunte liquida contra `account.balance` |

Los movimientos de crédito (`WITHDRAWAL` y su `FEE` con tarjeta `CREDIT`) se escriben con
`settles_account = false`. La invariante pasa a ser dos, enunciadas en `PLAN.md` §6 — ambas
verificables, ninguna relajada. El extracto sigue siendo auditable línea a línea, que era el objetivo
de haber modelado la comisión como movimiento propio.

**Corolario que casi se escapa:** partir la invariante en dos obliga a fijar la **convención de
signo**, porque cada mitad la necesita opuesta — la del balance quiere que una retirada reste, la de
`creditUsed` quiere que sume. Resuelto en `PLAN.md` §2: `amount` siempre positivo con `CHECK` en el
schema, y el signo derivado del tipo mediante `signOf(MovementType)` en `domain/`. Sin esa función,
A-1 solo habría movido la contradicción un nivel más abajo, donde cuesta más verla.

**Plan B (el modelo contablemente correcto):** una línea de crédito **es** una cuenta pasiva. En
producción, `Account.type ∈ {CHECKING, CREDIT_LINE}`, la tarjeta de crédito apunta a su propia
cuenta con balance negativo, y la invariante vuelve a ser una sola sin excepciones. No se hace hoy
porque obliga a duplicar el seed y a repensar `GET /accounts` (¿enseñas la línea de crédito como
una cuenta más?), y eso son 15–20 min que salen del bloque de tests.

**Coste de migrar:** una migración que crea una `account` de tipo `CREDIT_LINE` por cada tarjeta de
crédito, backfill de `creditUsed` → `balance`, y `settles_account` pasa a ser siempre `TRUE`. La
columna se queda como histórico o se elimina. ~2 h. **Sin la columna de hoy, el backfill sería
imposible** porque no habría forma de saber qué apuntes eran de crédito.

### A-2 · `FeeCalculator` no puede consultar la tabla de comisiones

`PLAN.md` §5 y `CLAUDE.md` sitúan `FeeCalculator` en `domain/`, con "cero repositorios, cero I/O".
`PLAN.md` §3 decía literalmente "`FeeCalculator` la consulta" refiriéndose a la tabla `fee_policy`.
Si el calculador inyecta un repositorio deja de ser puro y sus tests dejan de ser instantáneos —
se pierde justo lo que justificaba la separación de capas.

**Resolución (2026-07-31):** la entidad JPA `FeePolicy` se lee en `service/`, que la traduce a un
record inmutable **`FeeRule(fixedAmount, percentage)`** y lo **pasa como argumento** al dominio:

```java
// domain/, sin Spring, sin I/O
BigDecimal fee = FeeCalculator.forWithdrawal(atmBankId, accountBankId, amount, rule);
```

Nombres distintos a propósito: `FeePolicy` es la fila, `FeeRule` es el valor. Llamar igual a las dos
las pone en colisión dentro del mismo paquete y borra justo la frontera que esta decisión establece.
El dominio recibe datos y devuelve decisiones; `FeeCalculatorTest` construye sus `FeeRule` a mano y
no arranca contexto.

### A-3 · `fee_policy.bank_code` significaba dos cosas distintas

En una retirada, quien cobra la comisión es **el banco del cajero** (es quien presta el servicio).
En una transferencia, quien cobra es **el banco de la cuenta origen**. Con una sola columna
`bank_code` sin semántica declarada, el primer bug de comisión mal cobrada es cuestión de tiempo.

**Resolución (2026-07-31):** `bank_code` se documenta como **"banco que cobra"**, y es la capa de
servicio quien decide cuál es según la operación. La tabla vive en `PLAN.md` §3.

**Segundo error, del mismo párrafo:** la versión original resolvía "mismo banco" en la transferencia
comparando **prefijos de IBAN**. El prefijo de un IBAN son el país y los dígitos de control (`ES91`);
el código de entidad está en las posiciones **5–8**. Comparar prefijos habría dado "mismo banco" para
cualquier par de cuentas españolas con el mismo dígito de control, y "distinto banco" para dos
cuentas del mismo banco con distinto DC. La identidad de banco se resuelve ahora por `bankId`, y solo
se extrae del IBAN cuando la cuenta destino no está en nuestra base (`PLAN.md` §3, transferencia).

### A-4 · Testcontainers vs. H2 vs. "el schema real"

La cabecera de `PLAN.md` decía "Testcontainers/H2", §6 decía "contra el schema real" y `CLAUDE.md`
"base de datos real, datos de `V2__seed.sql`". H2 en modo Postgres obliga a escribir las
migraciones en el mínimo común denominador de SQL y produce divergencia silenciosa con producción:
el `V1__schema.sql` que pasa los tests deja de ser el que corre en el servidor.

**Resolución (2026-07-31):** **Testcontainers con la misma imagen de Postgres del `docker-compose`**,
un único contenedor reutilizado para toda la suite (`@ServiceConnection` + contenedor `static`).
Cuesta ~20 s de arranque una vez y garantiza que Flyway se ejecuta contra el motor de verdad.

**Deuda que genera:** el CI necesita Docker en el runner. Ver [C-9](#c-9--sin-pipeline-de-ci).

### A-5 · El bloque de tests estaba contabilizado dos veces

`CLAUDE.md` dice que cada `feature/*` se mergea con sus tests en verde. `PLAN.md` §8 reservaba además
35 min al final "para tests". O los tests van con cada feature, o van al final; no las dos cosas
dentro de 3 h.

**Resolución (2026-07-31):** **los unitarios de `domain/` van dentro del bloque de su feature** (son
los que justifican la clase pura: se escriben en 5 min y corren en milisegundos). El bloque de
2:05–2:35 es **exclusivamente para los 7 E2E**, que necesitan el sistema entero en pie y por eso no
pueden ir antes. Ver [Parte E](#parte-e--timeboxing-revisado).

### A-6 · El token de sesión estaba sin decidir

§1 decía "JWT o UUID en memoria". Era la única fila de la tabla "Decisiones tomadas" que no estaba
tomada, y afecta al filtro, a los tests y al README.

**Resolución (2026-07-31):** **UUID opaco en un `ConcurrentHashMap` con TTL de 120 s.** Un JWT aquí
sería peor: no se puede revocar (y un cajero necesita invalidar la sesión al retirar la tarjeta),
obliga a gestionar una clave de firma, y el payload viajaría con datos de la tarjeta. El token
opaco no dice nada de sí mismo, que es justo lo que se quiere. Ver [C-1](#c-1--sesiones-en-memoria-de-proceso).

### A-7 · El template de la empresa no cumplía el stack del plan

El repositorio base entregado por NextDigital (`spring-boot-template`) venía con
`spring-boot-starter-parent 3.1.0` y `<java.version>17</java.version>`, mientras `PLAN.md` §Stack
fija **Java 21 + Spring Boot 3**. No es un detalle cosmético: Boot 3.1.0 es de mayo de 2023,
anterior al GA de Java 21, y el soporte oficial de Java 21 no llega hasta Boot **3.2**. Compilar a
21 sobre 3.1 arrastra un ByteBuddy que no reconoce el bytecode 65 — el riesgo se materializaría
como una suite de tests roja en el tramo 2:05–2:35, que es exactamente donde no hay margen.

Además faltaban dependencias que el plan da por supuestas, una de ellas silenciosa:
`spring-boot-starter-web` **no incluye implementador de Bean Validation**, así que los `@Valid` /
`@Positive` de los DTOs no se habrían evaluado y la primera capa de defensa de [B-2](#parte-b--huecos-que-son-bugs-no-deuda-se-cierran-dentro-del-alcance)
habría sido decorativa sin dar ningún error.

**Resolución (2026-07-31):** parent a **3.5.16** — última línea 3.x con soporte (3.3 quedó EOL en
2026, y Boot 4 se descarta porque contradice el "Boot 3" del plan y metería riesgo de compatibilidad
con springdoc y Testcontainers en una prueba de 3 h). `java.version` a 21. Se añaden
`spring-boot-starter-validation`, `flyway-core` + `flyway-database-postgresql` (desde Flyway 10 el
soporte de cada motor va en módulo aparte), el driver `postgresql`, `spring-security-crypto`
suelto, `springdoc-openapi-starter-webmvc-ui` y los artefactos de Testcontainers. Se retira
`spring-boot-devtools`: está fuera de la lista del stack y sus reinicios en caliente estorban en
los E2E.

Se conserva el `groupId` **`es.nextdigital`** del template y solo se renombra el artefacto y el
paquete base `demo → atm`, es decir **`es.nextdigital.atm`**. `PLAN.md` §5 y `CLAUDE.md` decían
`com.nextdigital.atm`; se ajustan esos dos ficheros, porque el groupId que fija la empresa pesa más
que el ejemplo del plan.

**Coste de revertir:** nulo mientras no haya código. Por eso se hizo en el bloque 0 y no después.

### A-8 · El repositorio se checkouteaba en CRLF sobre blobs en LF

El template no traía `.gitattributes`. Los blobs commiteados están en LF y el working tree en
Windows estaba en CRLF, de modo que `git status` marcaba los 9 ficheros como modificados (658
líneas de puro ruido) y el primer commit real habría enterrado el cambio de verdad. Peor: un `mvnw`
con CRLF falla dentro del Dockerfile multi-stage del tramo 2:35 con `bad interpreter`, un fallo que
no se reproduce en local y cuesta media hora encontrar.

**Resolución (2026-07-31):** `.gitattributes` con `* text=auto eol=lf`, `mvnw` y `*.sh` forzados a
LF, `*.cmd` / `*.bat` a CRLF, y el working tree normalizado antes del primer commit.

---

## Parte B — Huecos que son bugs, no deuda: se cierran dentro del alcance

Ninguno de estos aparece en `PLAN.md` y todos son baratos. No documentarlos como "fuera de alcance"
sería esconder un fallo detrás de una excusa.

| # | Hueco | Riesgo si no se cierra | Cierre |
|---|---|---|---|
| B-1 | `failedAttempts` no se resetea tras un PIN correcto | La tarjeta se bloquea sola tras 3 errores repartidos en meses | `failedAttempts = 0` en cada validación correcta. 1 línea |
| B-2 | Ninguna regla exige `amount > 0` | Un ingreso negativo es una retirada sin límites ni comisión. **Crítico** | `AmountPolicy` en `domain/`, invocada por las **tres** operaciones (no solo la retirada: no hay `DepositPolicy`, y dejar la única defensa en el `@Positive` del DTO pondría la regla en la capa que `CLAUDE.md` define como "nunca lógica") + `CHECK (amount > 0)` en el schema |
| B-3 | Conflicto de bloqueo optimista sin mapear | El bloqueo optimista, que es la respuesta a "¿y si dos cajeros a la vez?", devuelve un 500 con stacktrace | `409 CONCURRENT_MODIFICATION` en el advice, capturando **`ObjectOptimisticLockingFailureException`** de Spring. Un handler sobre la `OptimisticLockException` de JPA no se dispara nunca: Spring Data ya la ha traducido |
| B-4 | `POST /cards/activation` es un oráculo de PIN sin token ni contador | Fuerza bruta sobre el PIN inicial sin bloquear la tarjeta | La activación usa **el mismo** contador `failedAttempts` y bloquea a los 3 intentos |
| B-5 | No está definido qué hace una tarjeta `CREDIT` en ingreso ni en transferencia | Comportamiento indefinido en 2 de los 3 endpoints de dinero | **Ingresos y transferencias son solo `DEBIT`** → `409 OPERATION_NOT_SUPPORTED_FOR_CARD_TYPE`. Un ingreso con tarjeta de crédito sería una amortización, y eso está fuera de alcance ([C-7](#c-7--el-crédito-no-se-amortiza)) |
| B-6 | `atmId` inexistente sin código de error | `NullPointerException` → 500 | `404 ATM_NOT_FOUND` en `POST /sessions` **y en la activación**, que también lo recibe |
| B-7 | PIN nuevo solo se valida como "≠ actual, 4 dígitos" | `0000` y `1234` pasan | Rechazar secuencias triviales y dígitos repetidos → `INVALID_PIN_FORMAT`. ~4 líneas, test unitario |
| B-8 | `@Version` solo en `Account` | **El camino de crédito no toca `Account`.** Dos retiradas de crédito simultáneas pasan las dos el `creditLimit` y se pierde una actualización — el bloqueo optimista no cubre justo el caso que el plan presume cubierto. Igual con `failedAttempts` | `@Version` **también en `Card`**. Una línea |
| B-9 | `403` al consultar la cuenta de otro titular | Es un oráculo de enumeración: distinguir `403` de `404` confirma qué IDs existen. Contradice la regla del propio `CLAUDE.md` de no revelar la existencia de cuentas ajenas | **`404 ACCOUNT_NOT_FOUND`** en ambos casos. El E2E 7 asserta `404` |
| B-10 | `balanceAfter` indefinido en los apuntes que no liquidan | Repetir el saldo anterior en una línea de crédito es mentir en el extracto | `NULL` cuando `settles_account = false` |
| B-11 | Guarda 1 mezclaba "no existe", "inactiva" y "bloqueada" | Una tarjeta bloqueada respondía `CARD_NOT_ACTIVATED`, invitando al usuario a activarla | Cuatro guardas y códigos separados: `CARD_NOT_FOUND`, `CARD_NOT_ACTIVATED`, `CARD_BLOCKED`, `CARD_ALREADY_ACTIVE` |

**Opcional si sobra reloj (realismo de cajero, no corrección):** exigir que la retirada sea múltiplo
de 10 → `INVALID_AMOUNT`, ya en el catálogo. Es lo que hace un cajero real y cuesta 2 líneas, pero
añade un test. Se hace solo si el bloque de transferencias cierra antes de 2:05.

---

## Parte C — Deuda técnica consciente

Todo lo de esta parte es una decisión de la que **se sale sin reescribir**, porque la base ya está
puesta. Ese es el criterio para haberla aceptado.

### C-1 · Sesiones en memoria de proceso

- **Fecha:** 2026-07-31 · **Estado:** aceptada
- **Contexto:** el token de sesión hay que guardarlo en algún sitio.
- **Decisión:** `ConcurrentHashMap<String, SessionContext>` con TTL de 120 s y limpieza perezosa.
- **Por qué así:** un `SessionStore` real (Redis) mete infraestructura, un contenedor más en el
  compose y configuración de conexión. 25 min que salen de las reglas de negocio, que es lo que
  se evalúa.
- **Plan B:** Redis con TTL nativo, o token firmado de vida ultracorta si se acepta no poder revocar.
- **Semilla:** el `Map` está detrás de una interfaz `SessionStore` con dos métodos (`issue`,
  `resolve`). El filtro y los servicios solo conocen la interfaz.
- **Coste de migrar:** una implementación nueva de `SessionStore` y un bean. ~30 min. Cero cambios
  en servicios, controllers ni tests de dominio.
- **Consecuencia hoy, dicha en voz alta:** la API **no es horizontalmente escalable**. Dos réplicas
  no comparten sesiones. Es aceptable en una prueba; no lo sería en producción.

### C-2 · Idempotencia: la semilla está, el mecanismo no

- **Fecha:** 2026-07-31 · **Estado:** aceptada (ya estaba en §9, aquí se le pone base)
- **Contexto:** un cajero que pierde la respuesta HTTP reintenta. Sin idempotencia, duplica la
  retirada. Es el fallo más caro que puede tener esta API.
- **Decisión:** no se implementa. **Pero** cada operación de dinero genera un `operation_id UUID`
  que se escribe en **todas** las líneas que produce (`WITHDRAWAL` + `FEE` comparten el mismo).
- **Por qué así:** el mecanismo completo (cabecera `Idempotency-Key`, tabla de claves, respuesta
  cacheada, expiración) es una feature entera, ~45 min con sus tests.
- **Plan B:** cabecera `Idempotency-Key` obligatoria en los tres endpoints de dinero + índice único
  `(card_id, idempotency_key)` + almacenar la respuesta para devolverla idéntica en el reintento.
- **Semilla:** `movement.operation_id`. Además de habilitar la migración, **arregla hoy un problema
  real de modelado**: sin él, `WITHDRAWAL` y su `FEE` son dos filas sin ninguna relación entre sí y
  el extracto no puede agrupar "esta comisión es de esta retirada".
- **Coste de migrar:** una migración con la columna nueva y su índice, más un interceptor. ~1 h.

### C-3 · `withdrawalLimit` es por operación, no diario

- **Fecha:** 2026-07-31 · **Estado:** aceptada
- **Contexto:** `PLAN.md` §3 dice `amount <= card.withdrawalLimit`. Un límite real de tarjeta es
  **acumulado diario**: 10 retiradas de 300 € no deberían pasar un límite de 500 €/día.
- **Decisión:** límite por operación. Se documenta explícitamente en el README para que no se lea
  como un descuido.
- **Por qué así:** el acumulado exige una consulta agregada sobre `movement` por ventana temporal,
  decidir la zona horaria de corte del día, y tratarlo dentro de la misma transacción para que dos
  cajeros simultáneos no se lo salten. Es media hora y abre la caja de la concurrencia.
- **Plan B:** `SUM(amount) WHERE card_id = ? AND type = 'WITHDRAWAL' AND created_at >= :inicio_del_día`
  dentro de la transacción, con `card.dailyWithdrawalLimit` separado del límite por operación.
- **Semilla:** `movement.created_at` y `movement.card_id` ya existen — la consulta es escribible el
  día que se quiera, sin tocar el schema. `WithdrawalPolicy` recibe hoy el importe y el límite; el
  día de mañana recibe además el acumulado, y **la firma cambia en un sitio**.
- **Coste de migrar:** ~40 min con tests.

### C-4 · Sin doble partida: `balanceAfter` es un saldo denormalizado

- **Fecha:** 2026-07-31 · **Estado:** aceptada
- **Contexto:** un core bancario real lleva un libro de doble entrada (todo apunte tiene contrapartida
  y la suma global es cero). Aquí `Movement` guarda `balanceAfter`, un saldo calculado en el momento
  de la escritura.
- **Decisión:** saldo denormalizado.
- **Por qué así:** la doble partida obliga a modelar cuentas de contrapartida (efectivo del cajero,
  ingresos por comisiones, cuenta puente interbancaria). Es el modelo correcto y es un día de trabajo.
- **Riesgo asumido:** `balanceAfter` deja de ser cierto si algún día se insertan movimientos con
  fecha retroactiva. Hoy es imposible porque solo se escribe en orden, dentro de transacción.
- **Plan B:** ledger de doble entrada con `journal_entry` / `journal_line`, y `account.balance` como
  proyección.
- **Semilla:** el sistema **ya no descuenta la comisión en silencio**: la comisión es un apunte
  propio. Ese es exactamente el hábito que la doble partida exige, y es lo caro de retrofitear en
  sistemas que descuentan neto.
- **Coste de migrar:** reescritura del módulo de movimientos, ~1 semana. Es la deuda más cara de la
  lista y es la correcta de asumir en una prueba de 3 h.

### C-5 · Transferencia saliente sin liquidación

- **Fecha:** 2026-07-31 · **Estado:** aceptada (ya en §9)
- **Contexto:** si el IBAN destino no es de nuestro banco, no hay contra quién liquidar.
- **Decisión:** se registra el `TRANSFER_OUT` y se descuenta. La transferencia se da por buena.
- **Por qué así:** no hay core externo ni SEPA que simular, y simularlo con un mock no demuestra nada.
- **Plan B:** patrón **outbox** — el `TRANSFER_OUT` se escribe en la misma transacción que un evento
  `outbox_event`, un publicador lo envía a la cola SEPA, y el estado del movimiento avanza
  `PENDING → SETTLED | RETURNED`. Una devolución genera un apunte compensatorio, nunca un borrado.
- **Semilla:** `movement.counterparty_iban` ya guarda el destino, que es el dato que el evento
  necesita. Falta añadir `movement.status`, hoy implícitamente `SETTLED`.
- **Coste de migrar:** columna `status` + tabla `outbox_event` + publicador. ~1 día.
- **Consecuencia hoy:** una transferencia externa **no se puede devolver**. Dicho en el README.

### C-6 · Bloqueo optimista sin reintento

- **Fecha:** 2026-07-31 · **Estado:** aceptada
- **Contexto:** `@Version` en `Account` detecta la escritura concurrente, pero no la resuelve: el
  segundo cajero recibe un error.
- **Decisión:** detectar y responder `409 CONCURRENT_MODIFICATION` ([B-3](#parte-b--huecos-que-son-bugs-no-deuda-se-cierran-dentro-del-alcance)). El cliente reintenta.
- **Por qué así:** reintentar en servidor sin idempotencia es peligroso — un reintento sobre una
  operación que sí se aplicó duplica dinero. **El reintento automático es una feature que depende de
  [C-2](#c-2--idempotencia-la-semilla-está-el-mecanismo-no), no de este bloque.** Hacerlo antes sería el orden equivocado.
- **Plan B:** `SELECT ... FOR UPDATE` sobre la cuenta (pesimista, correcto para un dominio con alta
  contención sobre pocas filas) o serialización por cuenta mediante cola.
- **Semilla:** toda mutación de saldo **y de `creditUsed`** pasa por un único método de servicio
  ([B-8](#parte-b--huecos-que-son-bugs-no-deuda-se-cierran-dentro-del-alcance) puso la versión en `Card` precisamente porque el camino de crédito no toca `Account`).
  El bloqueo se cambia en un sitio.
- **Coste de migrar:** ~20 min para el pesimista.

### C-7 · El crédito no se amortiza

- **Fecha:** 2026-07-31 · **Estado:** aceptada
- **Contexto:** `creditUsed` solo sube. No hay forma de devolver el dinero dispuesto.
- **Decisión:** no se implementa la amortización.
- **Por qué así:** una amortización real necesita ciclo de facturación, fecha de corte, intereses y
  cuota mínima. Es un subdominio entero.
- **Plan B:** un ingreso con tarjeta de crédito se interpreta como amortización → movimiento
  `CREDIT_REPAYMENT` con `settles_account = false` y signo contrario. El ciclo de facturación es un
  agregado aparte.
- **Semilla:** la invariante de crédito de [A-1](#a-1--la-invariante-de-balance-era-incompatible-con-el-modelo-de-crédito) ya está enunciada como suma de movimientos, no como
  contador. El día que haya apuntes negativos, **sigue cuadrando sin tocar nada**.
- **Coste de migrar:** el apunte de amortización, ~30 min. El ciclo de facturación, semanas.

### C-8 · Tarifas sin versionado temporal

- **Fecha:** 2026-07-31 · **Estado:** aceptada
- **Contexto:** `fee_policy` guarda la comisión vigente. Si un banco la cambia, se pierde la que
  se aplicó a los movimientos antiguos y el extracto histórico deja de ser reproducible.
- **Decisión:** una fila por banco y operación, sin historial.
- **Por qué así:** el versionado obliga a resolver la política por fecha en cada cálculo.
- **Plan B:** `valid_from` / `valid_to` y resolución por `created_at` de la operación.
- **Semilla:** **las columnas `valid_from` (`NOT NULL DEFAULT now()`) y `valid_to` (`NULL`) se crean
  ya en `V1__schema.sql`**, aunque hoy nadie las consulte. Cuestan dos líneas y evitan una migración
  con backfill sobre una tabla que para entonces ya tendrá historia que nadie sabrá fechar.
- **Coste de migrar:** cambiar el `WHERE` del repositorio. ~15 min. Sin migración de datos.

### C-9 · Sin pipeline de CI

- **Fecha:** 2026-07-31 · **Estado:** aceptada — **cambio respecto a `PLAN.md` §8**
- **Contexto:** el plan colocaba GitHub Actions en el tramo 2:55–3:00 "si queda hueco".
- **Decisión:** **se saca del alcance y se documenta.** Cinco minutos no dan para un workflow con
  Docker en el runner (necesario por [A-4](#a-4--testcontainers-vs-h2-vs-el-schema-real)), y un CI en rojo en el repo entregado es peor
  señal que no tenerlo.
- **Por qué así:** prefiero un recorte declarado a un intento a medias. Es la regla de oro del
  timeboxing de `CLAUDE.md`.
- **Plan B:** workflow con `services: postgres` o Testcontainers sobre el runner de Ubuntu (que trae
  Docker), cacheando `~/.m2`.
- **Semilla:** el `./mvnw -q verify` corre entero en local sin nada montado a mano, que es la única
  precondición real de un CI. Portarlo es copiar 20 líneas de YAML.
- **Coste de migrar:** ~20 min.

### C-10 · Multi-divisa, auditoría y trazabilidad

- **Fecha:** 2026-07-31 · **Estado:** aceptada (ya en §9, aquí con base)
- **Decisión:** todo en EUR; sin log de auditoría separado.
- **Semillas puestas hoy** (ver [Parte D](#parte-d--semillas-de-schema-el-resumen)): `account.currency` con default `'EUR'`, y
  `movement.atm_id`, que responde a "¿qué cajero dispensó este dinero?" sin necesidad de un
  subsistema de auditoría.
- **Plan B multi-divisa:** el importe pasa a ser un value object `Money(amount, currency)` y aparece
  una tabla de tipos de cambio. **Es la migración más invasiva de la lista** si no existe la columna
  desde el día 1: hay que decidir la divisa de cada fila histórica sin información. De ahí que la
  columna esté puesta ya.
- **Coste de migrar:** con la columna, ~1 día. Sin ella, arqueología.

---

## Parte D — Semillas de schema: el resumen

Es el centro de este documento.

Siete columnas que convierten cada punto de "fuera de alcance" en una migración de horas en vez de
una reescritura. **Están en dos grupos distintos y conviene no confundirlos**, porque solo uno es
gratis:

**Grupo 1 — las que la v1.0.0 sí usa.** No son "semillas gratis": cuestan código de aplicación. Se
pagan solas porque sin ellas ni siquiera se pueden enunciar las invariantes de
[A-1](#a-1--la-invariante-de-balance-era-incompatible-con-el-modelo-de-crédito).

| Columna | Para qué sirve **hoy** | Habilita **mañana** |
|---|---|---|
| `movement.card_id` | filtro de la invariante de `creditUsed` | [C-3](#c-3--withdrawallimit-es-por-operación-no-diario) (límite diario acumulado) |
| `movement.settles_account` | separa apuntes de cuenta y de crédito | [C-7](#c-7--el-crédito-no-se-amortiza) (amortización) |
| `movement.operation_id` | enlaza la `FEE` con la retirada que la originó | [C-2](#c-2--idempotencia-la-semilla-está-el-mecanismo-no) (idempotencia) |

**Grupo 2 — las que la v1.0.0 escribe pero nunca lee.** Estas sí son gratis: cuatro líneas en
`V1__schema.sql`, cero lógica, cero cambio de comportamiento.

| Columna | Existe solo para… | Habilita |
|---|---|---|
| `movement.atm_id` | poder responder "¿qué cajero dispensó esto?" | [C-10](#c-10--multi-divisa-auditoría-y-trazabilidad) (auditoría) |
| `account.currency` | fijar la divisa en vez de asumirla | [C-10](#c-10--multi-divisa-auditoría-y-trazabilidad) (multi-divisa) |
| `fee_policy.valid_from` / `valid_to` | fechar la vigencia de cada tarifa | [C-8](#c-8--tarifas-sin-versionado-temporal) (extracto histórico reproducible) |

La distinción importa porque las del grupo 2 son las que un revisor con prisa marcaría como "código
muerto". No lo son: son las que evitan un backfill sin información. `account.currency` es el caso
extremo — añadirla el día 400 obliga a decidir la divisa de cada fila histórica adivinando.

---

## Parte E — Timeboxing revisado

La tabla vigente está en `PLAN.md` §8. Aquí solo el porqué de cada ajuste, que suman cero: los
180 min siguen siendo 180.

| Ajuste | Min | Razón |
|---|---|---|
| Bloque 0: 20 → 30 | **+10** | Levantar proyecto, compose, 7 tablas con sus `CHECK` y el seed en 20 min no es realista |
| Ingresos + movimientos: 20 → 15 | **−5** | Son los dos bloques más mecánicos: sin comisión, sin política de dominio |
| Bloque de tests: 35 → 30 | **−5** | Solo E2E. Los unitarios se contabilizaban dos veces ([A-5](#a-5--el-bloque-de-tests-estaba-contabilizado-dos-veces)) y ahora van dentro de su feature |
| Dockerfile + OpenAPI: 20 → 15 | **−5** | El README sale de este bloque y se junta con el cierre |
| Cierre: 5 → 10 | **+5** | README + merge + tag no caben en 5 min. Y GitHub Actions sale del alcance ([C-9](#c-9--sin-pipeline-de-ci)) |

**Punto de corte declarado** (también en `PLAN.md` §8): si a las 2:05 los E2E no han empezado, se
sacrifican los escenarios 6 y 7 — transferencias y consulta de movimientos. **Nunca** el 1 (guardas),
el 3 (comisión) ni el 4 (crédito), que son los que demuestran que el dominio existe. Sacrificar un
escenario significa **borrarlo y decirlo en el README**, no dejarlo escrito y en rojo.

---

## Cómo se lee este documento en una revisión

Las entradas de la **parte B** son cosas que estarían mal si no se hicieran. Las de la **parte C**
son cosas que están bien hechas para el presupuesto que tienen, con la salida documentada. La
**parte D** es la que responde a la pregunta que importa: *si esto entrara en producción el lunes,
¿cuánto de lo que hay habría que tirar?* La respuesta pretendida es: nada del schema, nada del
dominio, y la capa de sesión.
