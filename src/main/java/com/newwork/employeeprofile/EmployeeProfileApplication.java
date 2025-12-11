package com.newwork.employeeprofile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableMongoAuditing
public class EmployeeProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeProfileApplication.class, args);
    }
}
