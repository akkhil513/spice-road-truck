# 🌶️ Spice Road Truck

A full-stack food truck platform for managing a digital menu of dishes. The backend is a RESTful API built with **Quarkus** (Java 17), backed by **PostgreSQL** on **AWS RDS**, with optional **AWS Secrets Manager** integration for credential management.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [File-by-File Breakdown](#file-by-file-breakdown)
  - [Root](#root)
  - [Backend – Build & Config](#backend--build--config)
  - [Backend – Source Code](#backend--source-code)
  - [Backend – Resources](#backend--resources)
  - [Backend – Docker](#backend--docker)
  - [Backend – Build Output](#backend--build-output)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Docker Deployment](#docker-deployment)
- [Configuration](#configuration)

---

## Architecture Overview

```
┌──────────────┐       HTTP/JSON        ┌─────────────────────┐       JDBC        ┌──────────────┐
│              │  ───────────────────▶   │   Quarkus Backend   │  ──────────────▶   │  PostgreSQL  │
│    Client    │                         │   (JAX-RS + CDI)    │                    │  (AWS RDS)   │
│              │  ◀───────────────────   │                     │  ◀──────────────   │              │
└──────────────┘                         └─────────────────────┘                    └──────────────┘
                                                  │
                                                  │ (optional)
                                                  ▼
                                         ┌─────────────────────┐
                                         │  AWS Secrets Manager │
                                         └─────────────────────┘
```

The backend follows a clean **three-layer architecture**:

1. **API Layer** (`api/`) – JAX-RS REST resources that handle HTTP requests/responses.
2. **Service Layer** (`service/`) – Business logic and transaction management.
3. **Repository Layer** (`repository/`) – Data access via Hibernate ORM with Panache.
4. **Entity Layer** (`entity/`) – JPA entity definitions mapping to database tables.

---

## Tech Stack

| Component              | Technology                                  |
| ---------------------- | ------------------------------------------- |
| **Language**           | Java 17                                     |
| **Framework**          | Quarkus 3.19.2                              |
| **REST**               | Quarkus REST (Jakarta RESTful Web Services) |
| **ORM**                | Hibernate ORM with Panache                  |
| **Database**           | PostgreSQL (AWS RDS)                        |
| **Serialization**      | Jackson (via `quarkus-rest-jackson`)        |
| **API Docs**           | SmallRye OpenAPI + Swagger UI               |
| **Secrets Management** | AWS Secrets Manager (Quarkiverse 2.16.0)    |
| **DI**                 | Quarkus Arc (CDI)                           |
| **Build Tool**         | Apache Maven (with Maven Wrapper)           |
| **Containerization**   | Docker (JVM, Legacy JAR, Native, Native Micro) |
| **Testing**            | JUnit 5, REST Assured                       |

---

## Project Structure

```
spice-road-truck/
├── README.md                          # This file – project documentation
├── backend/
│   ├── pom.xml                        # Maven project descriptor (dependencies, plugins, build config)
│   ├── mvnw                           # Maven Wrapper script (Unix/macOS)
│   ├── mvnw.cmd                       # Maven Wrapper script (Windows)
│   ├── README.md                      # Quarkus-generated backend README
│   └── src/
│       └── main/
│           ├── java/com/spiceroad/
│           │   ├── api/
│           │   │   └── DishResource.java       # REST controller – HTTP endpoints for dishes
│           │   ├── service/
│           │   │   └── DishService.java        # Business logic – CRUD operations on dishes
│           │   ├── repository/
│           │   │   └── DishRepository.java     # Data access – Panache repository for Dish entity
│           │   └── entity/
│           │       └── Dish.java               # JPA entity – maps to the "dish" database table
│           ├── resources/
│           │   └── application.properties      # Application configuration (DB, Hibernate, OpenAPI)
│           └── docker/
│               ├── Dockerfile.jvm              # Docker image for JVM mode
│               ├── Dockerfile.legacy-jar       # Docker image for legacy JAR mode
│               ├── Dockerfile.native           # Docker image for native executable (UBI 9 minimal)
│               └── Dockerfile.native-micro     # Docker image for native executable (micro image)
```

---

## File-by-File Breakdown

### Root

| File | Description |
| ---- | ----------- |
| **`README.md`** | Top-level project documentation (this file). Provides an overview of the architecture, tech stack, API reference, and instructions for building and running the application. |

### Backend – Build & Config

| File | Description |
| ---- | ----------- |
| **`backend/pom.xml`** | Maven POM file defining the project coordinates (`com.spiceroad:backend:1.0.0-SNAPSHOT`), Quarkus BOM (`3.19.2`), and all dependencies: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-smallrye-openapi`, `quarkus-hibernate-orm-panache`, `quarkus-jdbc-postgresql`, `quarkus-amazon-secretsmanager` (Quarkiverse `2.16.0`), `quarkus-arc`, plus test dependencies (`quarkus-junit5`, `rest-assured`). Configures Maven Compiler Plugin (Java 17), Surefire, Failsafe, and the Quarkus Maven Plugin for build, code generation, and test generation. |
| **`backend/mvnw`** | Maven Wrapper shell script for Unix/macOS. Allows running Maven commands (`./mvnw ...`) without requiring a global Maven installation. Automatically downloads the correct Maven version. |
| **`backend/mvnw.cmd`** | Maven Wrapper batch script for Windows. Windows equivalent of `mvnw`. |
| **`backend/README.md`** | Auto-generated Quarkus README with instructions for running in dev mode (`./mvnw quarkus:dev`), packaging, building native executables, and links to Quarkus guides for Hibernate ORM Panache, SmallRye OpenAPI, REST, and PostgreSQL JDBC. |

### Backend – Source Code

#### `backend/src/main/java/com/spiceroad/entity/Dish.java`

**JPA Entity – `Dish`**

- Extends `PanacheEntity` (provides auto-generated `id` field of type `Long` and built-in CRUD/query methods).
- Mapped to the `dish` table in PostgreSQL.
- **Fields:**

  | Field | Type | Description |
  | --- | --- | --- |
  | `id` | `Long` | Primary key (inherited from `PanacheEntity`, auto-generated) |
  | `name` | `String` | Name of the dish (e.g., "Butter Chicken Wrap") |
  | `description` | `String` | Textual description of the dish |
  | `price` | `double` | Price of the dish |

---

#### `backend/src/main/java/com/spiceroad/repository/DishRepository.java`

**Panache Repository – `DishRepository`**

- Annotated with `@ApplicationScoped` (CDI singleton bean).
- Implements `PanacheRepository<Dish>`, which provides ready-made methods: `listAll()`, `findById()`, `persist()`, `deleteById()`, `count()`, `find()`, etc.
- Currently contains no custom query methods – all standard CRUD is inherited from Panache.

---

#### `backend/src/main/java/com/spiceroad/service/DishService.java`

**Service Layer – `DishService`**

- Annotated with `@ApplicationScoped` (CDI singleton bean).
- Injects `DishRepository` via `@Inject`.
- **Methods:**

  | Method | Annotation | Description |
  | --- | --- | --- |
  | `getAllDishes()` | – | Returns `List<Dish>` of all dishes via `dishRepository.listAll()` |
  | `getDish(Long id)` | – | Returns a single `Dish` by primary key, or `null` if not found |
  | `createDish(Dish dish)` | `@Transactional` | Resets `dish.id` to `null` to ensure Hibernate treats it as a new entity, persists it, and returns the saved entity with generated ID |
  | `updateDish(Long id, Dish updated)` | `@Transactional` | Finds existing dish by ID; if found, updates `name`, `description`, and `price` fields (dirty-checking auto-flushes changes); returns updated entity or `null` |
  | `deleteDish(Long id)` | `@Transactional` | Deletes dish by ID; returns `true` if the entity existed and was deleted, `false` otherwise |

---

#### `backend/src/main/java/com/spiceroad/api/DishResource.java`

**REST Resource – `DishResource`**

- Base path: **`/srt/v1/dishes`**
- Produces and consumes `application/json`.
- Injects `DishService` via `@Inject`.
- **Endpoints:**

  | HTTP Method | Path | Method | Description | Success Response | Error Response |
  | --- | --- | --- | --- | --- | --- |
  | `GET` | `/srt/v1/dishes` | `getDishes()` | List all dishes | `200 OK` – JSON array of dishes | – |
  | `GET` | `/srt/v1/dishes/{id}` | `getDishById(Long id)` | Get a single dish by ID | `200 OK` – JSON dish object | Throws `RuntimeException` if not found |
  | `POST` | `/srt/v1/dishes` | `addDish(Dish dish)` | Create a new dish | `201 Created` – JSON saved dish | – |
  | `PUT` | `/srt/v1/dishes/{id}` | `updateDish(Long id, Dish dish)` | Update an existing dish | `200 OK` – JSON updated dish | `404 Not Found` if ID doesn't exist |
  | `DELETE` | `/srt/v1/dishes/{id}` | `deleteDish(Long id)` | Delete a dish by ID | `204 No Content` | `404 Not Found` if ID doesn't exist |

---

### Backend – Resources

#### `backend/src/main/resources/application.properties`

Central configuration file for the Quarkus application:

| Property | Value | Purpose |
| --- | --- | --- |
| `quarkus.devservices.enabled` | `false` | Disables Quarkus Dev Services (no automatic Docker containers in dev mode) |
| `quarkus.datasource.devservices.enabled` | `false` | Disables automatic datasource provisioning |
| `quarkus.aws.region` | `us-east-1` | AWS region for Secrets Manager client |
| `quarkus.datasource.username` | `admin_srt` | Database username (hardcoded; Secrets Manager alternative is commented out) |
| `quarkus.datasource.password` | *(hardcoded)* | Database password (Secrets Manager alternative is commented out) |
| `quarkus.datasource.db-kind` | `postgresql` | Database type |
| `quarkus.datasource.jdbc.url` | `jdbc:postgresql://...rds.amazonaws.com:5432/srt_db` | JDBC connection URL pointing to AWS RDS PostgreSQL |
| `quarkus.hibernate-orm.database.generation` | `update` | Hibernate auto-DDL strategy – updates schema to match entities |
| `quarkus.hibernate-orm.log.sql` | `true` | Logs all SQL statements to the console |
| `quarkus.smallrye-openapi.path` | `/openapi` | Path to the OpenAPI specification document |
| `quarkus.swagger-ui.path` | `/swagger` | Path to the Swagger UI interface |

> **Note:** AWS Secrets Manager integration is configured but currently commented out. To enable it, uncomment the `${aws-secretsmanager:...}` lines and remove the hardcoded credentials.

---

### Backend – Docker

Four Dockerfile variants are provided for containerizing the application:

| File | Mode | Base Image | Description |
| --- | --- | --- | --- |
| **`Dockerfile.jvm`** | JVM | `ubi9/openjdk-17-runtime` | Runs the Quarkus fast-jar on a JVM. Uses `run-java.sh` with automatic memory tuning. Supports `JAVA_OPTS`, `JAVA_OPTS_APPEND`, `JAVA_MAX_MEM_RATIO`, etc. |
| **`Dockerfile.legacy-jar`** | Legacy JAR (JVM) | `ubi9/openjdk-17-runtime` | Runs a traditional single JAR (`-Dquarkus.package.jar.type=legacy-jar`). Same JVM tuning options as the JVM variant. |
| **`Dockerfile.native`** | Native | `ubi9/ubi-minimal:9.6` | Runs a GraalVM native executable. Minimal UBI 9 base image. No JVM required at runtime. |
| **`Dockerfile.native-micro`** | Native (Micro) | `ubi9-quarkus-micro-image:2.0` | Runs a GraalVM native executable on an even smaller micro base image, optimized for Quarkus native apps. |

All Dockerfiles expose port **8080** and run as non-root user (`UID 1001`).

---

### Backend – Build Output

| Path | Description |
| ---- | ----------- |
| `backend/target/backend-1.0.0-SNAPSHOT.jar` | Compiled project JAR (thin JAR, not directly runnable) |
| `backend/target/quarkus-app/quarkus-run.jar` | **Main entry point** – the fast-jar launcher. Run with `java -jar target/quarkus-app/quarkus-run.jar` |
| `backend/target/quarkus-app/app/` | Contains the application classes JAR |
| `backend/target/quarkus-app/lib/boot/` | Quarkus bootstrap JARs (classloader, logging, etc.) |
| `backend/target/quarkus-app/lib/main/` | Third-party dependency JARs (Jackson, Hibernate, Netty, Vert.x, etc.) |
| `backend/target/quarkus-app/quarkus/` | Quarkus-generated bytecode and application metadata |
| `backend/target/classes/` | Compiled `.class` files and copied resources |
| `backend/target/quarkus-artifact.properties` | Metadata about the built artifact type and path |

---

## API Reference

Base URL: `http://localhost:8080`

### Dishes

| Method | Endpoint | Request Body | Response |
| ------ | -------- | ------------ | -------- |
| `GET` | `/srt/v1/dishes` | – | `200` – Array of all dishes |
| `GET` | `/srt/v1/dishes/{id}` | – | `200` – Single dish object |
| `POST` | `/srt/v1/dishes` | `{ "name": "...", "description": "...", "price": 0.0 }` | `201` – Created dish with ID |
| `PUT` | `/srt/v1/dishes/{id}` | `{ "name": "...", "description": "...", "price": 0.0 }` | `200` – Updated dish |
| `DELETE` | `/srt/v1/dishes/{id}` | – | `204` – No content |

### Example

```bash
# Create a dish
curl -X POST http://localhost:8080/srt/v1/dishes \
  -H "Content-Type: application/json" \
  -d '{"name": "Butter Chicken Wrap", "description": "Creamy butter chicken in a warm naan wrap", "price": 12.99}'

# List all dishes
curl http://localhost:8080/srt/v1/dishes

# Get a dish by ID
curl http://localhost:8080/srt/v1/dishes/1

# Update a dish
curl -X PUT http://localhost:8080/srt/v1/dishes/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Spicy Butter Chicken Wrap", "description": "Extra spicy butter chicken in a warm naan wrap", "price": 13.99}'

# Delete a dish
curl -X DELETE http://localhost:8080/srt/v1/dishes/1
```

### Interactive API Docs

- **Swagger UI:** [http://localhost:8080/swagger](http://localhost:8080/swagger)
- **OpenAPI Spec:** [http://localhost:8080/openapi](http://localhost:8080/openapi)

---

## Getting Started

### Prerequisites

- **Java 17+** (or use the Maven Wrapper which handles Maven itself)
- **PostgreSQL** database (local or AWS RDS)

### Run in Dev Mode

```bash
cd backend
./mvnw quarkus:dev
```

> Dev UI available at: [http://localhost:8080/q/dev/](http://localhost:8080/q/dev/)

### Package the Application

```bash
cd backend
./mvnw package
```

### Run the Packaged Application

```bash
java -jar backend/target/quarkus-app/quarkus-run.jar
```

### Build an Über-JAR

```bash
cd backend
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### Build a Native Executable (requires GraalVM)

```bash
cd backend
./mvnw package -Dnative
./target/backend-1.0.0-SNAPSHOT-runner
```

---

## Docker Deployment

### JVM Mode

```bash
cd backend
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t spice-road-truck/backend-jvm .
docker run -i --rm -p 8080:8080 spice-road-truck/backend-jvm
```

### Native Mode

```bash
cd backend
./mvnw package -Dnative
docker build -f src/main/docker/Dockerfile.native -t spice-road-truck/backend .
docker run -i --rm -p 8080:8080 spice-road-truck/backend
```

### Native Micro Mode (smallest image)

```bash
cd backend
./mvnw package -Dnative
docker build -f src/main/docker/Dockerfile.native-micro -t spice-road-truck/backend-micro .
docker run -i --rm -p 8080:8080 spice-road-truck/backend-micro
```

---

## Configuration

All configuration is in `backend/src/main/resources/application.properties`. Key settings can be overridden via environment variables using the Quarkus naming convention:

```bash
# Override database URL
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/srt_db

# Override credentials
export QUARKUS_DATASOURCE_USERNAME=myuser
export QUARKUS_DATASOURCE_PASSWORD=mypassword
```

### Enabling AWS Secrets Manager

To pull database credentials from AWS Secrets Manager instead of hardcoding them, update `application.properties`:

```properties
quarkus.datasource.username=${aws-secretsmanager:prod/srt-db/credentials:username}
quarkus.datasource.password=${aws-secretsmanager:prod/srt-db/credentials:password}
```

Ensure the application has appropriate AWS IAM permissions to access the secret `prod/srt-db/credentials`.

---

## License

This project is private and not currently licensed for public distribution.
