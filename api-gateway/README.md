# api-gateway

## Stack
- Spring WebFlux via Spring Cloud Gateway (Java 21 / Spring Boot managed by platform-bom)

## Dependencies to start first
- `user-service`
- `income-service`
- `expense-service`
- `investment-service`
- `transaction-service`

Run:
```bash
docker compose up -d --build
```

Local run defaults route to services on localhost:
- `income-service`: `http://localhost:8081`
- `expense-service`: `http://localhost:8082`
- `investment-service`: `http://localhost:8083`
- `transaction-service`: `http://localhost:8084`
- `user-service`: `http://localhost:8085`

Override any route with `INCOME_SERVICE_URL`, `EXPENSE_SERVICE_URL`, `INVESTMENT_SERVICE_URL`, `TRANSACTION_SERVICE_URL`, and `USER_SERVICE_URL`.
