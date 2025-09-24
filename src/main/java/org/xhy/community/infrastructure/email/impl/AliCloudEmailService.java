package org.xhy.community.infrastructure.email.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.config.EmailConfig;
import org.xhy.community.infrastructure.email.EmailService;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * 阿里云Direct Mail邮件发送服务实现
 */
@Service
public class AliCloudEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(AliCloudEmailService.class);

    private final EmailConfig emailConfig;
    private Session mailSession;

    public AliCloudEmailService(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
        initMailSession();
    }

    /**
     * 初始化邮件会话
     */
    private void initMailSession() {
        if (!emailConfig.getSmtp().isEnabled()) {
            log.warn("邮件服务已禁用");
            return;
        }

        EmailConfig.Smtp smtp = emailConfig.getSmtp();

        // 配置SMTP属性
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", smtp.getHost());
        props.put("mail.smtp.port", smtp.getPort());
        props.put("mail.smtp.from", smtp.getUsername());
        props.put("mail.user", smtp.getUsername());
        props.put("mail.password", smtp.getPassword());

        // 用于解决附件名过长导致的显示异常
        System.setProperty("mail.mime.splitlongparameters", "false");

        // 构建授权信息
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtp.getUsername(), smtp.getPassword());
            }
        };

        // 创建邮件会话
        this.mailSession = Session.getInstance(props, authenticator);

        log.info("邮件服务初始化完成: host={}, port={}, username={}",
                smtp.getHost(), smtp.getPort(), smtp.getUsername());
    }

    @Override
    public boolean sendEmail(String to, String subject, String content) {
        return sendEmail(to, subject, content, emailConfig.getSmtp().getSenderName());
    }

    @Override
    public boolean sendEmail(String to, String subject, String content, String senderName) {
        if (!isEnabled()) {
            log.warn("邮件服务未启用，跳过发送");
            return false;
        }

        try {
            // 生成Message-ID
            String messageIDValue = generateMessageID(emailConfig.getSmtp().getUsername());

            MimeMessage message = new MimeMessage(mailSession) {
                @Override
                protected void updateMessageID() throws MessagingException {
                    setHeader("Message-ID", messageIDValue);
                }
            };

            // 设置发件人
            InternetAddress from = new InternetAddress(
                emailConfig.getSmtp().getUsername(),
                senderName != null ? senderName : emailConfig.getSmtp().getSenderName()
            );
            message.setFrom(from);

            // 设置收件人
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // 设置邮件主题
            message.setSubject(subject);

            // 设置发送时间
            message.setSentDate(new Date());

            // 创建多重消息以支持HTML内容
            Multipart multipart = new MimeMultipart();

            // 创建HTML内容部分
            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(content, "text/html;charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // 设置邮件内容
            message.setContent(multipart);

            // 发送邮件
            Transport.send(message);

            log.info("邮件发送成功: to={}, subject={}", to, subject);
            return true;

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("邮件发送失败: to={}, subject={}, error={}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return emailConfig.getSmtp().isEnabled() &&
               mailSession != null &&
               isConfigValid();
    }

    /**
     * 检查配置是否有效
     */
    private boolean isConfigValid() {
        EmailConfig.Smtp smtp = emailConfig.getSmtp();
        return smtp.getUsername() != null &&
               !smtp.getUsername().trim().isEmpty() &&
               smtp.getPassword() != null &&
               !smtp.getPassword().trim().isEmpty();
    }

    /**
     * 生成Message-ID
     */
    private String generateMessageID(String mailFrom) {
        if (!mailFrom.contains("@")) {
            throw new org.xhy.community.infrastructure.exception.ValidationException(
                org.xhy.community.infrastructure.config.ValidationErrorCode.EMAIL_FORMAT_INVALID,
                "Invalid email format: " + mailFrom
            );
        }
        String domain = mailFrom.split("@")[1];
        UUID uuid = UUID.randomUUID();
        return "<" + uuid.toString() + "@" + domain + ">";
    }
}
