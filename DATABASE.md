# Database Schema

## Entity-Relationship Diagram

```mermaid
erDiagram
    users {
        SERIAL id PK
        VARCHAR(50) username UK "NOT NULL"
        VARCHAR(255) password "NOT NULL, bcrypt hash"
        VARCHAR(20) role "NOT NULL, CHECK (USER | OPERATOR)"
    }

    rooms {
        SERIAL id PK
        VARCHAR(50) name UK "NOT NULL"
        TEXT description
    }

    conversation {
        SERIAL id PK
        BIGINT room_id FK "NOT NULL"
        BIGINT user_id FK "NOT NULL"
        BIGINT operator_id FK "nullable until claimed"
        VARCHAR(255) title "NOT NULL"
        VARCHAR(20) status "NOT NULL, DEFAULT WAITING"
        TIMESTAMPTZ created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    message {
        SERIAL id PK
        BIGINT conversation_id FK "NOT NULL"
        BIGINT sender_id FK "NOT NULL"
        VARCHAR(20) sender_type "NOT NULL (USER | OPERATOR)"
        TEXT content "NOT NULL"
        TIMESTAMPTZ created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    users ||--o{ conversation : "creates (user_id)"
    users ||--o{ conversation : "claims (operator_id)"
    rooms ||--o{ conversation : "belongs to"
    conversation ||--o{ message : "contains"
    users ||--o{ message : "sends (sender_id)"
```

## Conversation Lifecycle

```mermaid
stateDiagram-v2
    [*] --> WAITING : User creates conversation
    WAITING --> ACTIVE : Operator claims conversation
    ACTIVE --> CLOSED : Conversation resolved
    CLOSED --> [*]
```

## Table Details

### users

Stores both regular users and operators. The `role` column determines API access.

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | SERIAL | PRIMARY KEY |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL |
| `password` | VARCHAR(255) | NOT NULL (bcrypt hash) |
| `role` | VARCHAR(20) | NOT NULL, CHECK (`USER`, `OPERATOR`) |

### rooms

Predefined support categories that conversations are created under.

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | SERIAL | PRIMARY KEY |
| `name` | VARCHAR(50) | UNIQUE, NOT NULL |
| `description` | TEXT | |

Seeded rooms: `TEHNIKA`, `STORITVE`, `POGOVOR`

### conversation

A support ticket linking a user to a room, optionally assigned to an operator.

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | SERIAL | PRIMARY KEY |
| `room_id` | BIGINT | NOT NULL, FK → `rooms(id)` |
| `user_id` | BIGINT | NOT NULL, FK → `users(id)` |
| `operator_id` | BIGINT | FK → `users(id)`, nullable until claimed |
| `title` | VARCHAR(255) | NOT NULL |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT `WAITING`, CHECK (`WAITING`, `ACTIVE`, `CLOSED`) |
| `created_at` | TIMESTAMPTZ | DEFAULT `CURRENT_TIMESTAMP` |

### message

Chat messages within a conversation, sent by either the user or the operator.

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | SERIAL | PRIMARY KEY |
| `conversation_id` | BIGINT | NOT NULL, FK → `conversation(id)` ON DELETE CASCADE |
| `sender_id` | BIGINT | NOT NULL, FK → `users(id)` |
| `sender_type` | VARCHAR(20) | NOT NULL (`USER` or `OPERATOR`) |
| `content` | TEXT | NOT NULL |
| `created_at` | TIMESTAMPTZ | DEFAULT `CURRENT_TIMESTAMP` |

## Indexes

| Index | Table | Columns | Purpose |
|-------|-------|---------|---------|
| `idx_message_conversation` | message | `conversation_id, created_at` | Fast message polling with `?since=` filter |
| `idx_conversation_status` | conversation | `status` | Fast lookup of WAITING conversations |
| `idx_conversation_operator` | conversation | `operator_id` | Fast lookup of operator's active conversations |

## Migration

Schema is managed by **Flyway**. Migration files are in `src/main/resources/db/migration/`.

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__init.sql` | Creates all tables, constraints, sequences, and indexes |
