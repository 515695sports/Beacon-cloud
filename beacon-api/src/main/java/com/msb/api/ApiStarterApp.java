package com.msb.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {
        "com.msb.api",
        "com.msb.common"
})
public class ApiStarterApp {

    public static void main(String[] args) {
        SpringApplication.run(ApiStarterApp.class,args);
    }
}
