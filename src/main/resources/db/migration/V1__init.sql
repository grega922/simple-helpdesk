-- 1 - SQL Script to create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Tukaj bo shranjen hash gesla
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'OPERATOR'))
);

-- 2 - SQL Script to create rooms table
CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- 3 - SQL Script to create conversation table
CREATE TABLE conversation (
    id SERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,     -- User who created the conversation
    operator_id BIGINT,           -- Operater (nullable, until assigned)
    title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'ACTIVE', 'CLOSED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_operator FOREIGN KEY (operator_id) REFERENCES users(id)
);

-- 4 - SQL Script to create messages table
CREATE TABLE message (
    id SERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE,

    CONSTRAINT fk_sender
        FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE SEQUENCE app_user_seq START 1;

ALTER TABLE users 
ALTER COLUMN id SET DEFAULT nextval('app_user_seq');


--5 - SQL indexes for faster polling
CREATE INDEX idx_message_conversation ON message(conversation_id, created_at);
CREATE INDEX idx_conversation_status ON conversation(status);
CREATE INDEX idx_conversation_operator ON conversation(operator_id);
