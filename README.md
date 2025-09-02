# TSB Banking API

Spring Boot 3.x demo implementing:
1. REST API skeleton
2. List **all accounts for a customer**
3. List **all transactions for an account** (date range + paging)
4. Transfer between accounts (double-entry + idempotency)

## Run (H2, zero setup)
```bash
mvn spring-boot:run
# Swagger UI
open http://localhost:8080/swagger
# H2 Console
open http://localhost:8080/h2-console  # JDBC: jdbc:h2:mem:bankdb, user: sa
```

## Sample
```bash
# Accounts of customer 1 (Alice)
curl http://localhost:8080/customers/1/accounts

# Transactions of account 1 (last 7 days), page=0,size=10
curl "http://localhost:8080/accounts/1/transactions?page=0&size=10"

## Transfer API (Requirement #4)
`POST /transfers`
```json
{
  "customerId": 1,
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": "100.00",
  "currency": "NZD",
  "memo": "Move to savings",
  "idempotencyKey": "e.g. a-uuid"
}
```

## Password Reset with OTP (Requirement #5)

**Request OTP**
```bash
curl -X POST http://localhost:8080/auth/request-reset \
  -H "Content-Type: application/json" \
  -d '{"identifier":"alice"}'
# => {"sentToMasked":"+6****11"}
# OTP is logged in app logs (mock SMS)
```
**Reset Password**
```bash
curl -X POST http://localhost:8080/auth/confirm-reset \
  -H "Content-Type: application/json" \
  -d '{"identifier":"alice","code":"213836","newPassword":"Password123"}'
# => 200 OK "Password reset successful"
``` 


## Tech
- Java 17, Spring Boot 3.3, Spring Data JPA, H2 (dev), Flyway, springdoc-openapi

## Next
- Transfers with double-entry + idempotency
- Security (JWT) + Password reset with OTP
- Switch prod profile to PostgreSQL
```
