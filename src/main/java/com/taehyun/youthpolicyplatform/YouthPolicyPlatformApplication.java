package com.taehyun.youthpolicyplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class YouthPolicyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouthPolicyPlatformApplication.class, args);
    }

}
