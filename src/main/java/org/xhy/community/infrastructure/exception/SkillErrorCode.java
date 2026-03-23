package org.xhy.community.infrastructure.exception;

public enum SkillErrorCode implements ErrorCode {

    SKILL_NOT_FOUND(16000, "Skill 不存在"),
    UNAUTHORIZED_MODIFY(16001, "只能操作自己上传的 Skill");

    private final int code;
    private final String message;

    SkillErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
