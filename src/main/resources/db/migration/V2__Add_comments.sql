-- 테이블 및 컬럼에 주석 추가
COMMENT ON TABLE account IS '사용자 계정 테이블';
COMMENT ON COLUMN account.id IS '계정 ID';
COMMENT ON COLUMN account.uuid IS 'UUID';
COMMENT ON COLUMN account.email IS '이메일';
COMMENT ON COLUMN account.password_hash IS '비밀번호 해시';
COMMENT ON COLUMN account.name IS '이름';
COMMENT ON COLUMN account.created_at IS '생성일시';

COMMENT ON TABLE board IS '게시판 테이블';
COMMENT ON COLUMN board.id IS '게시판 ID';
COMMENT ON COLUMN board.uuid IS 'SELECT, DELETE, UPDATE에서 사용될 UUID';
COMMENT ON COLUMN board.title IS '제목';
COMMENT ON COLUMN board.content IS '본문내용';
COMMENT ON COLUMN board.nick_name IS '등록자';
COMMENT ON COLUMN board.account_uuid IS '등록자의 UUID';
COMMENT ON COLUMN board.created_at IS '생성일시';

COMMENT ON TABLE common_file IS '공통 파일 테이블';
COMMENT ON COLUMN common_file.id IS '파일 ID';
COMMENT ON COLUMN common_file.uuid IS 'SELECT, DELETE, UPDATE에서 사용될 UUID';
COMMENT ON COLUMN common_file.table_name IS '등록한 테이블 이름';
COMMENT ON COLUMN common_file.table_id IS '등록한 테이블의 ID';
COMMENT ON COLUMN common_file.original_file_name IS '원본 파일 이름';
COMMENT ON COLUMN common_file.change_file_name IS '변경된 파일 이름';
COMMENT ON COLUMN common_file.file_path IS '파일경로';
COMMENT ON COLUMN common_file.file_size IS '파일크기';
COMMENT ON COLUMN common_file.file_extension IS '파일확장자';
COMMENT ON COLUMN common_file.delete_yn IS '삭제여부 (Y: 삭제, N: 미삭제)';
COMMENT ON COLUMN common_file.deleted_at IS '삭제일시';
COMMENT ON COLUMN common_file.created_at IS '등록일시';

