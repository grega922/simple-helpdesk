# Simple Helpdesk API

REST API for a helpdesk chat system built with [Quarkus](https://quarkus.io/).  
Users create support conversations, operators claim and respond to them.

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| **Java** | 17 or 21 | JDK required (e.g. Eclipse Temurin, GraalVM) |
| **Docker** | 20+ | Required for DevServices (auto-starts PostgreSQL) and production database |
| **Maven** | 3.9+ | Included via Maven Wrapper (`mvnw` / `mvnw.cmd`) — no separate install needed |

> **Windows note:** Use `mvnw.cmd` instead of `./mvnw` for all commands below.

## Quick Start

### 1. Run in Development Mode

Quarkus DevServices automatically starts a PostgreSQL container — no manual database setup required.

```shell
./mvnw quarkus:dev
```

The application starts on **http://localhost:8080** with live-reload enabled.  
The Dev UI is available at **http://localhost:8080/q/dev/**.

### 2. Seed Data

On first startup the application automatically seeds:

| Type | Username | Password | Role |
|------|----------|----------|------|
| User | `JanezNovak` | `Janez123` | USER |
| User | `AnaKovac` | `Ana456` | USER |
| User | `MarkoKrajnc` | `Marko789` | USER |
| Operator | `Operater_Petra` | `PetraOp123` | OPERATOR |
| Operator | `Operater_Luka` | `LukaOp123` | OPERATOR |

Three rooms are also created: `TEHNIKA`, `STORITVE`, `POGOVOR`.

## API Documentation (Swagger UI)

Available **only in dev mode**:

| Resource | URL |
|----------|-----|
| Swagger UI | [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui) |

Use the **Authorize** button in Swagger UI to paste a JWT token obtained from `POST /v1/auth/login`.

## API Endpoints Overview

See [APIENDPOINTS.md](APIENDPOINTS.md) for API endpoints quicklist with short descriptions

Postman collections are found in **Simple Helpdesk API.postman_collection.json**

## Testing

### Run Unit / Integration Tests

```shell
./mvnw test
```

Quarkus DevServices starts a temporary PostgreSQL container for the test run automatically.

### Run with Verbose Output

```shell
./mvnw test -Dsurefire.useFile=false
```

### Run a Single Test Class

```shell
./mvnw test -Dtest=AuthApiTest
./mvnw test -Dtest=ConversationApiTest
./mvnw test -Dtest=OperatorApiTest
./mvnw test -Dtest=RoomApiTest
```

## Building and Packaging

### Build JAR (Standard)

```shell
./mvnw package
```

Produces `target/quarkus-app/quarkus-run.jar` (fast-jar format with dependencies in `target/quarkus-app/lib/`).

### Build Uber-JAR (Single Fat JAR)

```shell
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

Produces `target/*-runner.jar` — a single self-contained JAR.

### Build Native Executable (GraalVM)

```shell
./mvnw package -Dnative
```

Or build inside a container (no local GraalVM required):

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Production Deployment

### 1. Start the Database

```shell
docker compose up -d
```

This starts:
- **PostgreSQL 16** on port `5432` (database: `helpdesk_db`, user/password: `helpdesk`)
- **pgAdmin 4** on port `5050` (login: `admin@admin.com` / `admin`)

### 2. Run the Application

**From JAR:**

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

**From Uber-JAR:**

```shell
java -jar target/*-runner.jar
```

**From Native Executable:**

```shell
./target/code-with-quarkus-1.0.0-SNAPSHOT-runner
```

The production profile connects to `localhost:5432/helpdesk_db` with credentials `helpdesk/helpdesk` (configured in `application.properties`).

### 3. Stop the Database

```shell
docker compose down
```

To also remove persisted data:

```shell
docker compose down -v
```

## Database Schema

See [DATABASE.md](DATABASE.md) for the full entity-relationship diagram and table definitions.

## Project Structure

See [PROJECT.md](PROJECT.md) for file structure of the project and tech stack used with this project 
