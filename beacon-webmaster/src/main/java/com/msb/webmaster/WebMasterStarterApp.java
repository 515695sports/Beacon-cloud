package com.msb.webmaster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.msb.webmaster.mapper")
public class WebMasterStarterApp {
    public static void main(String[] args) {
        SpringApplication.run(WebMasterStarterApp.class,args);
    }
}
