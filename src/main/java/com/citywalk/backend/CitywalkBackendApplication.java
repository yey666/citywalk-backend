package com.citywalk.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.citywalk.backend.mapper")
public class CitywalkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitywalkBackendApplication.class, args);
    }
}