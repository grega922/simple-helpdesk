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

-- 3 - SQL Script to create conversations table
CREATE TABLE conversations (
    id SERIAL PRIMARY KEY,
    room_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,     -- User who created the conversation
    operator_id INTEGER,           -- Operater (nullable, until assigned)
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'ACTIVE', 'CLOSED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_operator FOREIGN KEY (operator_id) REFERENCES users(id)
);

-- 4 - SQL Script to create messages table
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,

    CONSTRAINT fk_sender
        FOREIGN KEY (sender_id) REFERENCES users(id)
);

--5 - SQL indexes for faster polling
CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at);
CREATE INDEX idx_conversations_status ON conversations(status);
CREATE INDEX idx_conversations_operator ON conversations(operator_id);

-- Test users to be inserted
INSERT INTO users (username, password, role) VALUES 
('JanezNovak', 'Janez123', 'USER'),
('AnaKovac', 'Ana456', 'USER'),
('Marko Krajnc', 'Marko789', 'USER'),
('Operater_Petra', 'PetraOp123', 'OPERATOR'),
('Operater_Luka', 'LukaOp123', 'OPERATOR');

-- Test rooms to be inserted
INSERT INTO rooms (name, description) VALUES 
('Tehnika', 'Tehnična pomoč uporabnikom'),
('Storitve', 'Informacije o storitvah in naročninah'),
('Pogovor', 'Splošni pogovor z operaterjem');