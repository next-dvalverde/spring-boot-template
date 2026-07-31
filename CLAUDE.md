# CLAUDE.md — API de Cajeros (NextDigital, prueba técnica)

Prueba técnica de backend. **Presupuesto: 3 horas.** El plan completo está en `PLAN.md`;
este fichero son las reglas de trabajo. Si algo aquí contradice a `PLAN.md`, gana `PLAN.md`.

`DECISIONS.md` es el registro fechado de decisiones y deuda técnica. **Toda decisión que se tome
por falta de tiempo se anota ahí antes de seguir**, con su plan B y el coste de migrar. Las
referencias `[A-n]` / `[B-n]` / `[C-n]` de `PLAN.md` apuntan a ese fichero.

## Contexto y criterio de éxito

Lo que se evalúa no es el volumen de código: es **el dominio bancario bien modelado, los tests
que lo demuestran y un repo que se entiende solo**. Ante cualquier duda entre "más features" y
"lo que hay, correcto y probado", elige lo segundo.

Regla de oro del timeboxing: **no empieces nada que no puedas terminar y testear.** Si un bloque
se desvía del tramo asignado en `PLAN.md` §8, corta por lo sano, deja lo que hay funcionando y
documenta el recorte en el README (§9 "Fuera de alcance").

## Stack

Java 21 · Spring Boot 3 · Maven · PostgreSQL (docker-compose) · Flyway · Spring Data JPA ·
BCrypt · JUnit 5 + MockMvc + **Testcontainers** · springdoc-openapi.

**No añadas dependencias** fuera de esta lista sin justificarlo. En particular:

- **Nada de Spring Security completo.** Filtro propio ligero para la sesión. Decisión tomada.
- **Nada de Lombok** si no está ya en el `pom.xml`; records de Java para DTOs.
- **Nada de Apache Commons / librerías de IBAN.** El validador mod-97 se implementa a mano (~15 líneas).
- **Nada de MapStruct.** Mapeos a mano en métodos estáticos o `record ... from(Entity)`.
- **Nada de H2.** Los E2E van contra Testcontainers con la misma imagen de Postgres del compose:
  un `V1__schema.sql` que solo pasa en H2 no es el schema de producción.

## Arquitectura

```
com.nextdigital.atm
├── config/       beans, SessionFilter, OpenAPI
├── domain/       entidades + enums + REGLAS PURAS (sin Spring)
├── repository/
├── service/      orquestación + @Transactional
├── web/          controllers, DTOs, GlobalExceptionHandler
└── resources/db/migration/  V1__schema.sql, V2__seed.sql
```

Capas clásicas con **dominio rico**. La regla que separa las capas:

- `domain/` — `FeeCalculator`, `IbanValidator`, `WithdrawalPolicy`, `PinPolicy`. **Cero anotaciones
  de Spring, cero repositorios, cero I/O.** Reciben datos, devuelven decisiones. Es donde vive el
  valor evaluable y donde los tests son instantáneos.
  - Corolario: `FeeCalculator` **no lee `fee_policy`**. El servicio carga la política y se la pasa
    como argumento (`record FeePolicy`). Si el dominio necesita un dato, se le pasa; no va a buscarlo.
- `service/` — carga entidades, invoca el dominio, persiste, abre transacción. No contiene
  aritmética de dinero ni condicionales de negocio: los delega.
- `web/` — valida forma (`@Valid`), traduce a DTO. Nunca lógica.

Si te encuentras escribiendo un `if (balance < amount)` dentro de un `@Service`, está en el sitio
equivocado.

## Reglas de dinero — innegociables

1. **`BigDecimal` siempre**, escala 2, `RoundingMode.HALF_UP`. Un `double` o un `float` en el
   camino del dinero es un fallo de la prueba.
2. **La comisión es un `Movement` de tipo `FEE`**, nunca un descuento silencioso dentro de la
   retirada. El extracto tiene que ser auditable línea a línea.
3. **Crédito ≠ débito.** Una operación con tarjeta de crédito **no toca `account.balance`**:
   incrementa `card.creditUsed`, y sus apuntes llevan `settlesAccount = false`. Es la distinción
   conceptual que más pesa en la corrección.
4. **`Movement.amount` es siempre positivo.** El signo lo da el tipo, vía `signOf(MovementType)`
   en `domain/`. Nunca guardes un importe negativo ni deduzcas la dirección del signo.
