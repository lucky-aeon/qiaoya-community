-- 课程完成证书/完成标识
CREATE TABLE IF NOT EXISTS course_certificates (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    certificate_no VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    meta JSONB,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_course_certificate_user_course
ON course_certificates (user_id, course_id)
WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_course_certificate_no
ON course_certificates (certificate_no);

CREATE INDEX IF NOT EXISTS idx_course_certificate_user ON course_certificates (user_id);
CREATE INDEX IF NOT EXISTS idx_course_certificate_course ON course_certificates (course_id);

COMMENT ON TABLE course_certificates IS '课程完成证书/完成标识';
COMMENT ON COLUMN course_certificates.id IS '主键ID（UUID）';
COMMENT ON COLUMN course_certificates.user_id IS '用户ID';
COMMENT ON COLUMN course_certificates.course_id IS '课程ID';
COMMENT ON COLUMN course_certificates.certificate_no IS '证书编号（唯一）';
COMMENT ON COLUMN course_certificates.status IS '证书状态：ISSUED/REVOKED';
COMMENT ON COLUMN course_certificates.issued_at IS '颁发时间';
COMMENT ON COLUMN course_certificates.revoked_at IS '撤销时间（如已撤销）';
COMMENT ON COLUMN course_certificates.meta IS '扩展信息（JSONB）';
COMMENT ON COLUMN course_certificates.create_time IS '创建时间';
COMMENT ON COLUMN course_certificates.update_time IS '更新时间';
COMMENT ON COLUMN course_certificates.deleted_at IS '逻辑删除时间（NULL 表示未删除）';
