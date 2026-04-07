### Authentication (`/v1/auth`) — no token required

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/auth/login` | Login with username/password, receive JWT |

### User Conversations (`/v1/conversations`) — requires `USER` role

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/conversations` | List current user's conversations |
| POST | `/v1/conversations/new` | Create conversation with initial message |
| GET | `/v1/conversations/{id}` | Get conversation details |
| GET | `/v1/conversations/{id}/messages` | Get messages (supports `?since=` polling) |
| POST | `/v1/conversations/{id}/messages` | Send message (conversation must be ACTIVE) |

### Operator Conversations (`/v1/operator/conversations`) — requires `OPERATOR` role

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/operator/conversations/waiting` | List all WAITING conversations |
| GET | `/v1/operator/conversations/active` | List operator's ACTIVE conversations |
| POST | `/v1/operator/conversations/{id}/claim` | Claim a WAITING conversation |
| GET | `/v1/operator/conversations/{id}` | Get conversation details |
| GET | `/v1/operator/conversations/{id}/messages` | Get messages (supports `?since=` polling) |
| POST | `/v1/operator/conversations/{id}/messages` | Reply to the user |