5. **Dos invariantes** (`PLAN.md` §6), y todo test E2E que mueva dinero debe afirmar las dos: la del
   balance filtra por `settles_account = TRUE`, la de `creditUsed` por `FALSE`. Si una falla,
   **se arregla el código, jamás la aserción**.
6. `amount > 0` en las tres operaciones, no solo en la retirada: `AmountPolicy` en `domain/` +
   `@Positive` en el DTO + `CHECK` en el schema. Un ingreso negativo es una retirada sin límite.
7. Toda operación de dinero genera un `operationId` que comparten **todas** sus líneas: la `FEE`
   tiene que poder decir de qué retirada viene.
8. El PIN va **siempre** con BCrypt (`spring-security-crypto`, el artefacto suelto — no el starter;
   esto no rompe la regla de "nada de Spring Security"). Nunca en claro, nunca en un log, nunca en
   una respuesta, nunca en un mensaje de excepción. Con 4 dígitos el espacio son 10⁴ combinaciones:
   **quien protege de verdad es el contador de intentos, no el hash.**

### Orden de las guardas (importa, y se testea)

Antes de cualquier operación, en este orden exacto (detalle en `PLAN.md` §3):

1. Tarjeta existe → `404 CARD_NOT_FOUND`
2. `status == ACTIVE` → `INACTIVE` da `CARD_NOT_ACTIVATED`, `BLOCKED` da `CARD_BLOCKED`.
   **Dos códigos distintos**, nunca uno solo
3. `pinChangeRequired == false` → `409 PIN_CHANGE_REQUIRED` (solo pasa `PUT /cards/pin`)
4. PIN correcto → al 3.er fallo, `status = BLOCKED`. **Un acierto resetea el contador a 0.**

`POST /cards/activation` es la **única** excepción al orden: invierte la guarda 2 (exige `INACTIVE`)
pero sí ejecuta la 4 con el mismo contador — es el único endpoint sin token y no puede ser un
oráculo de PIN gratuito.

Después, las reglas específicas de cada operación (`PLAN.md` §3).

### Seguridad por diseño

El cliente **nunca envía un `accountId`** en operaciones de dinero. La cuenta sale del token de
sesión, que se emite contra `cardNumber + pin + atmId`. Operar sobre una cuenta ajena tiene que
ser imposible por construcción, no por una comprobación. No introduzcas endpoints que rompan esto.

Excepción única: `GET /accounts/{id}/movements`, que valida que la cuenta pertenece **al titular
de la tarjeta** (puede tener varias) → si no, **`404`, no `403`**. Un `403` confirmaría que la
cuenta existe y sería el oráculo de enumeración que la regla de abajo prohíbe. El cliente no puede
distinguir "no es tuya" de "no existe".

## Convenciones de código

- **Código, identificadores, nombres de tabla, commits y ramas: en inglés.** Comentarios y README:
  español. Solo comenta el *porqué* de una decisión, nunca el *qué* hace la línea.
- Errores de negocio → excepción propia con **código estable**, capturada por
  `@RestControllerAdvice` → `{ code, message, timestamp }`. Nada de `RuntimeException` genéricas ni
  de mensajes que revelen estado interno (saldos, existencia de cuentas ajenas).

  **Catálogo canónico** — esta es la única lista; `PLAN.md` remite aquí:

  | HTTP | Código |
  |---|---|
  | 400 | `INVALID_IBAN`, `INVALID_PIN_FORMAT`, `INVALID_AMOUNT` |
  | 401 | `INVALID_PIN`, `INVALID_SESSION` |
  | 403 | `FOREIGN_ATM_DEPOSIT_NOT_ALLOWED` |
  | 404 | `CARD_NOT_FOUND`, `ATM_NOT_FOUND`, `ACCOUNT_NOT_FOUND` |
  | 409 | `CARD_NOT_ACTIVATED`, `CARD_BLOCKED`, `CARD_ALREADY_ACTIVE`, `PIN_CHANGE_REQUIRED`, `INSUFFICIENT_FUNDS`, `LIMIT_EXCEEDED`, `CREDIT_LIMIT_EXCEEDED`, `OPERATION_NOT_SUPPORTED_FOR_CARD_TYPE`, `CONCURRENT_MODIFICATION` |

  Añadir un código sin añadirlo aquí es dejar la tabla mintiendo.
