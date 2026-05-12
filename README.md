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
./start-all-services.sh
```

The script creates the shared external network (`dev-network`) if needed, then starts all service compose files.

## Version alignment
To prevent version drift, all modules use the shared `platform-bom` module.

## Port allocation
See [`PORT_REGISTRY.md`](PORT_REGISTRY.md).
