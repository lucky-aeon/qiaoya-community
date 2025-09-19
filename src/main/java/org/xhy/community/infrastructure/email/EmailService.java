package org.xhy.community.infrastructure.email;

/**
 * 邮件发送服务接口
 */
public interface EmailService {

    /**
     * 发送邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（支持HTML）
     * @return 是否发送成功
     */
    boolean sendEmail(String to, String subject, String content);

    /**
     * 发送邮件（带发件人名称）
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（支持HTML）
     * @param senderName 发件人名称
     * @return 是否发送成功
     */
    boolean sendEmail(String to, String subject, String content, String senderName);

    /**
     * 检查邮件服务是否可用
     *
     * @return 是否可用
     */
    boolean isEnabled();
}