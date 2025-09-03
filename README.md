# TSB Banking API

Spring Boot 3.x demo implementing a secure banking-style REST API with JWT auth, password reset via OTP, double-entry transfers, and idempotency.

## Features

* **Accounts**: list all accounts for a customer
* **Transactions**: list transactions for an account (date range + paging)
* **Transfers**: intra-customer transfer (double-entry) with **idempotency**
* **Auth**: JWT **access** tokens + **rotating refresh** tokens, logout (AT blacklist optional)
* **Password reset**: OTP (mock SMS logged), secure reset flow
* **Docs**: OpenAPI/Swagger UI
* **Data**: Flyway migrations; H2 in dev, PostgreSQL in prod-ready profile

---

## Quick start (H2, zero setup)

```bash
# Run
mvn spring-boot:run

# Swagger UI
open http://localhost:8080/swagger

# H2 Console
open http://localhost:8080/h2-console   # JDBC URL: jdbc:h2:mem:bankdb, user: sa
```

**Seed user**: `alice / Password123` (bcrypt in seed or updated on first run)

---

## Authentication (JWT)

* **Login:** `POST /auth/login` → `{ accessToken, refreshToken, tokenType, expiresInSeconds }`
* **Use token:** add header `Authorization: Bearer <accessToken>` to protected endpoints
* **Refresh:** `POST /auth/refresh` with `{ "refreshToken": "<token>" }` returns a **new pair**; the **old refresh** is revoked

> Access tokens are **stateless** and remain valid until expiration (e.g., 15 minutes). Refresh rotation revokes only the **refresh** token by default.

---

## Core endpoints

### Auth

#### `POST /auth/login`

Obtain **access** and **refresh** tokens.

**Headers**

```
Content-Type: application/json
```

**Request**

```json
{
  "username": "alice",
  "password": "Password123"
}
```

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 900
}
```

**Errors**

* `401 Unauthorized` – bad credentials
* `400 Bad Request` – invalid body

---

#### `POST /auth/refresh`

Exchange a valid **refresh** token for a **new access + refresh** pair (rotation).

**Headers**

```
Content-Type: application/json
```

**Request**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 900
}
```

**Errors**

* `401 Unauthorized` – invalid/expired/revoked refresh token
* `400 Bad Request` – not a refresh token / malformed JSON

> Note: Access tokens stay valid until their `exp` unless you implement an **access-token blacklist** (optional).

---

### Accounts

```http
GET /customers/{customerId}/accounts
Authorization: Bearer <accessToken>
```

### Transactions

```http
GET /accounts/{accountId}/transactions?from=2025-08-27&to=2025-09-03&page=0&size=10
Authorization: Bearer <accessToken>
```

### Transfers (Requirement #4)

```http
POST /transfers
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "customerId": 1,
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": "100.00",
  "currency": "NZD",
  "memo": "Move to savings",
  "idempotencyKey": "a-uuid-or-random-string"
}
```

* **Idempotency:** repeated requests with the same `idempotencyKey` return the **original result**.

### Password reset with OTP (Requirement #5)

**Request OTP**

```bash
curl -X POST http://localhost:8080/auth/request-reset \
  -H "Content-Type: application/json" \
  -d '{"identifier":"alice"}'
# => {"sentToMasked":"+6****11"}  (mock SMS; OTP logged)
```

**Confirm reset**

```bash
curl -X POST http://localhost:8080/auth/confirm-reset \
  -H "Content-Type: application/json" \
  -d '{"identifier":"alice","code":"123456","newPassword":"Password123"}'
# => 200 OK "Password reset successful"
```

---

## Configuration

`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:bankdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    username: sa
    password: ""
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  h2:
    console:
      enabled: true
      path: /h2-console
  flyway:
    enabled: true

server:
  port: 8080

springdoc:
  swagger-ui:
    path: /swagger

app:
  security:
    jwt:
      secret: "REPLACE_WITH_BASE64_KEY"
      issuer: "tsb-bank"
      access-ttl-minutes: 15
      refresh-ttl-days: 7
```

**Production profile (suggested):** use PostgreSQL; keep Flyway enabled and `ddl-auto: validate`; externalize secrets via env vars.

---

## Tech Stack

* **Java 17**, **Spring Boot 3.3**
* Spring Web, Validation, Data JPA
* Spring Security 6 + JWT (**jjwt**)
* **Flyway**, **H2** (dev); PostgreSQL (prod-ready)
* **springdoc-openapi** (Swagger UI)

---

## Development tips

* **CORS**: enabled via `http.cors()`; configure origins if calling from a browser app
* **CSRF**: disabled for stateless APIs
* **Logs**: enable security debug if needed

  ```yaml
  logging:
    level:
      org.springframework.security: DEBUG
  ```
* **Troubleshooting**

    * `401`: missing/expired/invalid **access** token, or using a **refresh** token against APIs
    * `403`: CSRF (if re-enabled) or authorization rule mismatch
    * H2 is **in-memory**; restart resets data

---

## Roadmap / Next

* (Optional) **Access-token blacklist** by JTI for instant AT revoke
* Metrics / health endpoints (Actuator)
* Logout endpoint
* Dockerfile + Docker Compose
* Testcontainers for Postgres integration tests


---

## **License:** No License (public domain)
