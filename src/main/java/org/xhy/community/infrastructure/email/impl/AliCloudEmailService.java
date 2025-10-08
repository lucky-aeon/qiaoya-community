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
import java.util.*;

import org.xhy.community.infrastructure.exception.ValidationException;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.domain.notification.valueobject.BatchSendConfig;

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
    public boolean sendEmail(List<String> to, String subject, String content) {
        return sendEmail(to, subject, content, emailConfig.getSmtp().getSenderName());
    }

    @Override
    public boolean sendEmail(List<String> to, String subject, String content, String senderName) {
        if (!isEnabled()) {
            log.warn("邮件服务未启用，跳过发送");
            return false;
        }

        if (to.isEmpty()){
            return true;
        }

        // 使用批量发送配置（默认值：batchSize=50, delay=1s, 跳过错误, 无重试）
        BatchSendConfig config = new BatchSendConfig();

        int total = to.size();
        int batchSize = Math.max(1, config.getBatchSize());
        int batchCount = (total + batchSize - 1) / batchSize;

        int successBatches = 0;
        int failedBatches = 0;

        if (config.isLogDetail()) {
            log.info("开始批量发送邮件: 共{}人, 批大小={}, 批次数={} subject={}", total, batchSize, batchCount, subject);
        }

        for (int start = 0, batchIndex = 1; start < total; start += batchSize, batchIndex++) {
            int end = Math.min(start + batchSize, total);
            List<String> batch = to.subList(start, end);

            boolean sent = sendOneBatchWithRetry(batch, subject, content, senderName, config, batchIndex, batchCount);
            if (sent) {
                successBatches++;
            } else {
                failedBatches++;
                if (!config.isSkipOnError()) {
                    if (config.isLogDetail()) {
                        log.warn("批量发送中止：第{}/{}批失败且配置为不跳过错误", batchIndex, batchCount);
                    }
                    return false;
                }
            }

            // 批次间延迟（最后一批不延迟）
            if (end < total && config.getDelayBetweenBatches() > 0) {
                try {
                    Thread.sleep(config.getDelayBetweenBatches());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (config.isLogDetail()) {
            log.info("批量发送完成: 成功批次={}, 失败批次={}, subject={}", successBatches, failedBatches, subject);
        }

        // 所有批次均成功则返回true，否则返回false
        return failedBatches == 0;
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
            throw new ValidationException(
                ValidationErrorCode.EMAIL_FORMAT_INVALID,
                "Invalid email format: " + mailFrom
            );
        }
        String domain = mailFrom.split("@")[1];
        UUID uuid = UUID.randomUUID();
        return "<" + uuid.toString() + "@" + domain + ">";
    }

    /**
     * 发送单个批次（带重试）
     */
    private boolean sendOneBatchWithRetry(List<String> batch,
                                          String subject,
                                          String content,
                                          String senderName,
                                          BatchSendConfig config,
                                          int batchIndex,
                                          int batchCount) {
        int attempts = 0;
        int maxAttempts = Math.max(1, config.getMaxRetries() + 1); // 初次 + 重试次数
        Exception lastEx = null;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                sendOneMessage(batch, subject, content, senderName);
                if (config.isLogDetail()) {
                    log.info("第{}/{}批发送成功，共{}人，尝试次数={}", batchIndex, batchCount, batch.size(), attempts);
                }
                return true;
            } catch (Exception e) {
                lastEx = e;
                log.error("第{}/{}批发送失败，尝试次数={}，错误={}，收件人={}", batchIndex, batchCount, attempts, e.getMessage(), batch, e);
                // 简单线性等待，可根据需要改为指数退避
                if (attempts < maxAttempts) {
                    try {
                        Thread.sleep(Math.min(3000, Math.max(200, config.getDelayBetweenBatches())));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        if (config.isLogDetail()) {
            log.warn("第{}/{}批最终失败，共{}人，最后错误: {}", batchIndex, batchCount, batch.size(), lastEx != null ? lastEx.getMessage() : "unknown");
        }
        return false;
    }

    /**
     * 实际发送单封邮件（一个批次，多个收件人）
     */
    private void sendOneMessage(List<String> recipients, String subject, String content, String senderName)
            throws MessagingException, UnsupportedEncodingException {
        // 生成Message-ID（每批一封信件）
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

        InternetAddress[] addresses = new InternetAddress[recipients.size()];
        for (int i = 0; i < recipients.size(); i++) {
            addresses[i] = new InternetAddress(recipients.get(i));
        }
        // 设置收件人
        message.setRecipients(Message.RecipientType.TO, addresses);

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
    }
}
