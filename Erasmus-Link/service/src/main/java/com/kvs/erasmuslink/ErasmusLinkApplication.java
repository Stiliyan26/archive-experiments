package com.kvs.erasmuslink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.kvs.erasmuslink", exclude= {UserDetailsServiceAutoConfiguration.class})
public class ErasmusLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErasmusLinkApplication.class, args);
    }
}