- **Ninguna excepción de infraestructura escapa como 500.** El conflicto de bloqueo optimista se
  captura como `org.springframework.orm.ObjectOptimisticLockingFailureException` — **no** como
  `jakarta.persistence.OptimisticLockException`, que Spring Data ya ha traducido y que nunca llega
  al advice.
- **El schema lo gestiona Flyway.** `ddl-auto: validate`, jamás `update` ni `create-drop`. Una
  migración nunca se edita una vez commiteada: se añade una nueva.
- DTOs de entrada y salida separados de las entidades. Una entidad JPA no cruza el controller.
- `@Transactional` en la capa de servicio, nunca en el controller.
- Constructor injection. Sin `@Autowired` en campos.

## Tests

Dos niveles, y ninguno es opcional:

- **Unitarios** de `domain/` — sin contexto de Spring, milisegundos. `IbanValidatorTest`,
  `FeeCalculatorTest`, `WithdrawalPolicyTest`, `AmountPolicyTest`, `PinPolicyTest`. Cubren casos
  límite, no el camino feliz. Se escriben **en el bloque de su feature**, no al final.
- **E2E** con `@SpringBootTest` + MockMvc + Testcontainers, contra el schema real de Flyway. Los 7
  escenarios de `PLAN.md` §6. Cada uno recorre un flujo completo por HTTP. Un solo contenedor
  `static` reutilizado por toda la suite.

Reglas:

- **Un test por regla de negocio, empezando por el fallo.** El caso feliz solo demuestra que
  compila; los rechazos demuestran que el dominio existe.
- Nada de mockear repositorios en los E2E. Base de datos real, datos de `V2__seed.sql`.
- Los tests son parte del entregable, no un extra al final: cada `feature/*` se mergea **con sus
  tests en verde**, no antes.
- Si un test falla, se arregla el código. Nunca se relaja la aserción para que pase.

## Git

- Flujo: `main` ← `develop` ← `feature/*`. Una rama por bloque de `PLAN.md` §7.
- **Conventional commits** (`feat:`, `test:`, `fix:`, `chore:`, `docs:`), pequeños y atómicos.
  El mensaje describe el cambio, no el fichero tocado.
- Merge a `develop` con `--no-ff`: el grafo tiene que contar la historia del desarrollo.
- Primer commit: `.gitignore` + README esqueleto, **antes de escribir código**.
- Tag `v1.0.0` al final, sobre `main`.
- Nunca hagas `push --force`, ni commits directos a `main` o `develop`, ni `git add .` a ciegas.
- No pidas confirmación para commitear dentro de una rama `feature/*`: hazlo. **Sí pregunta antes
  de mergear a `develop` o `main`, y antes de taguear.**

## Comandos

```bash
docker compose up -d              # Postgres — solo para spring-boot:run
./mvnw spring-boot:run            # arrancar la app
./mvnw test                       # toda la suite (levanta su propio Postgres vía Testcontainers)
./mvnw test -Dtest=FeeCalculatorTest   # un test suelto
./mvnw -q verify                  # build completo antes de mergear
```

Los tests **no** dependen del `docker compose`: Testcontainers arranca su propio Postgres. Lo único
que necesitan es un demonio Docker corriendo. Esa es la precondición de `[C-9]`.

App en `:8080`, Swagger en `/swagger-ui.html`.

## Cómo quiero que trabajes

- **Ejecuta, no propongas.** Si el paso está en `PLAN.md`, hazlo directamente; no pidas permiso
  para escribir código que ya está planificado.
- **Verifica antes de decir que algo funciona.** "Implementado" solo es válido si has corrido los
  tests y los has visto pasar. No afirmes un resultado que no has observado.
- **Sé conciso.** Al terminar un bloque: qué se hizo, tests en verde, siguiente paso. Sin resúmenes
  largos ni recapitulaciones de lo que ya se ha visto.
- **Avisa del reloj.** Si un bloque se está comiendo el tramo siguiente, dilo y propón el recorte.
- **Si una decisión de diseño no está en `PLAN.md` y afecta al modelo o a la API, pregunta.**
  Si es interna y reversible, decide tú y sigue.
- **Todo atajo se anota en `DECISIONS.md` en el momento, no al final.** Una entrada de deuda sin
  plan B ni coste de migrar no es deuda: es un descuido. Y si un atajo no tiene salida documentada,
  no se toma.
- No generes README, javadoc ni ficheros auxiliares que no se hayan pedido. El README final es un
  bloque con su tramo asignado (2:50–3:00) y enlaza a `DECISIONS.md`, que vale tanto como el código.
