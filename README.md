
# Home Loan Management App

Spring Boot + PostgreSQL + JWT application implementing:
- Login (JWT Auth, Roles: USER/ADMIN)
- Home Loan Offerings
- Home Loan Dashboard
- New Home Loan (EMI calculator + apply)
- Home Loan Tracker (message for loans < 2 weeks)

## Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL running locally with credentials:
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/postgres`
  - `spring.datasource.username=postgres`
  - `spring.datasource.password=root`

> No Flyway. Schema is created automatically via `spring.jpa.hibernate.ddl-auto=update`.

## Run
```bash
mvn spring-boot:run
```

### Seeded users
- admin / admin123 (ROLE_ADMIN)
- customer1 / password (ROLE_USER)

## Endpoints (base URL: `http://localhost:8080`)
- Auth: `POST /api/auth/login`, `POST /api/auth/register`
- Products: `GET /api/products`, `GET /api/products/{id}`
- Loans: `POST /api/loans/emi`, `POST /api/loans/apply`, `GET /api/loans`, `GET /api/loans/{accountNumber}`
- Admin: `POST /api/loans/admin/{accountNumber}/activate`
```
