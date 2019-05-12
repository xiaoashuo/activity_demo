package com.lovecyy.activity.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableTransactionManagement
public class ActivityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityDemoApplication.class, args);
    }

}
