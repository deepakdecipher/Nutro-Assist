--liquibase formatted sql

--changeset nutro-assist:002-knowledge-sources
CREATE TABLE knowledge_sources (
    id          BIGSERIAL PRIMARY KEY,
    file_name   VARCHAR(255) NOT NULL,
    file_type   VARCHAR(50)  NOT NULL,
    total_chunks INT         NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP            DEFAULT NOW()
);

--changeset nutro-assist:002-knowledge-chunks
CREATE TABLE knowledge_chunks (
    id          BIGSERIAL PRIMARY KEY,
    source_id   BIGINT  NOT NULL REFERENCES knowledge_sources(id) ON DELETE CASCADE,
    content     TEXT    NOT NULL,
    chunk_index INT     NOT NULL
);

CREATE INDEX idx_chunks_source_id ON knowledge_chunks(source_id);
