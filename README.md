# A-Farms-backend

FarmStack Backend: Microservice-based farm management API with multi-tenancy, Clerk auth, and subscription services. Java 21 + Spring Boot + PostgreSQL + Docker.

## Scaffolded services
- income-service
- expense-service
- investment-service
- transaction-service
- user-service
- api-gateway

## Quick start
```bash
cp .env.example .env
./start-all-services.sh
```

The script creates the shared external network (`dev-network`) if needed, then starts all service compose files.

## Local standalone testing (without API Gateway)
Each microservice can run directly without going through `api-gateway`.

- `income-service` default port: `8081`
- `expense-service` default port: `8082`
- `investment-service` default port: `8083`
- `transaction-service` default port: `8084`
- `user-service` default port: `8085`

Run any service independently:
```bash
cd <service-folder>
../mvnw spring-boot:run
```

`api-gateway` now defaults to local routes (`http://localhost:8081` ... `http://localhost:8085`) and can still be overridden with env vars for Docker/networked deployments.

## Version alignment
To prevent version drift, all modules use the shared `platform-bom` module.

## Port allocation
See [`PORT_REGISTRY.md`](PORT_REGISTRY.md).
