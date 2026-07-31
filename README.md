# API de Cajeros — NextDigital (prueba técnica)

API REST que modela las operaciones de un cajero automático: activación de tarjeta, cambio de PIN,
retirada, ingreso, transferencia y consulta de movimientos, con distinción real entre tarjeta de
débito y de crédito.

- Plan de trabajo y reglas de negocio: [`PLAN.md`](PLAN.md)
- Registro de decisiones y deuda técnica: [`DECISIONS.md`](DECISIONS.md)

## Stack

Java 21 · Spring Boot 3.5 · Maven · PostgreSQL · Flyway · Spring Data JPA · BCrypt ·
JUnit 5 + MockMvc + Testcontainers · springdoc-openapi

## Puesta en marcha

```bash
docker compose up -d     # Postgres
./mvnw spring-boot:run   # app en :8080
```

Swagger UI en `/swagger-ui.html`.

## Tests

```bash
./mvnw test              # levanta su propio Postgres vía Testcontainers
./mvnw -q verify         # build completo
```

Requieren un demonio Docker corriendo. No dependen del `docker compose`.

## Modelo de dominio

_Pendiente._

## Endpoints

_Pendiente._

## Decisiones de diseño

_Pendiente — ver [`DECISIONS.md`](DECISIONS.md)._

## Fuera de alcance

_Pendiente._
