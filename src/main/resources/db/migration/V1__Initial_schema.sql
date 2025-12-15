-- Account 테이블 생성
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Account 테이블 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_account_email ON account(email);
CREATE INDEX IF NOT EXISTS idx_account_uuid ON account(uuid);
CREATE INDEX IF NOT EXISTS idx_account_created_at ON account(created_at);

-- Board 테이블 생성
CREATE TABLE IF NOT EXISTS board (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    nick_name VARCHAR(20) NOT NULL,
    account_uuid VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Board 테이블 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_board_uuid ON board(uuid);
CREATE INDEX IF NOT EXISTS idx_board_account_uuid ON board(account_uuid);
CREATE INDEX IF NOT EXISTS idx_board_created_at ON board(created_at);
CREATE INDEX IF NOT EXISTS idx_board_account_created ON board(account_uuid, created_at);

-- CommonFile 테이블 생성
CREATE TABLE IF NOT EXISTS common_file (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    table_name VARCHAR(255) NOT NULL,
    table_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    change_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension VARCHAR(50) NOT NULL,
    delete_yn VARCHAR(1) NOT NULL DEFAULT 'N',
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_uuid_table_name_table_id UNIQUE (uuid, table_name, table_id)
);

-- CommonFile 테이블 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_common_file_uuid ON common_file(uuid);
CREATE INDEX IF NOT EXISTS idx_common_file_table ON common_file(table_name, table_id);
CREATE INDEX IF NOT EXISTS idx_common_file_delete_yn ON common_file(delete_yn);
CREATE INDEX IF NOT EXISTS idx_common_file_created_at ON common_file(created_at);

