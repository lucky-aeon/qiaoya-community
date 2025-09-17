package org.xhy.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("org.xhy.community.domain.*.repository")
@EnableAsync
public class CommunityApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}