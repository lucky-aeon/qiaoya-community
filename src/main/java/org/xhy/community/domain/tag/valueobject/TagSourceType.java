package org.xhy.community.domain.tag.valueobject;

/**
 * 标签授予的来源类型枚举。
 * 用于标记“为何/通过何种途径”给用户授予了某个标签，便于审计、展示与后续统计。
 *
 * 说明：
 * - 存储层通过自定义 TypeHandler 将枚举与 VARCHAR 互转。
 * - 业务侧应尽量传入明确的 sourceId（如课程ID、操作单据ID），以便追踪。
 */
public enum TagSourceType {
    /**
     * 人工发放（后台运营或管理端手动授予）。
     * 典型场景：管理员在控制台为用户添加身份/成就类标签。
     * sourceId：建议填写操作单据/任务ID（可为空）。
     */
    MANUAL,

    /**
     * 课程完成自动发放。
     * 典型场景：用户完成某课程后由系统监听“课程完成事件”自动授予“课程完成”类标签。
     * sourceId：课程ID（必填）。
     */
    COURSE_COMPLETION;

    public static TagSourceType fromCode(String code) {
        for (TagSourceType t : values()) {
            if (t.name().equals(code)) return t;
        }
        throw new IllegalArgumentException("Unknown TagSourceType: " + code);
    }
}
