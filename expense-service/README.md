# expense-service

## Stack
- Spring MVC + Spring Data JPA (Java 21 / Spring Boot managed by platform-bom)

## Dependencies to start first
- `expense-service-db` (PostgreSQL, provided in this service's `docker-compose.yml`)

Run:
```bash
cp ../.env.example ../.env
docker compose up -d --build
```
