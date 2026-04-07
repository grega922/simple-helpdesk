## Project Structure

```
src/main/java/org/acme/helpdesk/
  api/              REST resource classes (AuthApi, ConversationApi, OperatorApi)
  config/           Startup seeder, global exception mapper
  dto/              Request/response DTOs
  entity/           JPA entities (User, Conversation, Message, Rooms)
  enums/            UserRole, ConversationStatus
  jwt/              JWT token generation service
  services/         Business logic (ConversationService, MessageService)

src/main/resources/
  application.properties   Configuration
  db/migration/            Flyway SQL migrations
  privateKey.pem           JWT signing key
  publicKey.pem            JWT verification key

src/test/java/org/acme/helpdesk
  AuthApiTest.java         Authentication tests
  ConversationApiTest.java User conversation tests
  OperatorApiTest.java     Operator conversation tests
```

## Tech Stack

- **Quarkus 3.27.3** — Supersonic Subatomic Java Framework
- **Hibernate ORM + Panache** — Active Record pattern for JPA
- **Flyway** — Database schema migrations
- **SmallRye JWT** — JWT authentication (RS256)
- **PostgreSQL 16** — Relational database
- **SmallRye OpenAPI** — Swagger UI and OpenAPI spec generation
- **Hibernate Validator** — Bean validation
- **REST-assured** — API testing