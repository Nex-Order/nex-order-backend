# Nex Order API

Nex Order is a REST API built with Java 21 and Spring Boot 3.5.7, 
designed to manage sales—including customers, employees, products, 
orders, and payments—and featuring JWT-based login for employees. 
It follows a layered architecture (controller → service → repository → JPA entity) 
within a single Maven module, with the root package `com.aceleradev.nexorder`.

## 🚀 Technologies

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **H2 Database** (development)
- **PostgreSQL** (production)
- **Spring Security**
- **JWT**
- **MapStruct**
- **Lombok**
- **Swagger**
- **Docker**
- **Docker Compose**
- **Maven**

## Running with Docker

1. Copy the example file and set your database credentials:
   ```bash
   cp .env.example .env
   ```
2. Build and start the app:
   ```bash
   docker compose up --build
   ```
3. API: http://localhost:8080 · Swagger: http://localhost:8080/swagger


## 📂 Project Structure and Main Folders

nex-order-backend/
├── pom.xml, mvnw, mvnw.cmd, .mvn/      build Maven + wrapper
├── Dockerfile, docker-compose.yaml     imagem e execução em container
├── .env.example                        modelo das variáveis do banco
├── docs/roadmap/backlog.json           roadmap: milestones, sprints e issues
├── scripts/import-github-backlog.ps1   importa o backlog para o GitHub
└── src/
├── main/java/com/aceleradev/nexorder/
│   ├── NexOrderApplication.java    ponto de entrada (main)
│   ├── commons/enums/              enums de domínio
│   ├── config/                     OpenAPI e seed do perfil test
│   ├── controllers/                endpoints REST
│   ├── dtos/                       objetos de entrada/saída por módulo
│   ├── entities/                   entidades JPA
│   ├── infra/security/             JWT, filtro e SecurityFilterChain
│   ├── mapper/                     interfaces MapStruct
│   ├── repositories/               interfaces JpaRepository
│   └── services/                   regras de negócio
├── main/resources/application.properties
└── test/
├── java/.../NexOrderApplicationTests.java
├── java/.../repositories/OrderRepositoryTest.java
└── resources/application-test.properties