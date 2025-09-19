package org.xhy.community.infrastructure.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮件服务测试
 * 注意：此测试需要正确配置邮件服务才能通过
 */
@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testEmailServiceAvailable() {
        // 测试邮件服务是否可用
        assertNotNull(emailService, "EmailService应该被正确注入");

        // 在测试环境中，邮件服务可能被禁用，这是正常的
        System.out.println("邮件服务状态: " + (emailService.isEnabled() ? "启用" : "禁用"));
    }

    @Test
    void testSendEmailWhenDisabled() {
        // 当邮件服务禁用时，应该返回false而不是抛出异常
        if (!emailService.isEnabled()) {
            boolean result = emailService.sendEmail(
                "test@example.com",
                "测试邮件",
                "<h1>这是一个测试邮件</h1><p>用于验证邮件服务功能</p>"
            );
            assertFalse(result, "邮件服务禁用时应该返回false");
            System.out.println("✓ 邮件服务禁用状态测试通过");
        }
    }

    /**
     * 手动测试邮件发送
     * 需要在配置文件中启用邮件服务并配置正确的SMTP信息
     * 运行前请修改收件人邮箱地址
     */
    // @Test
    void manualTestSendEmail() {
        if (emailService.isEnabled()) {
            String testEmail = "your-test-email@example.com"; // 请修改为实际邮箱

            boolean result = emailService.sendEmail(
                testEmail,
                "敲鸭社区邮件服务测试",
                generateTestEmailContent()
            );

            assertTrue(result, "邮件发送应该成功");
            System.out.println("✓ 测试邮件已发送到: " + testEmail);
        } else {
            System.out.println("⚠ 邮件服务未启用，跳过手动测试");
        }
    }

    private String generateTestEmailContent() {
        return "<html>" +
            "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
            "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
            "<h1 style=\"color: #4CAF50; border-bottom: 2px solid #4CAF50; padding-bottom: 10px;\">" +
            "🦆 敲鸭社区邮件服务测试" +
            "</h1>" +
            "<p>您好！</p>" +
            "<p>这是一封来自<strong>敲鸭社区</strong>的测试邮件，用于验证邮件发送功能是否正常工作。</p>" +
            "<div style=\"background-color: #f9f9f9; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;\">" +
            "<h3 style=\"margin-top: 0; color: #4CAF50;\">✅ 测试内容</h3>" +
            "<ul>" +
            "<li>HTML邮件格式支持</li>" +
            "<li>中文字符显示</li>" +
            "<li>样式渲染效果</li>" +
            "<li>阿里云Direct Mail SMTP集成</li>" +
            "</ul>" +
            "</div>" +
            "<p>如果您收到此邮件，说明邮件服务配置成功！🎉</p>" +
            "<div style=\"margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px;\">" +
            "<p>此邮件由敲鸭社区邮件服务自动发送，请勿回复。</p>" +
            "<p>发送时间: " + java.time.LocalDateTime.now() + "</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
}