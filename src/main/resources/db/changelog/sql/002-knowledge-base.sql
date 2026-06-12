--liquibase formatted sql

--changeset nutro-assist:002-knowledge-sources splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='knowledge_sources'
CREATE TABLE IF NOT EXISTS knowledge_sources (
    id           BIGSERIAL    PRIMARY KEY,
    file_name    VARCHAR(255) NOT NULL,
    file_type    VARCHAR(50)  NOT NULL,
    total_chunks INT          NOT NULL DEFAULT 0,
    uploaded_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

--changeset nutro-assist:002-knowledge-chunks splitStatements:false
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='knowledge_chunks'
CREATE TABLE IF NOT EXISTS knowledge_chunks (
    id          BIGSERIAL PRIMARY KEY,
    source_id   BIGINT    NOT NULL REFERENCES knowledge_sources(id) ON DELETE CASCADE,
    content     TEXT      NOT NULL,
    chunk_index INT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chunks_source_id ON knowledge_chunks(source_id);
