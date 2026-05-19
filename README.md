# Digital Wallet & P2P Payment API

A production-ready REST API built with Spring Boot for managing digital wallets and peer-to-peer money transfers.

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Security** with JWT authentication
- **Spring Data JPA** + **PostgresSQL**
- **Flyway** for database migrations
- **Swagger / OpenAPI 3** for API documentation
- **MapStruct** for DTO mapping
- **Lombok** to cut boilerplate

## Getting Started

### Prerequisites

- Java 17+
- PostgresSQL 14+
- Maven 3.8+

### Database Setup

```sql
CREATE DATABASE digital_wallet_db;
```

### Configuration

Copy `application.yml` and set the following environment variables (or override in a local `application-local.yml`):

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-256-bit-secret-key-here
```

### Run

```bash
mvn spring-boot:run
```

API will be available at `http://localhost:8080/api`  
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

## Project Structure

```
src/main/java/com/digitalwallet/
├── config/          # Spring config beans (OpenAPI, Security, etc.)
├── controller/      # REST controllers
├── domain/
│   ├── entity/      # JPA entities
│   └── enums/       # Status and type enums
├── dto/
│   ├── request/     # Incoming payload DTOs
│   └── response/    # Outgoing response DTOs
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, UserDetailsService, etc.
├── service/         # Business logic interfaces + implementations
├── util/            # Utility classes
└── validation/      # Custom constraint annotations
```

## API Endpoints (v1)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and get JWT |
| GET | `/wallets/me` | Get my wallets |
| POST | `/wallets` | Create a new wallet |
| POST | `/wallets/{id}/deposit` | Deposit funds |
| POST | `/wallets/{id}/withdraw` | Withdraw funds |
| POST | `/transfers` | Send money to another wallet |
| GET | `/transactions` | List my transactions |
| GET | `/transactions/{reference}` | Get transaction by reference |
