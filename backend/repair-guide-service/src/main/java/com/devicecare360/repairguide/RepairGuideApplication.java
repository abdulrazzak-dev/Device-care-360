package com.devicecare360.repairguide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RepairGuideApplication {
    public static void main(String[] args) {
        SpringApplication.run(RepairGuideApplication.class, args);
    }
}
