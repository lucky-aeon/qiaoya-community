package org.xhy.community.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Ensure JVM uses Asia/Shanghai as default timezone to align DB/application times.
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void setDefaultTimeZone() {
        // Set default JVM timezone at application startup
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        System.setProperty("user.timezone", "Asia/Shanghai");
    }
}

